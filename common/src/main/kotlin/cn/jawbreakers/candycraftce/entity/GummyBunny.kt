package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.utils.CUtils.defineId
import cn.jawbreakers.candycraftce.utils.CUtils.synched
import cn.jawbreakers.candycraftce.utils.LinearGradient.Companion.rgb
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.SpawnGroupData
import net.minecraft.world.entity.ai.attributes.AttributeInstance
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.block.state.BlockState
import kotlin.math.abs

class GummyBunny(type: EntityType<out GummyBunny>, level: Level) : Animal(type, level) {
    companion object {
        private val COLOR = defineId(EntityDataSerializers.INT)
        const val TAG_COLOR = "Color"

        fun createAttributes(): AttributeSupplier.Builder {
            return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.20000000298023224)
        }

        val food: Ingredient by lazy { Ingredient.of(CItems.licorice.get()) }

    }

    private var lastJumpYaw = 0f
    private var jumpDelay = 0
    private var jumpLocked = false
    private var legacyJumping = false

    var color: Int by synched(COLOR)

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, PanicGoal(this, 2.0))
        goalSelector.addGoal(2, BreedGoal(this, 1.0))
        goalSelector.addGoal(3, TemptGoal(this, 1.25, food, false))
        goalSelector.addGoal(4, FollowParentGoal(this, 1.25))
        goalSelector.addGoal(5, WaterAvoidingRandomStrollGoal(this, 1.0))
        goalSelector.addGoal(6, LookAtPlayerGoal(this, Player::class.java, 6.0f))
        goalSelector.addGoal(7, RandomLookAroundGoal(this))
    }

    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(COLOR, 0xffffff)
    }

    fun setColor(red: Int, green: Int, blue: Int) {
        color = rgb(red, green, blue)
    }

    fun randomizeColor() {
        setColor(random.nextInt(230) + 20, random.nextInt(230) + 20, random.nextInt(230) + 20)
    }

    override fun isFood(stack: ItemStack) = food.test(stack)
    override fun playStepSound(pos: BlockPos, state: BlockState) {
        playSound(SoundEvents.SLIME_JUMP_SMALL, 0.15f, 1.0f)
    }

    override fun causeFallDamage(distance: Float, damageMultiplier: Float, source: DamageSource) = false

    override fun aiStep() {
        if (!level().isClientSide && !isInWaterOrBubble) {
            val grounded = onGround()
            val movementSpeed = getAttribute(Attributes.MOVEMENT_SPEED)
            if (jumpDelay > 0 && grounded) {
                jumpDelay--
                if (speed != 0f) {
                    speed = 0f
                }
                stopHorizontalMovement()
            }
            if (jumpDelay <= 0) {
                setMovementSpeedIfChanged(movementSpeed, 0.2)
            }
            if (grounded) {
                jumpLocked = false
            }
            if (hasHorizontalMovement() && !jumpLocked && jumpDelay <= 0) {
                legacyJumping = true
                setJumping(true)
                jumpDelay = 10
            }
            if (legacyJumping) {
                setDeltaMovement(deltaMovement.x, if (isInLove) 0.45 else 0.55, deltaMovement.z)
                setJumping(false)
                legacyJumping = false
                jumpLocked = true
                playSound(SoundEvents.SLIME_JUMP_SMALL, 0.15f, 1.0f)
            }
            if (jumpLocked && !grounded) {
                yRot = lastJumpYaw
                if (!isInLove) {
                    setMovementSpeedIfChanged(movementSpeed, 0.4)
                }
            }
            lastJumpYaw = yRot
        }
        super.aiStep()
    }

    private fun stopHorizontalMovement() {
        val movement = deltaMovement
        if (movement.x != 0.0 || movement.z != 0.0) {
            setDeltaMovement(0.0, movement.y, 0.0)
        }
    }

    override fun getBreedOffspring(level: ServerLevel, partner: AgeableMob): GummyBunny? {
        val bunny: GummyBunny? = CEntityTypes.gummy_bunny.get().create(level)
        bunny?.randomizeColor()
        return bunny
    }

    override fun finalizeSpawn(
        level: ServerLevelAccessor,
        difficulty: DifficultyInstance,
        spawnType: MobSpawnType,
        spawnGroupData: SpawnGroupData?,
        tag: CompoundTag?,
    ): SpawnGroupData {
        val data: SpawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag)!!
        randomizeColor()
        return data
    }

    private fun hasHorizontalMovement(): Boolean {
        return abs(deltaMovement.x) > 0.003 || abs(deltaMovement.z) > 0.003
    }

    private fun setMovementSpeedIfChanged(speed: AttributeInstance?, value: Double) {
        if (speed != null && speed.baseValue != value) {
            speed.baseValue = value
        }
    }

    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        tag.putInt("Color", color)
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        if (tag.contains(TAG_COLOR, Tag.TAG_ANY_NUMERIC.toInt())) {
            color = tag.getInt(TAG_COLOR)
        }
    }
}
