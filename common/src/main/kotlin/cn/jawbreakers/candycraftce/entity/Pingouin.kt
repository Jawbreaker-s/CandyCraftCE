package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.utils.CUtils.defineId
import cn.jawbreakers.candycraftce.utils.CUtils.synched
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.SpawnGroupData
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.block.Block

class Pingouin(type: EntityType<out Pingouin>, level: Level) : Animal(type, level) {
    companion object {
        const val COLOR_VARIANTS = 5
        private val COLOR: EntityDataAccessor<Int> = defineId(EntityDataSerializers.INT)
        private val SUPER: EntityDataAccessor<Boolean> = defineId(EntityDataSerializers.BOOLEAN)

        fun createAttributes(): AttributeSupplier.Builder {
            return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
        }
    }

    var color by synched(COLOR).sanitize { it.coerceIn(0, COLOR_VARIANTS - 1) }

    //是否是首领（带披风）
    var isSuperPingouin by synched(SUPER)

    private val iceCreamDrop: Block
        get() = when (this.color) {
            0 -> CBlocks.strawberry_ice_cream.get()
            1 -> CBlocks.mint_ice_cream.get()
            2 -> CBlocks.blueberry_ice_cream.get()
            3 -> CBlocks.chocolate_ice_cream.get()
            4 -> CBlocks.banana_ice_cream.get()
            else -> CBlocks.ice_cream.get()
        }

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, BreedGoal(this, 0.5))
        goalSelector.addGoal(2, TemptGoal(this, 0.5, Ingredient.of(CItems.cranberry_fish.get()), false))
        goalSelector.addGoal(3, TemptGoal(this, 0.5, Ingredient.of(CItems.marshmallow_flower.get()), false))
        goalSelector.addGoal(5, FollowParentGoal(this, 0.28))
        goalSelector.addGoal(6, WaterAvoidingRandomStrollGoal(this, 0.5))
        goalSelector.addGoal(7, LookAtPlayerGoal(this, Player::class.java, 6.0f))
        goalSelector.addGoal(8, RandomLookAroundGoal(this))
    }

    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(COLOR, 0)
        entityData.define(SUPER, false)
    }

    override fun aiStep() {
        super.aiStep()
        if (!onGround() && deltaMovement.y < 0.0) {
            deltaMovement = deltaMovement.multiply(1.0, 0.6, 1.0)
        }
    }

    override fun causeFallDamage(distance: Float, damageMultiplier: Float, source: DamageSource): Boolean {
        return false
    }

    override fun isFood(stack: ItemStack): Boolean {
        return stack.`is`(CItems.cranberry_fish.get())
    }

    override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(hand)
        if (stack.`is`(CItems.marshmallow_flower.get())) {
            if (level().isClientSide) {
                return InteractionResult.SUCCESS
            }
            spawnAtLocation(ItemStack(this.iceCreamDrop, random.nextInt(6) + 5))
            if (!player.abilities.instabuild) {
                stack.shrink(1)
            }
            return InteractionResult.SUCCESS
        }
        return super.mobInteract(player, hand)
    }

    override fun finalizeSpawn(
        level: ServerLevelAccessor,
        difficulty: DifficultyInstance,
        spawnType: MobSpawnType,
        spawnGroupData: SpawnGroupData?,
        tag: CompoundTag?,
    ): SpawnGroupData {
        val data: SpawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag)!!
        this.color = random.nextInt(COLOR_VARIANTS)
        this.isSuperPingouin = random.nextInt(30) == 0
        return data
    }

    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        tag.putBoolean("Super", this.isSuperPingouin)
        tag.putInt("Color", this.color)
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        this.isSuperPingouin = tag.getBoolean("Super")
        this.color = tag.getInt("Color")
    }

    override fun getBreedOffspring(level: ServerLevel, partner: AgeableMob): AgeableMob? {
        return CEntityTypes.pingouin.get().create(level)?.also {
            it.color = if (random.nextBoolean() || partner !is Pingouin) this.color else partner.color
        }
    }

    override fun getAmbientSound() = null

    override fun getHurtSound(damageSource: DamageSource) = null

    override fun getDeathSound(): SoundEvent? = null
}
