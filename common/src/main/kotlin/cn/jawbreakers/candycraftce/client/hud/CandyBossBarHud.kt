package cn.jawbreakers.candycraftce.client.hud

import cn.jawbreakers.candycraftce.client.hud.CandyBossBarHud.Companion.BAR_HEIGHT
import cn.jawbreakers.candycraftce.client.hud.CandyBossBarHud.Companion.BAR_WIDTH
import cn.jawbreakers.candycraftce.client.hud.CandyBossBarHud.Companion.BOSS_BAR_TEXTURE
import cn.jawbreakers.candycraftce.entity.ICandyBoss
import cn.jawbreakers.candycraftce.mixin.level.LevelMixinAccessor
import cn.jawbreakers.candycraftce.mixin_stub.ICandyBossTarget
import cn.jawbreakers.candycraftce.utils.CUtils.guiTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.CUtils.use
import cn.jawbreakers.candycraftce.utils.ClientOnly
import cn.jawbreakers.candycraftce.utils.LinearGradient.Companion.vecColorNormal
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.util.Mth.approach
import net.minecraft.util.Mth.lerp
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import net.minecraft.world.entity.player.Player

/**
 * Created in 2026/10/7 22:46 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@ClientOnly
open class CandyBossBarHud(private val minecraft: Minecraft) {
    companion object {
        const val SENSE_RANGE = 32.0
        val isCandyBoss: TargetingConditions = TargetingConditions
            .forCombat()
            .selector { it is ICandyBoss && it.isBarActive() }
        val BOSS_BAR_TEXTURE = "boss_bar".modLoc().guiTex()
        const val BAR_WIDTH = 256
        const val BAR_HEIGHT = 14

        //still 同步的时间间隔
        const val STILL_INTERVAL_TICKS = 40

        //每 tick 向目标靠拢的比例
        const val STILL_LERP = 0.08f

        const val EYE_WIDTH = 64
        const val EYE_HEIGHT = 56
        const val EYE_OFFSET_V = 200

    }

    private var boss: LivingEntity? = null

    // 上一 tick 与当前 tick 的显示值，render 里用 partialTick 插值
    private var health = 1f
    private var stillPrev = 1f
    private var still = 1f
    private var stillTarget = 1f

    //客户端 tick
    fun tick() {
        minecraft.profiler.use("tickCandyBossbar") {
            //没有玩家代表这个世界还没有进入
            val player = minecraft.player ?: run {
                setBoss(null)
                return
            }
            val boss = findBoss(player)
            if (boss !is ICandyBoss) {
                setBoss(null)
                return
            }
            setBoss(boss)
            //把当前值存为 prev，本 tick 结束后作为插值起点
            stillPrev = still
            health = boss.getHealthBarProgress()

            //受伤后一段时间内白条保持不动，之后才追向当前血量
            if (boss.tickCount - boss.lastHurtByMobTimestamp > STILL_INTERVAL_TICKS) {
                stillTarget = health
            }
            //平滑推进（tick 恒为 20Hz，与渲染帧率无关）
            still = approach(still, stillTarget, STILL_LERP)
        }
    }

    //渲染
    fun render(graphics: GuiGraphics, partialTick: Float, yOffset: Int = 0) {
        minecraft.profiler.use("renderCandyBossbar") {
            val boss = boss ?: return
            val bar = boss as? ICandyBoss ?: return
            val guiWidth = graphics.guiWidth()

            val eyeX = (guiWidth - EYE_WIDTH) / 2
            val eyeY = yOffset + 6
            val barY = eyeY + ((EYE_HEIGHT - BAR_HEIGHT) / 2)

            TexturedBar.FRAME.drawBar(graphics, barY, 1f)
            TexturedBar.STILL.drawBar(graphics, barY, lerp(partialTick, stillPrev, still))
            bar.getBar().drawBar(graphics, barY, health)
            //eye
            val eyeOrd = 0
            graphics.blit(
                BOSS_BAR_TEXTURE,
                eyeX, eyeY,
                EYE_WIDTH * eyeOrd, EYE_OFFSET_V,
                EYE_WIDTH, EYE_HEIGHT
            )
            val font = minecraft.font
            val textYo = 5
            graphics.drawCenteredString(
                font,
                boss.type.description,
                guiWidth / 2,
                eyeY + textYo - font.lineHeight / 2,
                0xFFFFFF
            )
            graphics.drawCenteredString(
                font,
                boss.bossType.description,
                guiWidth / 2,
                eyeY + EYE_HEIGHT - textYo - font.lineHeight / 2,
                0xFFFFFF
            )
            0
        }
    }

    fun findBoss(player: Player): LivingEntity? {
        val bossSource = (player as ICandyBossTarget).candycraftce_bossSource
        if (bossSource != null) {
            if (bossSource == boss?.uuid) return boss
            else {
                val source = (player.level() as LevelMixinAccessor).callGetEntities().get(bossSource)
                if (source is LivingEntity && source is ICandyBoss && source.isBarActive()) {
                    return source
                }
            }
        }
        if (player.lastHurtMob is ICandyBoss) return player.lastHurtMob
        return player.level().getNearestEntity(
            LivingEntity::class.java,
            isCandyBoss,
            player,
            player.x,
            player.y,
            player.z,
            player.boundingBox.inflate(SENSE_RANGE)
        )
    }

    private fun <T> setBoss(newBoss: T?) where T : LivingEntity, T : ICandyBoss {
        if (this.boss === newBoss) return
        this.boss = newBoss

        val h = newBoss?.getHealthBarProgress() ?: 1f
        health = h
        stillPrev = 1f
        still = 1f
        stillTarget = 1f
    }

}

interface IBossBar {
    fun drawBar(graphics: GuiGraphics, y: Int, progress: Float)
}

class ColorBar(rgb: Int) : IBossBar {
    val normal = rgb.vecColorNormal
    override fun drawBar(graphics: GuiGraphics, y: Int, progress: Float) {
        if (progress !in 0f..1f) return

        graphics.setColor(normal.x, normal.y, normal.z, 1f)
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        TexturedBar.STILL.drawBar(graphics, y, progress)
        graphics.setColor(1f, 1f, 1f, 1f)
        RenderSystem.disableBlend()
    }
}

enum class TexturedBar(val offset: Int) : IBossBar {
    FRAME(0),
    STILL(1),
    RED(2),
    GREEN(3),
    BLUE(4),
    GREY(5);


    override fun drawBar(graphics: GuiGraphics, y: Int, progress: Float) {
        val width = graphics.guiWidth()
        val xo = (width - BAR_WIDTH) / 2;
        if (xo > 0) {
            graphics.blit(
                BOSS_BAR_TEXTURE,
                xo, y,
                0, offset * BAR_HEIGHT,
                (BAR_WIDTH * progress).toInt(), BAR_HEIGHT
            )
        }
    }

}