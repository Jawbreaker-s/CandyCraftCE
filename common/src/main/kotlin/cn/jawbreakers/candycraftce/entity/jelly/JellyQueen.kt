package cn.jawbreakers.candycraftce.entity.jelly

import cn.jawbreakers.candycraftce.client.hud.ColorBar
import cn.jawbreakers.candycraftce.client.hud.IBossBar
import cn.jawbreakers.candycraftce.client.hud.TexturedBar
import cn.jawbreakers.candycraftce.entity.CandyBossType
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import cn.jawbreakers.candycraftce.utils.CUtils.defineId
import cn.jawbreakers.candycraftce.utils.CUtils.synched
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.Level

/**
 * Created in 2026/10/6 21:18 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 * 休眠模式。
 * 粉色模式（生命 > 50%）
 * 蓝色模式（生命 <= 50%，跳得更快、砸地更远）。
 * 棕色模式（生命 <= 25%，落地双重爆炸）。
 * 血量=300
 * 伤害值：size/2
 */
class JellyQueen(type: EntityType<out JellyQueen>, level: Level) : BossJelly(type, level) {
    companion object {
        val STATE = defineId(EntityDataSerializers.INT)
        const val STATE_SLEEPING = 0
        const val NORMAL_PROGRESS = 0.5f
        const val STATE_NORMAL = 1
        const val ANGRY_PROGRESS = 0.25f
        const val STATE_ANGRY = 2
        const val STATE_FRENZIED = 3

        val particle by lazy {
            ItemParticleOption(
                ParticleTypes.ITEM,
                CItems.strawberry_queen_jelly_ball.defaultInstance
            )
        }
    }

    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(STATE, STATE_NORMAL)
    }

    var state: Int by synched(STATE).strict { it.coerceIn(STATE_SLEEPING..STATE_FRENZIED) }
        private set

    override fun onServerAwakeTick() {
        //更新状态
        val s = health / maxHealth
        state = when {
            s >= NORMAL_PROGRESS -> STATE_NORMAL
            s >= ANGRY_PROGRESS -> STATE_ANGRY
            else -> STATE_FRENZIED
        }
        super.onServerAwakeTick()
    }

    override fun onServerSleepingTick() {
        state = 0
        super.onServerSleepingTick()
    }

    override fun getJumpDelay(): Int {
        return when {
            state >= STATE_ANGRY -> random.nextInt(40) + 5
            else -> random.nextInt(10) + 5
        }
    }

    override fun jumpFromGround() {
        if (state == STATE_FRENZIED) {
            level().explode(this, x, y, z, 3.0f, Level.ExplosionInteraction.MOB)
            level().explode(this, x, y + 2, z, 3.0f, Level.ExplosionInteraction.MOB)
        }
        super.jumpFromGround()
    }

    override fun getParticleType(): ParticleOptions = particle
    override fun setSize(size: Int, resetHealth: Boolean) {
        super.setSize(size, resetHealth)
        getAttribute(Attributes.MAX_HEALTH)?.apply {
            baseValue = 300.0
        }
        getAttribute(Attributes.ATTACK_DAMAGE)?.apply {
            baseValue = size / 2.0
        }
        if (resetHealth) health = maxHealth
        xpReward = 500
    }

    override fun onFinalizeSpawn() {
        setSize(6, true)
    }


    private val bars by lazy { arrayOf(TexturedBar.GREY, ColorBar(0xFF99CC), ColorBar(0x3399FF), ColorBar(0xEBA699)) }
    override fun getBar(): IBossBar {
        return bars[state.coerceIn(bars.indices)]
    }

    override val bossType: CandyBossType = CandyBossType.Boss

}
