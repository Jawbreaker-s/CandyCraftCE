package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.misc.CandyTargeting
import cn.jawbreakers.candycraftce.registry.CMobEffects
import cn.jawbreakers.candycraftce.utils.CLevelUtils.ifServer
import cn.jawbreakers.candycraftce.utils.CUtils.SEC2TICK
import cn.jawbreakers.candycraftce.utils.CUtils.defineId
import cn.jawbreakers.candycraftce.utils.CUtils.instance
import cn.jawbreakers.candycraftce.utils.CUtils.synched
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.FluidTags
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.Difficulty
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.math.max
import kotlin.math.pow

class CaramelBee(type: EntityType<out CaramelBee?>, level: Level) : Monster(type, level) {
    companion object {
        private const val HONEY_GLUE_DURATION = 5 * SEC2TICK
        private const val MAX_ANGER_DURATION = 30 * SEC2TICK
        private const val SUGUARD_WITNESS_RANGE = 16.0

        private val ANGRY = defineId(EntityDataSerializers.BOOLEAN)

        private const val TAG_ANGRY = "Angry"
        private const val TAG_ALWAYS_HOSTILE = "AlwaysHostile"
        private const val TAG_ANGER_TARGET = "AngerTarget"
        private const val TAG_ANGER_TICKS = "AngerTicks"
        fun createAttributes(): AttributeSupplier.Builder {
            return createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 2.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
        }


        fun checkSpawnRules(
            type: EntityType<CaramelBee>,
            level: ServerLevelAccessor,
            reason: MobSpawnType,
            pos: BlockPos,
            random: RandomSource,
        ): Boolean {
            return level.levelData.difficulty != Difficulty.PEACEFUL
                    && level.getBlockState(pos).isAir
                    && !level.getFluidState(pos).`is`(FluidTags.WATER)
        }

        fun alertSuguardAttackWitnesses(level: ServerLevel, suguard: Entity, attacker: Player) {
            if (attacker.isAlive && !attacker.abilities.instabuild && !attacker.isSpectator) {
                return
            }
            val searchBounds = suguard.boundingBox.inflate(SUGUARD_WITNESS_RANGE)
            for (bee in level.getEntitiesOfClass(CaramelBee::class.java, searchBounds)) {
                if (!bee.alwaysHostile && bee.hasLineOfSight(suguard) && bee.hasLineOfSight(attacker)) {
                    bee.provoke(attacker)
                }
            }
        }
    }

    private var flightTarget: BlockPos? = null
    private var attackTick = 0
    private var alwaysHostile = false
    private var angerTarget: UUID? = null
    private var angerTicks = 0

    init {
        isNoGravity = true
    }

