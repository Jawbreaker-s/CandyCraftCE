package cn.jawbreakers.candycraftce.client.hud

import cn.jawbreakers.candycraftce.client.hud.CandyBossBarHud.Companion.BAR_HEIGHT
import cn.jawbreakers.candycraftce.client.hud.CandyBossBarHud.Companion.BAR_WIDTH
import cn.jawbreakers.candycraftce.client.hud.CandyBossBarHud.Companion.BOSS_BAR_TEXTURE
import cn.jawbreakers.candycraftce.entity.ICandyBoss
import cn.jawbreakers.candycraftce.mixin_stub.ICandyBossTarget
import cn.jawbreakers.candycraftce.utils.CUtils.guiTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.CUtils.use
import cn.jawbreakers.candycraftce.utils.ClientOnly
import cn.jawbreakers.candycraftce.utils.LinearGradient.Companion.normalize
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.util.Mth.approach
import net.minecraft.util.Mth.lerp
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import org.joml.Vector3f
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Created in 2026/10/7 22:46 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@ClientOnly
class CandyBossBarHud(private val minecraft: Minecraft) {
    companion object {
        /** 发现半径：与 Boss 追踪范围一致，进入即显示 Boss 条 */
        const val SENSE_RANGE = ICandyBoss.BOSS_FOLLOW_RANGE

        /** 放弃半径：大于[SENSE_RANGE]，在边界来回移动时不会让 Boss 条一闪一闪 */
        const val RELEASE_RANGE = SENSE_RANGE * 1.5

        val BOSS_BAR_TEXTURE = "boss_bar".modLoc().guiTex()
        const val BAR_WIDTH = 256
        const val BAR_HEIGHT = 14

        //每 tick 向目标靠拢的比例
        const val STILL_LERP = 0.08f

        const val EYE_WIDTH = 64
        const val EYE_HEIGHT = 56
        const val EYE_OFFSET_V = 200

        //==================== 目标选择权重，只保证相对大小关系 ====================
        /** Boss 已把玩家锁定为攻击目标（最符合"我正在打的那只"直觉） */
        private const val W_LOCKED_ON = 800.0

        /** 短时间内伤害过玩家，且处于战斗状态（冲锋/远程 Boss 未必会 setTarget） */
        private const val W_HIT_PLAYER = 600.0

        /** 玩家短时间内攻击过它（原版 lastHurtMob 自带 100 tick 时效） */
        private const val W_HIT_BY_PLAYER = 500.0

        /** 准星正对着它，视为玩家的明确查看意图；仍要求其在检测半径内 */
        private const val W_CROSSHAIR = 400.0

        /** 醒着且未昏迷 */
        private const val W_ACTIVE = 200.0

        /** 已经在显示的目标，轻微加分 */
        private const val W_STICKY = 100.0

        /** 距离惩罚系数（按[SENSE_RANGE]归一化），量级小于以上任一档，只用于同档决胜 */
        private const val W_DISTANCE = 60.0

        /** 换目标所需的最小分值优势，防止多 Boss 势均力敌时逐 tick 反复横跳 */
        private const val SWITCH_MARGIN = 120.0

        private fun isActiveBoss(entity: Entity?) =
            entity is LivingEntity && entity is ICandyBoss && entity.isBarActive()
    }

    private var boss: LivingEntity? = null

    /** 单次 tick 内的候选目标及其得分 */
    private data class Choice(val entity: LivingEntity, val score: Double)

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
            val found = findBoss(player)
            //同一目标时 setBoss 内部直接返回，不会打断平滑动画；
            //也不再中途清空目标，避免切换过程中"满血闪一下"
            setBoss(found)
            val boss = found as? ICandyBoss ?: return
            //把当前值存为 prev，本 tick 结束后作为插值起点
            stillPrev = still
            health = boss.getHealthBarProgress()
            stillTarget = health
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
            val eyeOrd = mapTriangle(boss.getFaintProgress(), 3)
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
                boss.displayName,
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
        }
    }

    fun mapTriangle(input: Float, max: Int): Int {
        if (input <= 0f) return 0
        if (input >= 1f) return max
        val value = max - 2f * max * abs(input - 0.5f)
        return value.roundToInt().coerceIn(0, max)
    }

    /**
     * 选出当前最应该显示的那只 Boss。
     * 多 Boss 时按候选总分排序，并在新目标优势不足时保留当前目标，避免抖动。
     */
    fun findBoss(player: Player): LivingEntity? {
        val choices = collectChoices(player)
        if (choices.isEmpty()) return null
        val best = choices.maxByOrNull { it.score } ?: return null
        val current = choices.firstOrNull { it.entity === boss }
        if (current != null
            && best.entity !== current.entity
            && best.score - current.score < SWITCH_MARGIN
        ) {
            return current.entity
        }
        return best.entity
    }

    /** 一次性收集[RELEASE_RANGE]内的全部候选 Boss 并评分，取代原来的多次独立查询 */
    private fun collectChoices(player: Player): List<Choice> {
        val level = player.level()
        val area = player.boundingBox.inflate(RELEASE_RANGE)
        val bosses = level.getEntitiesOfClass(LivingEntity::class.java, area) { isActiveBoss(it) }
        if (bosses.isEmpty()) return emptyList()

        val lockedOn = (player as ICandyBossTarget).candycraftce_bossSource
        val crosshair = minecraft.crosshairPickEntity
        val hitByPlayer = player.lastHurtMob   // 我攻击过的对象
        val hitPlayer = player.lastHurtByMob   // 攻击我的对象
        val current = boss
        val eye = player.eyePosition

        return bosses.map { entity ->
            val iBoss = entity as ICandyBoss
            var score = 0.0
            if (entity.uuid == lockedOn) score += W_LOCKED_ON
            if (entity === hitPlayer && iBoss.isActive) score += W_HIT_PLAYER
            if (entity === hitByPlayer) score += W_HIT_BY_PLAYER
            if (entity === crosshair) score += W_CROSSHAIR
            if (iBoss.isActive) score += W_ACTIVE
            if (entity === current) score += W_STICKY
            //距离越远扣分越多，但量级小于任一权重档，只在同一优先级内决胜
            val distance = eye.distanceTo(entity.eyePosition) / SENSE_RANGE
            Choice(entity, score - W_DISTANCE * distance.coerceAtMost(1.5))
        }
    }

    private fun setBoss(newBoss: LivingEntity?) {
        if (this.boss === newBoss) return
        this.boss = newBoss
        val h = (newBoss as? ICandyBoss)?.getHealthBarProgress() ?: 1f
        health = h
        stillPrev = h
        still = h
        stillTarget = h
    }

}

interface IBossBar {
    fun drawBar(graphics: GuiGraphics, y: Int, progress: Float)
}

class ColorBar(val normal: Vector3f) : IBossBar {
    constructor(r: Float, g: Float, b: Float) : this(Vector3f(r, g, b))
    constructor(rgb: Int) : this(rgb.normalize())

    override fun drawBar(graphics: GuiGraphics, y: Int, progress: Float) {
        if (progress !in 0f..1f) return

        graphics.setColor(normal.x, normal.y, normal.z, 1f)
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        TexturedBar.GREY.drawBar(graphics, y, progress)
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
        val xo = (width - BAR_WIDTH) / 2
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