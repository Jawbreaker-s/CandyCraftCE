package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.misc.EmblemHelper
import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.registry.CItems.dragibus
import cn.jawbreakers.candycraftce.registry.CItems.dragibus_stick
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.entity.ai.util.DefaultRandomPos
import net.minecraft.world.entity.animal.Pig
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import net.minecraft.world.phys.Vec3
import java.util.*


class CandyPigEntity(type: EntityType<out CandyPigEntity?>, level: Level) : Pig(type, level) {
    private var boostTime = 0
    private var boostTimeTotal = 0

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, PanicGoal(this, 1.25))
        goalSelector.addGoal(2, BreedGoal(this, 1.0))
        goalSelector.addGoal(
            3, BadgeTemptGoal(
                this, 1.2,
                Ingredient.of(dragibus_stick.get(), dragibus.get())
            )
        )
        goalSelector.addGoal(4, FollowParentGoal(this, 1.1))
        goalSelector.addGoal(5, BadgeAvoidGoal(this))
        goalSelector.addGoal(6, WaterAvoidingRandomStrollGoal(this, 1.0))
        goalSelector.addGoal(7, LookAtPlayerGoal(this, Player::class.java, 6.0f))
        goalSelector.addGoal(8, RandomLookAroundGoal(this))
    }

    override fun isFood(stack: ItemStack): Boolean = stack.`is`(dragibus.get())

    override fun getMaxSpawnClusterSize(): Int = 6

    override fun aiStep() {
        super.aiStep()
        if (boostTime > 0 && ++boostTime > boostTimeTotal) {
            boostTime = 0
            boostTimeTotal = 0
        }
        if (isSprinting && level().isClientSide && random.nextInt(3) == 0) {
            level().addParticle(
                ParticleTypes.CLOUD,
                x + (random.nextDouble() - 0.5) * bbWidth,
                y + 0.1,
                z + (random.nextDouble() - 0.5) * bbWidth,
                -deltaMovement.x * 0.5,
                0.02,
                -deltaMovement.z * 0.5
            )
        }
    }

    override fun getControllingPassenger(): LivingEntity? {
        val entity = super.getControllingPassenger()
        if (entity !is Player) return null
        return if (hasDragibusStick(entity)) entity else null
    }

    override fun getRiddenInput(player: Player, travelVector: Vec3): Vec3 {
        return if (hasDragibusStick(player)) super.getRiddenInput(player, travelVector) else Vec3.ZERO
    }

    override fun boost(): Boolean {
        if (boostTime > 0) {
            return false
        }
        boostTime = 1
        boostTimeTotal = random.nextInt(841) + 140
        return true
    }

    override fun getRiddenSpeed(player: Player): Float {
        var speed = super.getRiddenSpeed(player)
        if (boostTime > 0 && boostTimeTotal > 0) {
            speed += speed * 1.15f * Mth.sin(boostTime.toFloat() / boostTimeTotal.toFloat() * Math.PI.toFloat())
        }
        return speed
    }

    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        tag.putInt("BoostTime", boostTime)
        tag.putInt("BoostTimeTotal", boostTimeTotal)
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        boostTime = tag.getInt("BoostTime")
        boostTimeTotal = tag.getInt("BoostTimeTotal")
    }

    override fun dropCustomDeathLoot(source: DamageSource, looting: Int, recentlyHit: Boolean) {
        if (!isBaby) {
            val count = random.nextInt(3)
            if (count > 0) {
                spawnAtLocation(ItemStack(dragibus.get(), count))
            }
        }
    }

    override fun getDefaultLootTable(): ResourceLocation {
        return BuiltInLootTables.EMPTY
    }

    private fun shouldAvoidPlayer(player: Player): Boolean {
        return !player.isSpectator && EmblemHelper.shouldCandyMobAvoid(player)
    }

    private fun hasDragibusStick(player: Player): Boolean {
        return player.mainHandItem.`is`(dragibus_stick.get()) || player.offhandItem.`is`(dragibus_stick.get())
    }

    override fun getBreedOffspring(level: ServerLevel, partner: AgeableMob): Pig? {
        return CEntityTypes.candy_pig.get().create(level)
    }

    private class BadgeTemptGoal(private val pig: CandyPigEntity, speedModifier: Double, items: Ingredient) : TemptGoal(
        pig, speedModifier, items, false
    ) {
        override fun canUse(): Boolean {
            if (!super.canUse()) {
                return false
            }
            if (pig.shouldAvoidPlayer(player!!)) {
                player = null
                return false
            }
            return true
        }
    }

    private class BadgeAvoidGoal(private val pig: CandyPigEntity) : Goal() {
        private var threat: Player? = null
        private var wanted: Vec3? = null

        init {
            setFlags(EnumSet.of(Flag.MOVE))
        }

        override fun canUse(): Boolean {
            threat = pig.level().getNearestPlayer(pig, AVOID_DISTANCE)
            if (threat == null || !pig.shouldAvoidPlayer(threat!!)) {
                return false
            }
            wanted = DefaultRandomPos.getPosAway(pig, 16, 7, threat!!.position())
            if (wanted == null) {
                val away = pig.position().subtract(threat!!.position()).normalize().scale(6.0)
                wanted = pig.position().add(away)
            }
            return wanted != null
        }

        override fun canContinueToUse(): Boolean {
            return threat != null
                    && pig.shouldAvoidPlayer(threat!!)
                    && pig.distanceToSqr(threat!!) < AVOID_DISTANCE * AVOID_DISTANCE * 1.8
                    && !pig.getNavigation().isDone
        }

        override fun start() {
            pig.setSprinting(true)
            pig.getNavigation().moveTo(wanted!!.x, wanted!!.y, wanted!!.z, 1.15)
        }

        override fun tick() {
            if (threat != null && pig.tickCount % 10 == 0) {
                val next = DefaultRandomPos.getPosAway(pig, 16, 7, threat!!.position())
                if (next != null) {
                    wanted = next
                    pig.getNavigation().moveTo(wanted!!.x, wanted!!.y, wanted!!.z, 1.15)
                }
            }
        }

        override fun stop() {
            threat = null
            wanted = null
            pig.setSprinting(false)
        }
    }

    companion object {
        private const val AVOID_DISTANCE = 7.0
    }
}

