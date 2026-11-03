package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.client.hud.IBossBar
import cn.jawbreakers.candycraftce.client.hud.TexturedBar
import cn.jawbreakers.candycraftce.misc.CandyTargeting
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.projectile.Projectile

/**
 * Created in 2026/10/6 21:19 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
interface ICandyBoss {
    companion object {
        const val BOSS_FOLLOW_RANGE = 32.0
        const val MAX_FAINT_TICKS = 1 * 20
        const val HALF_FAINT_TICKS = MAX_FAINT_TICKS / 2
        const val MAX_LOST_TARGET_TICKS = 5 * 20
    }

    fun self(): Mob
    val bossType: CandyBossType

    /*
    * 是否醒着
    * 默认应该为false
    * */
    var awake: Boolean

    /*
    *  boss昏迷tick
    * 范围：[0,MAX_FAINT_TICKS]
    * */
    var faint: Int
    fun setFainting(flag: Boolean = true) {
        faint = if (flag) MAX_FAINT_TICKS else 0
    }

    var lostTargetTicks: Int

    fun isActive() = faint == 0 && awake
    fun mayAttack(entity: Entity): Boolean {
        return CandyTargeting.canAttackEntity(entity, self())
    }


    fun awaken(entity: Entity?) {
        val source = if (entity is Projectile) entity.owner else entity
        if (source is LivingEntity && mayAttack(source)) {
            self().target = source
        }
        lostTargetTicks = 0
        awake = true
        faint = HALF_FAINT_TICKS //0.5f
    }
    //====================Ticking====================
    /**
     * 需要在customServerAiStep中调用这个方法
     * */
    fun bossServerAiStep() {
        if (faint > 0) faint--
        if (awake) onServerAwakeTick() else onServerSleepingTick()
    }

    fun onServerSleepingTick() {}

    fun onServerAwakeTick() {
        val target = self().target
        if (target == null || !mayAttack(target)) {
            lostTargetTicks++
            val remainLostTicks = MAX_LOST_TARGET_TICKS - lostTargetTicks
            //最后一段时间，让Boss逐渐进入昏迷状态
            if (remainLostTicks <= HALF_FAINT_TICKS) {
                faint = HALF_FAINT_TICKS - remainLostTicks
            }
            if (lostTargetTicks >= MAX_LOST_TARGET_TICKS) {
                awake = false
            }
        } else {
            lostTargetTicks = 0
        }
    }

    //====================Boss Bar====================
    fun isBarActive(): Boolean = self().isAlive
    fun getHealthBarProgress(): Float = self().health / self().maxHealth

    fun getBar(): IBossBar = TexturedBar.RED

    //0f全开，0.5f全闭，1f全开
    fun getFaintProgress(): Float = if (awake) faint.toFloat() / MAX_FAINT_TICKS else 0.5f
}


