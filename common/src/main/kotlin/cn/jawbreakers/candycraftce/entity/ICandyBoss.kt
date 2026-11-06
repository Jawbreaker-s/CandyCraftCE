package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.client.hud.IBossBar
import cn.jawbreakers.candycraftce.client.hud.TexturedBar
import cn.jawbreakers.candycraftce.entity.ai.BossSleepingGoal
import cn.jawbreakers.candycraftce.misc.CandyTargeting
import cn.jawbreakers.candycraftce.mixin_stub.ICandyBossTarget
import cn.jawbreakers.candycraftce.utils.ClientOnly
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.goal.GoalSelector
import net.minecraft.world.entity.projectile.Projectile

/**
 * Created in 2026/10/6 21:19 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 *
 * 子类实现需要实现数据字段，
 * 大部分数据字段可以直接使用简单字段即可
 * 其中[awake],[faint]需要同步
 * 强烈推荐将[awake]持久化
 *
 * 子类需要在某些时刻调用本接口的方法
 * [bossServerAiStep]、
 * [registerBossGoals]、
 * [applyBossSource]
 *
 * 建议实体重写的
 * [LivingEntity.canBreatheUnderwater]`=true`、
 * [Mob.shouldDespawnInPeaceful]`=false`、
 * [Mob.removeWhenFarAway]`=false`
 *
 */
interface ICandyBoss {
    companion object {
        const val BOSS_FOLLOW_RANGE = 32.0
        const val MAX_FAINT_TICKS = 1 * 20
        const val HALF_FAINT_TICKS = MAX_FAINT_TICKS / 2
        const val MAX_LOST_TARGET_TICKS = 5 * 20
    }
    //====================Implement to Use====================

    fun self(): Mob
    val bossType: CandyBossType

    /**
     * 是否醒着，需要用[net.minecraft.network.syncher.SynchedEntityData]同步
     * 默认：`false`
     * @sample cn.jawbreakers.candycraftce.entity.jelly.BossJelly.awake
     * @sample cn.jawbreakers.candycraftce.entity.jelly.BossJelly.defineSynchedData
     * */
    var awake: Boolean

    /**
     * boss昏迷tick
     * 范围：[`0`,[MAX_FAINT_TICKS]]
     * 默认：[HALF_FAINT_TICKS]
     * @sample cn.jawbreakers.candycraftce.entity.jelly.BossJelly.faint
     * @sample cn.jawbreakers.candycraftce.entity.jelly.BossJelly.defineSynchedData
     * */
    var faint: Int
    fun setFainting(flag: Boolean = true) {
        faint = if (flag) MAX_FAINT_TICKS else 0
    }

    /**
     * 简单Int var即可
     * */
    var lostTargetTicks: Int

    val isActive get() = faint == 0 && awake

    //====================Invoke to Use====================

    fun mayAttack(entity: Entity): Boolean {
        return CandyTargeting.canAttackEntity(entity, self())
    }

    /**
     * @sample cn.jawbreakers.candycraftce.entity.jelly.BossJelly.hurt
     * */
    fun awaken(entity: Entity?, setTarget: Boolean = true) {
        val source = if (entity is Projectile) entity.owner else entity
        if (setTarget && source is LivingEntity) {
            self().target = source
        }
        lostTargetTicks = 0
        awake = true
        faint = HALF_FAINT_TICKS //0.5f
    }

    /**
     * 需要在[Mob.setTarget]中调用这个方法
     * @sample cn.jawbreakers.candycraftce.entity.jelly.BossJelly.setTarget
     * */
    fun applyBossSource(prevTarget: LivingEntity?, target: LivingEntity?) {
        val level = self().level()
        if (!level.isClientSide) {
            if (prevTarget is ICandyBossTarget) {
                prevTarget.candycraftce_bossSource = null
            }
            if (target is ICandyBossTarget) {
                target.candycraftce_bossSource = self().uuid
            }
        }
    }

    /**
     * 需要在[Mob.registerGoals]中调用这个方法
     * @sample cn.jawbreakers.candycraftce.entity.jelly.BossJelly.registerGoals
     * */
    fun registerBossGoals(goalSelector: GoalSelector) {
        val self = self()
        self as ICandyBoss
        goalSelector.addGoal(-1, BossSleepingGoal(self))
    }

    /**
     * 需要在[Mob.customServerAiStep]中调用这个方法
     * @sample cn.jawbreakers.candycraftce.entity.jelly.BossJelly.customServerAiStep
     * */
    fun bossServerAiStep() {
        if (faint > 0) faint--
        if (awake) onServerAwakeTick() else onServerSleepingTick()
    }

    //====================Override to Use====================
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

    //====================Boss Bar Zone====================
    @ClientOnly
    fun isBarActive(): Boolean = self().isAlive

    @ClientOnly
    fun getHealthBarProgress(): Float = self().health / self().maxHealth

    @ClientOnly
    fun getBar(): IBossBar = TexturedBar.RED

    //0f全开，0.5f全闭，1f全开
    @ClientOnly
    fun getFaintProgress(): Float = if (awake) faint.toFloat() / MAX_FAINT_TICKS else 0.5f
}


