package cn.jawbreakers.candycraftce.client.level

import cn.jawbreakers.candycraftce.mixin_stub.ISpecialEffectsAddition
import cn.jawbreakers.candycraftce.utils.CUtils.use
import cn.jawbreakers.candycraftce.utils.ClientOnly
import cn.jawbreakers.candycraftce.utils.LinearGradient
import cn.jawbreakers.candycraftce.utils.LinearGradient.Companion.normal
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.*
import com.mojang.math.Axis
import net.minecraft.client.Camera
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.DimensionSpecialEffects
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.util.Mth
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import java.awt.Color
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min

@ClientOnly
object CandyEffects
    : DimensionSpecialEffects(192.0f, true, SkyType.NORMAL, false, false),
    ISpecialEffectsAddition {
    private val fog_color = Color(0xEEAABB)
    private val fog_normal = fog_color.normal
    private val sky_color = Color(0xFDD8D7)
    val sky = LinearGradient {
        Color(0x321326) % 0f
        sky_color % 1
    }

    override fun getBrightnessDependentFogColor(color: Vec3, brightness: Float): Vec3 {
        return Vec3(
            color.x * 0.94 + fog_normal.x * 0.06,
            color.y * 0.94 + fog_normal.y * 0.06,
            color.z * 0.94 + fog_normal.z * 0.06
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
    ): Boolean = true

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
        setupFog.run()
        val day = candySkyDayFactor(level, partialTick)
        val night = 1.0f - day
        val sky = sky.getColor(day)
        RenderSystem.depthMask(false)
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShader { GameRenderer.getPositionShader() }
        RenderSystem.setShaderColor(sky.normRed, sky.normGreen, sky.normBlue, 1.0f)

        poseStack.use {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f))
            val matrix = poseStack.last().pose()
            drawSkyQuad(matrix, 128.0f)
            if (night > 0.25f) {
                drawCandyStars(matrix, Mth.clamp((night - 0.25f) / 0.75f, 0.0f, 1.0f))
            }
        }

        RenderSystem.disableBlend()
        RenderSystem.depthMask(true)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        return true
    }

//    override fun `candycraftce$renderSnowAndRain`(
//        level: ClientLevel,
//        ticks: Int,
//        partialTick: Float,
//        lightTexture: LightTexture,
//        camX: Double,
//        camY: Double,
//        camZ: Double,
//    ): Boolean {
//        MilkRainRenderer.render(level, ticks, partialTick, lightTexture, camX, camY, camZ)
//        return true
//    }
//
//    override fun `candycraftce$tickRain`(level: ClientLevel, ticks: Int, camera: Camera): Boolean {
//        MilkRainRenderer.tick(level, ticks, camera)
//        return true
//    }

    private fun drawSkyQuad(matrix: Matrix4f, radius: Float) {
        val tesselator = Tesselator.getInstance()
        val buffer: BufferBuilder = tesselator.builder
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION)
        buffer.vertex(matrix, -radius, -radius, -radius).endVertex()
        buffer.vertex(matrix, -radius, -radius, radius).endVertex()
        buffer.vertex(matrix, radius, -radius, radius).endVertex()
        buffer.vertex(matrix, radius, -radius, -radius).endVertex()
        tesselator.end()
    }

    private fun drawCandyStars(matrix: Matrix4f, alpha: Float) {
        RenderSystem.setShader { GameRenderer.getPositionColorShader() }
        val tesselator = Tesselator.getInstance()
        val buffer: BufferBuilder = tesselator.builder
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
        for (i in 0..359) {
            val hash = i * 1103515245 + 12345
            val x = (((hash ushr 8) and 1023) / 1023.0f - 0.5f) * 240.0f
            val z = (((hash ushr 20) and 1023) / 1023.0f - 0.5f) * 240.0f
            if (x * x + z * z < 900.0f) {
                continue
            }
            val y = -122.0f + ((hash ushr 4) and 15) * 0.12f
            val size = 0.22f + ((hash ushr 16) and 3) * 0.08f
            val brightness = 210 + ((hash ushr 12) and 45)
            val red = brightness / 255.0f
            val green = (brightness * 0.86f) / 255.0f
            val blue = (brightness * 0.96f) / 255.0f
            buffer.vertex(matrix, x - size, y, z - size).color(red, green, blue, alpha).endVertex()
            buffer.vertex(matrix, x - size, y, z + size).color(red, green, blue, alpha).endVertex()
            buffer.vertex(matrix, x + size, y, z + size).color(red, green, blue, alpha).endVertex()
            buffer.vertex(matrix, x + size, y, z - size).color(red, green, blue, alpha).endVertex()
        }
        tesselator.end()
    }

    private fun candySkyDayFactor(level: Level, partialTick: Float): Float {
        val value = cos(level.getTimeOfDay(partialTick) * (Math.PI.toFloat() * 2.0f)) * 2.0f + 0.5f
        return max(0.0f, min(1.0f, value))
    }
}

