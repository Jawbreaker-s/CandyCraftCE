package cn.jawbreakers.candycraftce.client.level

import cn.jawbreakers.candycraftce.client.PuddingColor
import cn.jawbreakers.candycraftce.mixin_stub.ISpecialEffectsAddition
import cn.jawbreakers.candycraftce.utils.CUtils.use
import cn.jawbreakers.candycraftce.utils.ClientOnly
import cn.jawbreakers.candycraftce.utils.LinearGradient
import cn.jawbreakers.candycraftce.utils.LinearGradient.Companion.normal
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Axis
import net.minecraft.client.Camera
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.DimensionSpecialEffects
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import java.awt.Color
import kotlin.random.Random

@ClientOnly
object CandyEffects : DimensionSpecialEffects(192.0f, true, SkyType.NORMAL, false, false),
    ISpecialEffectsAddition {

    private val fogColor = Color(0xEEAABB)
    private val fogNormal = fogColor.normal

    /** 群系未提供 sky_color / 区块未加载时的兜底昼空色 */
    private val skyColor = Color(0xFDD8D7)

    /**
     * 午夜→夜→黄昏/黎明→正午 的夜空底色，ratio = 昼夜因子（0=午夜，1=正午）。
     * 正午端最终会被群系 sky_color 覆盖（见 renderSky 里的 dayWeight 混合），
     * 这条渐变只负责夜晚与晨昏的糖果色相。
     */
    private val skyTint = LinearGradient {
        Color(0x321326) % 0f
        Color(0x3E1830) % 0.22f
        Color(0x6E2B4A) % 0.40f
        Color(0xB4647E) % 0.52f
        skyColor % 1f
    }

    private const val DAY_LENGTH = 24000f

    /** 夜间雾/清屏色的亮度下限，防止地平线纯黑 */
    private const val FOG_NIGHT_FLOOR = 0.22f

    /**
     * 由renderSky每帧写入。雾色与清屏色在 renderSky 之前就由原版算好了，
     * 所以这里取的是上一帧的值（滞后一帧，肉眼不可见）。
     */
    @Volatile
    private var lastDayFactor: Float = 1.0f

    private const val SKY_BOX_RADIUS = 128.0f
    private const val STAR_RADIUS = 120.0f
    private const val STAR_COUNT = 400

    /**
     * 上半球均匀分布的星星（单位方向 + 贴片大小 + 颜色）。
     * 用固定种子在类加载时生成一次，保证每帧、每次进入世界都完全一致。
     */
    private val stars: Array<Star> = Array(STAR_COUNT) { index ->
        val random = Random(0xC0FFEE + index)
        // cos(θ) 在 [0,1] 均匀分布即上半球面积均匀分布
        val dy = random.nextFloat() * 0.95f + 0.03f
        val ring = Mth.sqrt(1.0f - dy * dy)
        val phi = random.nextFloat() * Mth.TWO_PI
        val brightness = (210 + random.nextInt(46)) / 255.0f
        Star(
            Mth.cos(phi) * ring,
            dy,
            Mth.sin(phi) * ring,
            0.22f + random.nextInt(4) * 0.08f,
            brightness,
            brightness * 0.86f,
            brightness * 0.96f
        )
    }

    private class Star(
        val dx: Float,
        val dy: Float,
        val dz: Float,
        val size: Float,
        val r: Float,
        val g: Float,
        val b: Float,
    )

    override fun getBrightnessDependentFogColor(color: Vec3, brightness: Float): Vec3 {
        val darken = FOG_NIGHT_FLOOR + (1.0f - FOG_NIGHT_FLOOR) * Mth.clamp(lastDayFactor, 0.0f, 1.0f)
        return Vec3(
            (color.x * 0.94 + fogNormal.x * 0.06) * darken,
            (color.y * 0.94 + fogNormal.y * 0.06) * darken,
            (color.z * 0.94 + fogNormal.z * 0.06) * darken
        )
    }

    override fun isFoggyAt(x: Int, z: Int): Boolean = false

    override fun `candycraftce$renderClouds`(
        level: ClientLevel,
        ticks: Int,
        partialTick: Float,
        poseStack: PoseStack,
        camX: Double,
        camY: Double,
        camZ: Double,
        projectionMatrix: Matrix4f,
    ): Boolean = true // 无云

    @Suppress("DEPRECATION")
    override fun `candycraftce$renderSky`(
        level: ClientLevel,
        ticks: Int,
        partialTick: Float,
        poseStack: PoseStack,
        camera: Camera,
        projectionMatrix: Matrix4f,
        isFoggy: Boolean,
        setupFog: Runnable,
    ): Boolean {
        val pos = camera.blockPosition
        if (!level.hasChunkAt(pos)) {
            return false
        }

        val tickOfDay = tickOfDay(level, partialTick)
        val dayFactor = candySkyDayFactor(tickOfDay)
        lastDayFactor = dayFactor
        val nightFactor = 1.0f - dayFactor

        setupFog.run()

        val color = Vec3.fromRGB24(
            PuddingColor.getBlendedPuddingColor(level, pos, PuddingColor.radius)
        )
        val biomeSky = color// level.getSkyColor(camera.position, partialTick)
        val dayWeight = Mth.clamp((dayFactor - 0.35f) * 2.0f, 0.0f, 1.0f)
        val skyColorVec = skyTint.getColor(dayFactor).normal.lerp(biomeSky, dayWeight.toDouble())

        RenderSystem.depthMask(false)
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.disableCull()
        RenderSystem.setShader { GameRenderer.getPositionShader() }
        RenderSystem.setShaderColor(
            skyColorVec.x.toFloat(),
            skyColorVec.y.toFloat(),
            skyColorVec.z.toFloat(),
            1.0f
        )

        poseStack.use {
            drawSkyBox(poseStack.last().pose(), SKY_BOX_RADIUS)
        }

        //渲染星星
        if (nightFactor > 0.25f) {
            val starAlpha = Mth.clamp((nightFactor - 0.25f) / 0.75f, 0.0f, 1.0f)
            poseStack.use {
                //星空随时间绕 Y 轴缓慢自转，与原版 celestial angle 的行为保持一致
                poseStack.mulPose(Axis.YP.rotationDegrees(tickOfDay / DAY_LENGTH * 360.0f))
                drawCandyStars(poseStack.last().pose(), starAlpha)
            }
        }

        RenderSystem.enableCull()
        RenderSystem.disableBlend()
        RenderSystem.depthMask(true)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)

        return true
    }

    /**
     * 绘制球面分布的星星，每颗星都做正对相机的 billboard
     */
    private fun drawCandyStars(matrix: Matrix4f, alpha: Float) {
        RenderSystem.setShader { GameRenderer.getPositionColorShader() }
        //必须先复位：否则星点颜色会被上面残留的天空色（午夜接近全黑）乘到看不见
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        val tesselator = Tesselator.getInstance()
        val buffer = tesselator.builder
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)

        for (star in stars) {
            val nx = star.dx
            val ny = star.dy
            val nz = star.dz

            //t1 = normalize(n × up)，up 取 (0,1,0)；n 接近正上方时退化为 (0,0,1)
            var t1x = -nz
            var t1y = 0.0f
            var t1z = nx
            var len = Mth.sqrt(t1x * t1x + t1z * t1z)
            if (len < 1.0E-4f) {
                t1x = ny
                t1y = -nx
                t1z = 0.0f
                len = Mth.sqrt(t1x * t1x + t1y * t1y)
            }
            t1x /= len
            t1y /= len
            t1z /= len

            //t2 = n × t1（已为单位向量，且与 n、t1 正交）
            val t2x = ny * t1z - nz * t1y
            val t2y = nz * t1x - nx * t1z
            val t2z = nx * t1y - ny * t1x

            val px = nx * STAR_RADIUS
            val py = ny * STAR_RADIUS
            val pz = nz * STAR_RADIUS
            val s = star.size

            buffer.vertex(matrix, px - t1x * s - t2x * s, py - t1y * s - t2y * s, pz - t1z * s - t2z * s)
                .color(star.r, star.g, star.b, alpha).endVertex()
            buffer.vertex(matrix, px + t1x * s - t2x * s, py + t1y * s - t2y * s, pz + t1z * s - t2z * s)
                .color(star.r, star.g, star.b, alpha).endVertex()
            buffer.vertex(matrix, px + t1x * s + t2x * s, py + t1y * s + t2y * s, pz + t1z * s + t2z * s)
                .color(star.r, star.g, star.b, alpha).endVertex()
            buffer.vertex(matrix, px - t1x * s + t2x * s, py - t1y * s + t2y * s, pz - t1z * s + t2z * s)
                .color(star.r, star.g, star.b, alpha).endVertex()
        }

        tesselator.end()

    }

    /**
     * 当天内的 tick 数（含渲染插值），范围 [0, 24000)。
     * 语义固定：0=日出、6000=正午、12000=日落、18000=午夜（与 /time set 的取值一一对应）。
     */
    private fun tickOfDay(level: ClientLevel, partialTick: Float): Float {
        val t = (level.dayTime % 24000L + 24000L) % 24000L
        return (t + partialTick) % DAY_LENGTH
    }

    /**
     * 昼夜亮度因子：0 = 午夜，1 = 正午。
     *
     * 刻意不使用 getTimeOfDay / getCelestialAngle / getSunAngle：
     * 这些方法在不同映射与版本下零点分别是「日出」或「正午」，混进 cos 公式会整体偏移 6 小时；
     * 原版的 cos(x*2π)*2+0.5 还有大段 clamp 饱和区（近半天天空完全不变）。
     * 这里由 dayTime 直接推导，用连续正弦保证时间一变天空立刻跟着连续变化。
     */
    private fun candySkyDayFactor(tickOfDay: Float): Float {
        val angle = tickOfDay / DAY_LENGTH * Mth.TWO_PI
        return Mth.sin(angle) * 0.5f + 0.5f
    }
}