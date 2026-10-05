package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.mixin.entity.EatBlockGoalAccessor
import cn.jawbreakers.candycraftce.mixin.entity.SheepAccessor
import cn.jawbreakers.candycraftce.registry.CBlockTags
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import cn.jawbreakers.candycraftce.registry.CItems
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.entity.animal.Sheep
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LevelEvent
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.pathfinder.BlockPathTypes
import kotlin.math.max

open class WaffleSheep(type: EntityType<out WaffleSheep?>, level: Level) : Sheep(type, level) {
    companion object {
        val food: Ingredient by lazy { Ingredient.of(CItems.candied_cherry.get()) }
        fun createAttributes(): AttributeSupplier.Builder {
            return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
        }
    }

    init {
        setPathfindingMalus(BlockPathTypes.WATER, -1.0f)
    }


    override fun registerGoals() {
        val eatBlockGoal = CandyEatBlockGoal()
        (this as SheepAccessor).eatBlockGoal = eatBlockGoal
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, PanicGoal(this, 1.25))
        goalSelector.addGoal(2, BreedGoal(this, 1.0))
        goalSelector.addGoal(3, TemptGoal(this, 1.1, food, false))
        goalSelector.addGoal(4, FollowParentGoal(this, 1.1))
        goalSelector.addGoal(5, eatBlockGoal)
        goalSelector.addGoal(6, WaterAvoidingRandomStrollGoal(this, 1.0))
        goalSelector.addGoal(7, LookAtPlayerGoal(this, Player::class.java, 6.0f))
        goalSelector.addGoal(8, RandomLookAroundGoal(this))
    }

    override fun getDefaultLootTable(): ResourceLocation = type.getDefaultLootTable()
    override fun isFood(stack: ItemStack): Boolean = food.test(stack)

    open fun willDropNugget(source: DamageSource): Boolean = random.nextInt(4) == 0

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (!level().isClientSide && source.entity != null && willDropNugget(source)) {
            spawnAtLocation(CItems.waffle_nugget.get())
        }
        return super.hurt(source, amount)
    }

    override fun getBreedOffspring(level: ServerLevel, partner: AgeableMob): WaffleSheep? {
        if (partner is WaffleSheep) {
            val type = if (random.nextBoolean()) type else partner.type
            return type.create(level) as WaffleSheep?
        }
        return null
    }


    fun mayAteInLevel(isBelow: Boolean, state: BlockState): BlockState? {
        return when {
            isBelow && state.`is`(CBlockTags.sweet_grass.block) -> Blocks.AIR.defaultBlockState()
            !isBelow && state.`is`(CBlocks.custard_pudding_block.get()) -> CBlocks.pudding_block.defaultBlockState()
            else -> null
        }
    }

    inner class CandyEatBlockGoal : EatBlockGoal(this) {
        override fun canUse(): Boolean {
            val mob = this@WaffleSheep
            return mob.random.nextInt(if (mob.isBaby) 50 else 1000) == 0
        }

        override fun tick() {
            val accessor = (this as EatBlockGoalAccessor)
            val mob = this@WaffleSheep
            val level = mob.level()

            accessor.eatAnimationTick = max(0, accessor.eatAnimationTick - 1)
            if (accessor.eatAnimationTick == adjustedTickDelay(4)) {
                val pos = mob.blockPosition()
                val state = level.getBlockState(pos)
                val newState = mayAteInLevel(false, state)
                if (newState != null) {
                    if (level.gameRules.getBoolean(GameRules.RULE_MOBGRIEFING)) {
                        level.levelEvent(
                            LevelEvent.PARTICLES_DESTROY_BLOCK,
                            pos,
                            Block.getId(state)
                        )
                        level.setBlockAndUpdate(pos, newState)
                    }
                    mob.ate()
                } else {
                    val belowPos = pos.below()
                    val below = level.getBlockState(belowPos)
                    val newBelow = mayAteInLevel(true, below)
                    if (newBelow != null) {
                        if (level.gameRules.getBoolean(GameRules.RULE_MOBGRIEFING)) {
                            level.levelEvent(
                                LevelEvent.PARTICLES_DESTROY_BLOCK,
                                belowPos,
                                Block.getId(below)
                            )
                            level.setBlockAndUpdate(belowPos, newBelow)
                        }

                        mob.ate()
                    }
                }
            }
        }
    }
}
