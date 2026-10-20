package cn.jawbreakers.candycraftce.entity.jelly

import cn.jawbreakers.candycraftce.entity.ICandyBoss
import cn.jawbreakers.candycraftce.mixin_stub.ICandyBossTarget
import cn.jawbreakers.candycraftce.utils.CLevelUtils.ifServer
import cn.jawbreakers.candycraftce.utils.CUtils.always
import cn.jawbreakers.candycraftce.utils.CUtils.defineId
import cn.jawbreakers.candycraftce.utils.CUtils.synched
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.level.Level
import kotlin.math.min


/**
 * Created in 2026/10/6 21:20 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
abstract class BossJelly(type: EntityType<out BossJelly>, level: Level) : BasicJelly(type, level), ICandyBoss {
    companion object {
        fun createBossJellyAttribute(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(FOLLOW_RANGE, ICandyBoss.BOSS_FOLLOW_RANGE)
        }

        val BOSS_AWAKE = defineId(EntityDataSerializers.BOOLEAN)
        val BOSS_FAINT_TICKS = defineId(EntityDataSerializers.INT)
        private val FROZEN_MODIFIER: AttributeModifier = AttributeModifier(
            "Sleeping Modifier",
            0.0,
            AttributeModifier.Operation.MULTIPLY_TOTAL
        )
        const val MAX_FAINT_TICKS = 1 * 20
        const val HALF_FAINT_TICKS = MAX_FAINT_TICKS / 2
        const val MAX_LOST_TARGET_TICKS = 5 * 20
    }

    private var lostTargetTicks = 0
    var awake: Boolean by synched(BOSS_AWAKE)
    var faint: Int by synched(BOSS_FAINT_TICKS)
    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(BOSS_AWAKE, false)
        entityData.define(BOSS_FAINT_TICKS, 0)
    }

    private var dormantYRot: Float? = null
    private var dormantYHeadRot: Float? = null

    override fun registerGoals() {
        super.registerGoals()
        goalSelector.removeAllGoals(::always)
        goalSelector.addGoal(1, BossAttackGoal())
        goalSelector.addGoal(2, BossKeepJumpingGoal())
    }

    override fun customServerAiStep() {
        super.customServerAiStep()
        if (faint > 0) faint--
        if (awake) onServerAwakeTick() else onServerSleepingTick()

        val speed = getAttribute(Attributes.MOVEMENT_SPEED)
        if (!awake) {
            if (dormantYRot == null) dormantYRot = yRot else yRot = dormantYRot!!
            if (dormantYHeadRot == null) dormantYHeadRot = yHeadRot else yHeadRot = dormantYHeadRot!!
            target = null
            navigation.stop()
            jumping = false
            speed?.apply {
                if (!hasModifier(FROZEN_MODIFIER)) {
                    addTransientModifier(FROZEN_MODIFIER)
                }
            }
            stopMovement(true)
        } else {
            dormantYRot = null
            dormantYHeadRot = null
            speed?.apply {
                removeModifier(FROZEN_MODIFIER)
            }
        }
    }


    override fun isBarActive(): Boolean = isAlive
    override fun getHealthBarProgress(): Float {
        return health / maxHealth
    }

    open fun onServerAwakeTick() {
        //防止跳的太高导致脱战
        if (target.let { it == null || !mayAttack(it) }) {
            lostTargetTicks--
            val remainLostTicks = MAX_LOST_TARGET_TICKS - lostTargetTicks  //d越来越小
            //最后一段时间，让Boss逐渐进入昏迷状态
            if (remainLostTicks <= HALF_FAINT_TICKS) {
                faint = HALF_FAINT_TICKS - remainLostTicks
            }
            if (lostTargetTicks >= MAX_LOST_TARGET_TICKS) {
                awake = false
            }
        }
    }

    open fun onServerSleepingTick() {
        //每秒恢复5点生命
        if (!level().isClientSide && tickCount % 20 == 0) {
            heal(5f)
        }
    }

    override fun getJumpPower(): Float {
        return if (awake) 1.5f else 0f
    }

    override fun setTarget(target: LivingEntity?) {
        if (!level().isClientSide) {
            val targetPrev = this.target
            if (targetPrev is ICandyBossTarget) {
                targetPrev.candycraftce_bossSource = null
            }
            if (target is ICandyBossTarget) {
                target.candycraftce_bossSource = this.uuid
            }
        }
        super.setTarget(target)
    }


    open fun awaken(entity: Entity?) {
        setDeltaMovement(deltaMovement.x, 2.0, deltaMovement.z)
        val t = if (entity is Projectile) entity.owner else entity
        if (t is LivingEntity && mayAttack(t)) {
            target = t
        }
        lostTargetTicks = 0
        awake = true
        faint = HALF_FAINT_TICKS //0.5f
    }

    override fun jumpFromGround() {
        if (isActive()) super.jumpFromGround()
        postJumpFromGround()
    }

    open fun postJumpFromGround() {}

    override fun causeFallDamage(fallDistance: Float, multiplier: Float, source: DamageSource): Boolean = false
    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (source.`is`(DamageTypes.FALL)) return false
        if (amount > 1 && random.nextInt(10) == 0) {
            setFaint()//受伤有10%概率让Boss进入昏迷状态
        }
        val entity = source.entity
        level().ifServer {
            if (!awake) {
                awaken(entity)
                if (entity is Player && amount > 0 && !entity.isCreative) {
                    var dx = entity.position().x - position().x
                    var dz = entity.position().z - this.position().z
                    //击退玩家
                    while (dx * dx + dz * dz < 1.0E-4) {
                        dx = (random.nextDouble() - random.nextDouble()) * 0.01
                        dz = (random.nextDouble() - random.nextDouble()) * 0.01
                    }
                    entity.knockback(1.5, -dx, -dz)
                }
            }
        }
        return super.hurt(source, amount)
    }

    /**
     * @param clampY 是否限制坠落
     * */
    private fun stopMovement(clampY: Boolean) {
        val movement = deltaMovement
        val y = if (clampY) min(0.0, movement.y) else movement.y
        if (movement.x != 0.0 || movement.z != 0.0 || movement.y != y) {
            setDeltaMovement(0.0, y, 0.0)
        }
    }

    fun setFaint(flag: Boolean = true) {
        faint = if (flag) MAX_FAINT_TICKS else 0
    }

    override fun getFaintProgress(): Float {
        return if (awake) faint.toFloat() / MAX_FAINT_TICKS else 0.5f
    }

    //是否激活状态
    fun isActive() = faint == 0 && awake
    override fun isPushable(): Boolean = false

    override fun isDealsDamage(): Boolean = isActive()

    //不能溺水死
    override fun canBreatheUnderwater(): Boolean = true

    //和平不消失
    override fun shouldDespawnInPeaceful(): Boolean = false

    //远离玩家也不消失
    override fun removeWhenFarAway(distanceToClosestPlayer: Double) = false
    inner class BossAttackGoal : SlimeAttackGoal(this) {
        override fun canUse(): Boolean {
            return isActive() && super.canUse()
        }

        override fun canContinueToUse(): Boolean {
            return isActive() && super.canContinueToUse()
        }
    }

    inner class BossKeepJumpingGoal : SlimeKeepOnJumpingGoal(this) {
        override fun canUse(): Boolean {
            return isActive() && target != null && super.canUse()
        }

        override fun canContinueToUse(): Boolean {
            return isActive() && target != null && super.canContinueToUse()
        }
    }

}