    // suguard在蜜蜂的背上，而不是在整个边界框的顶部。
    override fun getPassengersRidingOffset(): Double = super.getPassengersRidingOffset() - 0.3

    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(ANGRY, false)
    }

    override fun registerGoals() {
        targetSelector.addGoal(1, CaramelBeeHurtByTargetGoal(this).setAlertOthers())
        targetSelector.addGoal(2, AngryPlayerTargetGoal(this))
    }

    var isAngry: Boolean by synched(ANGRY)
    fun setAlwaysHostile(alwaysHostile: Boolean) {
        this.alwaysHostile = alwaysHostile
        if (alwaysHostile) {
            angerTarget = null
            angerTicks = 0
        }
        this.isAngry = alwaysHostile || angerTicks > 0 && angerTarget != null
    }

    fun provoke(player: Player) {
        if (alwaysHostile || !CandyTargeting.canAttackPlayer(player, this)) {
            return
        }
        angerTarget = player.getUUID()
        angerTicks = MAX_ANGER_DURATION
        this.isAngry = true
        target = player
    }

    override fun aiStep() {
        isNoGravity = true
        level().ifServer {
            tickAnger()
            if (target != null && !CandyTargeting.canAttackEntity(target!!, this)) target = null
            tickFlight()
        }
        super.aiStep()
    }

    private fun tickFlight() {
        var player: Player? = null
        if (isAngry) {
            val followRange = getAttributeValue(Attributes.FOLLOW_RANGE)
            val followRangeSqr = followRange * followRange
            val currentTarget = target
            if (currentTarget is Player && canTargetPlayer(currentTarget) && distanceToSqr(currentTarget) <= followRangeSqr) {
                player = currentTarget
            } else if (alwaysHostile) {
                player = level().getNearestPlayer(this, followRange)
            } else if (level() is ServerLevel && angerTarget != null) {
                val provoker = level().getPlayerByUUID(angerTarget!!)
                if (provoker != null && canTargetPlayer(provoker) && distanceToSqr(provoker) <= followRangeSqr) {
                    player = provoker
                }
            }
        }

        attackTick = max(attackTick - 1, 0)
        if (player != null) {
            target = player
        } else if (!this.isAngry) {
            target = null
        }

        if (player != null && canAttackPlayer(player)) {
            val reachSqr = (bbWidth * 2.0 * bbWidth * 2.0 + player.bbWidth).pow(2.0)
            if (distanceToSqr(player.x, player.y + 1.1, player.z) <= reachSqr && attackTick <= 0) {
                attackTick = 20
                doHurtTarget(player)
            }
        }
        var dx: Double
        var dy: Double
        var dz: Double
        if (this.isAngry && player != null) {
            this.flightTarget = player.blockPosition()
            dx = player.x - x
            dy = player.y + 1.1 - y
            dz = player.z - z
        } else {
            var flightTarget = flightTarget
            if (flightTarget == null
                || !level().isEmptyBlock(flightTarget)
                || flightTarget.y < level().minBuildHeight
                || random.nextInt(100) == 0
                || flightTarget.closerToCenterThan(position(), 2.0)
            ) {
                val pos = blockPosition()
                val randX = random.nextInt(27) - 13
                val randY = random.nextInt(27) - 13
                val randz = random.nextInt(6) - 2
                pos.offset(randX, randz, randY).also {
                    this.flightTarget = it
                    flightTarget = it
                }
            }
            dx = flightTarget!!.x + 0.5 - x
            dy = flightTarget.y + 0.1 - y
            dz = flightTarget.z + 0.5 - z
        }

        val toTarget = Vec3(dx, dy, dz)
        if (toTarget.lengthSqr() > 1.0E-4) {
            val direction = toTarget.normalize()
            val speed = if (this.isAngry && player != null) 0.30 else 0.20
            val desired = Vec3(
                direction.x * speed,
                Mth.clamp(direction.y * speed, -0.16, 0.20),
                direction.z * speed
            )
            val blend = if (this.isAngry && player != null) 0.12 else 0.08
            deltaMovement = deltaMovement.lerp(desired, blend)
        } else {
            deltaMovement = deltaMovement.scale(0.92)
        }

        val targetYaw = (Mth.atan2(deltaMovement.z, deltaMovement.x) * 180.0 / Math.PI).toFloat() - 90.0f
        yRot += Mth.wrapDegrees(targetYaw - yRot) * 0.22f
        yBodyRot = yRot
    }

    private fun canAttackPlayer(player: Player): Boolean {
        return this.isAngry && canTargetPlayer(player)
    }

    private fun canTargetPlayer(player: Player): Boolean {
        return CandyTargeting.canAttackPlayer(
            player,
            this
        ) && (alwaysHostile || angerTicks > 0 && angerTarget != null && angerTarget == player.getUUID())
    }

    private fun tickAnger() {
        if (alwaysHostile) {
            if (!this.isAngry) {
                this.isAngry = true
            }
            return
        }
        if (angerTicks > 0) {
            --angerTicks
        }
        if (angerTicks > 0 && angerTarget != null) {
            if (!this.isAngry) {
                this.isAngry = true
            }
            return
        }
        angerTicks = 0
        angerTarget = null
        this.isAngry = false
        if (target is Player) {
            target = null
        }
    }

    override fun doHurtTarget(target: Entity): Boolean {
        if (!CandyTargeting.canAttackEntity(target, this)) {
            setTarget(null)
            return false
        }
        val damage = if (level().difficulty == Difficulty.HARD) 3.0f else 2.0f
        val success = target.hurt(damageSources().mobAttack(this), damage)
        if (success && target is LivingEntity && random.nextBoolean()) {
            target.addEffect(CMobEffects.propolis.get().instance(HONEY_GLUE_DURATION), this)
        }
        return success
    }

    override fun causeFallDamage(distance: Float, damageMultiplier: Float, source: DamageSource): Boolean = false

    override fun checkFallDamage(y: Double, onGround: Boolean, state: BlockState, pos: BlockPos) {
    }

    override fun travel(travelVector: Vec3) {
        move(MoverType.SELF, deltaMovement)
    }

    override fun shouldDespawnInPeaceful(): Boolean {
        return true
    }

    override fun getHurtSound(source: DamageSource): SoundEvent = SoundEvents.BEE_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.BEE_DEATH

    override fun getAmbientSound(): SoundEvent? = null
    override fun getSoundVolume(): Float = 0.4f

    override fun finalizeSpawn(
        level: ServerLevelAccessor, difficulty: DifficultyInstance, reason: MobSpawnType,
        spawnData: SpawnGroupData?, tag: CompoundTag?,
    ): SpawnGroupData? {
        val data = super.finalizeSpawn(level, difficulty, reason, spawnData, tag)
        setAlwaysHostile(reason == MobSpawnType.SPAWNER)
        return data
    }

    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        tag.putBoolean(TAG_ANGRY, this.isAngry)
        tag.putBoolean(TAG_ALWAYS_HOSTILE, alwaysHostile)
        tag.putInt(TAG_ANGER_TICKS, angerTicks)
        if (angerTarget != null) {
            tag.putUUID(TAG_ANGER_TARGET, angerTarget!!)
        }
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        alwaysHostile = tag.getBoolean(TAG_ALWAYS_HOSTILE)
        angerTicks = tag.getInt(TAG_ANGER_TICKS)
        angerTarget = if (tag.hasUUID(TAG_ANGER_TARGET)) tag.getUUID(TAG_ANGER_TARGET) else null
        isAngry = alwaysHostile || (angerTicks > 0 && angerTarget != null)
    }

    private class AngryPlayerTargetGoal(private val bee: CaramelBee) : NearestAttackableTargetGoal<Player?>(
        bee, Player::class.java, 10, true, false,
        { it is Player && bee.canTargetPlayer(it) }) {
        override fun canUse(): Boolean {
            return bee.isAngry && super.canUse()
        }

        override fun canContinueToUse(): Boolean {
            return bee.isAngry && super.canContinueToUse()
        }
    }

    private class CaramelBeeHurtByTargetGoal(private val bee: CaramelBee) : HurtByTargetGoal(bee) {
        override fun start() {
            val attacker = bee.lastHurtByMob
            if (attacker is Player) {
                bee.provoke(attacker)
            }
            super.start()
        }

        override fun alertOther(mob: Mob, target: LivingEntity) {
            if (mob is CaramelBee && target is Player) {
                mob.provoke(target)
                return
            }
            super.alertOther(mob, target)
        }
    }
}
