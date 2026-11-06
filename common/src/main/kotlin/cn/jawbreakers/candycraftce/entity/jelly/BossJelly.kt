package cn.jawbreakers.candycraftce.entity.jelly

import cn.jawbreakers.candycraftce.entity.ICandyBoss
import cn.jawbreakers.candycraftce.entity.ICandyBoss.Companion.BOSS_FOLLOW_RANGE
import cn.jawbreakers.candycraftce.entity.ai.OnlyActiveLookControl
import cn.jawbreakers.candycraftce.utils.CLevelUtils.ifServer
import cn.jawbreakers.candycraftce.utils.CUtils.always
import cn.jawbreakers.candycraftce.utils.CUtils.defineId
import cn.jawbreakers.candycraftce.utils.CUtils.synched
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level


/**
 * Created in 2026/10/6 21:20 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
abstract class BossJelly(type: EntityType<out BossJelly>, level: Level) : BasicJelly(type, level), ICandyBoss {
    companion object {
        fun createBossJellyAttribute(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(FOLLOW_RANGE, BOSS_FOLLOW_RANGE)
        }

        val BOSS_AWAKE = defineId(EntityDataSerializers.BOOLEAN)
        val BOSS_FAINT_TICKS = defineId(EntityDataSerializers.INT)
    }

    override fun self() = this
    override var lostTargetTicks = 0
    override var awake: Boolean by synched(BOSS_AWAKE)
    override var faint: Int by synched(BOSS_FAINT_TICKS)

    init {
        lookControl = OnlyActiveLookControl(this)
    }

    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(BOSS_AWAKE, false)
        entityData.define(BOSS_FAINT_TICKS, 0)
    }

    override fun registerGoals() {
        super.registerGoals()
        goalSelector.removeAllGoals(::always)
        registerBossGoals(goalSelector)
        goalSelector.addGoal(1, SlimeAttackGoal(this))
        goalSelector.addGoal(2, BossAttackJumpingGoal())
    }

    override fun mayAttack(entity: Entity): Boolean {
        return super<BasicJelly>.mayAttack(entity)
    }


    override fun customServerAiStep() {
        bossServerAiStep()
    }

    override fun onServerSleepingTick() {
        //每秒恢复5点生命
        if (tickCount % 20 == 0) {
            heal(5f)
        }
    }

    override fun getJumpPower(): Float {
        return if (awake) 1.5f else 0f
    }

    override fun setTarget(target: LivingEntity?) {
        applyBossSource(this.target, target)
        super.setTarget(target)
    }


    override fun awaken(entity: Entity?, setTarget: Boolean) {
        //直接弹射起飞
        setDeltaMovement(deltaMovement.x, 2.0, deltaMovement.z)
        super.awaken(entity, setTarget)
    }

    override fun jumpFromGround() {
        if (isActive) super.jumpFromGround()
        postJumpFromGround()
    }

    open fun postJumpFromGround() {}

    override fun causeFallDamage(fallDistance: Float, multiplier: Float, source: DamageSource): Boolean = false
    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (source.`is`(DamageTypes.FALL)) return false
        if (amount > 1 && random.nextInt(10) == 0) {
            setFainting()//受伤有10%概率让Boss进入昏迷状态
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

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.putBoolean("Awake", awake)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        awake = compound.getBoolean("Awake")
    }

    override fun isPushable(): Boolean = false
    override fun isDealsDamage(): Boolean = isActive

    //不能溺水死
    override fun canBreatheUnderwater(): Boolean = true

    //和平不消失
    override fun shouldDespawnInPeaceful(): Boolean = false

    //远离玩家也不消失
    override fun removeWhenFarAway(distanceToClosestPlayer: Double) = false

    inner class BossAttackJumpingGoal : SlimeKeepOnJumpingGoal(this) {
        override fun canUse(): Boolean {
            return target != null && super.canUse()
        }
    }
}