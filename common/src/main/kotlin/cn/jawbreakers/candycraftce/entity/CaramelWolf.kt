package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.misc.CandyTargeting
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import cn.jawbreakers.candycraftce.utils.CLevelUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CLevelUtils.ifServer
import cn.jawbreakers.candycraftce.utils.CUtils.SEC2TICK
import cn.jawbreakers.candycraftce.utils.CUtils.defineId
import cn.jawbreakers.candycraftce.utils.CUtils.synched
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.TamableAnimal
import net.minecraft.world.entity.animal.Sheep
import net.minecraft.world.entity.animal.Wolf
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

class CaramelWolf(type: EntityType<out CaramelWolf?>, level: Level) : Wolf(type, level) {
    companion object {
        private val CARAMEL_TIME: EntityDataAccessor<Int> = defineId(EntityDataSerializers.INT)
        private const val CARAMEL_TIME_TAG = "CaramelTime"
        private const val IS_UNDER_CARAMEL = "UnderCaramel"
        private const val NORMAL_CARAMEL_TIME = 60 * 15 * SEC2TICK
        private const val LEAVES_TIME_SCALE = 0.2f
        private const val LEAVES_CARAMEL_TIME = (NORMAL_CARAMEL_TIME * LEAVES_TIME_SCALE).toInt()
        private const val LEAVES_CHECK_INTERVAL: Int = 20
    }

    var caramelTime by synched(CARAMEL_TIME)
    private var leavesCheckCooldown = 0
    var isUnderCaramelLeaves = false
        private set

    override fun registerGoals() {
        super.registerGoals()
    }

    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(CARAMEL_TIME, NORMAL_CARAMEL_TIME)
    }

    override fun isFood(stack: ItemStack): Boolean {
        return stack.`is`(CItems.lollipop.get()) || stack.`is`(CItems.candy_cane.get())
    }

    fun isTamingFood(stack: ItemStack): Boolean {
        return stack.`is`(CItems.candy_cane.get())
    }

    override fun aiStep() {
        super.aiStep()
        level().ifClient {
            if (isTame && caramelTime <= 0 && random.nextInt(30) == 0) {
                repeat(2) {
                    level().addParticle(
                        ParticleTypes.ENTITY_EFFECT,
                        getRandomX(0.5), randomY, getRandomZ(0.5),
                        0.8, 0.3, 0.0
                    )
                }
            }
        }
        level().ifServer {
            if (isTame && caramelTime > 0) {
                if (leavesCheckCooldown > 0) {
                    leavesCheckCooldown--
                } else {
                    val underCaramelPrev = isUnderCaramelLeaves
                    checkIsUnderCaramelLeaves()
                    leavesCheckCooldown = LEAVES_CHECK_INTERVAL
                    if (underCaramelPrev && !isUnderCaramelLeaves) {
                        caramelTime = (caramelTime / LEAVES_TIME_SCALE).toInt()
                    } else if (!underCaramelPrev && isUnderCaramelLeaves) {
                        caramelTime = (caramelTime * LEAVES_TIME_SCALE).toInt()
                    }
                }
            }
        }

    }

    private fun checkIsUnderCaramelLeaves() {
        val base = blockPosition()
        for (y in 1..10) {
            val block = level().getBlockState(base.above(y))
            if (block.`is`(CBlocks.caramel_leaves.get())) {
                isUnderCaramelLeaves = true
                return
            }
        }
        isUnderCaramelLeaves = false
    }

    fun resetCaramelTimer() {
        caramelTime = if (isUnderCaramelLeaves) LEAVES_CARAMEL_TIME else NORMAL_CARAMEL_TIME
    }

    override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(hand)
        val item = stack.item
        if (isTame) {
            //染色
            if (item is DyeItem && item.dyeColor != collarColor) {
                collarColor = item.dyeColor
                if (!player.abilities.instabuild) {
                    stack.shrink(1)
                }
                return InteractionResult.sidedSuccess(level().isClientSide)
            }

            if (stack.`is`(Items.BUCKET) && caramelTime <= 0) {
                if (!level().isClientSide) {
                    val filled = CItems.caramel_bucket.defaultInstance
                    if (!player.abilities.instabuild) {
                        stack.shrink(1)
                        if (stack.isEmpty) {
                            player.setItemInHand(hand, filled)
                        } else if (!player.inventory.add(filled)) {
                            player.drop(filled, false)
                        }
                    } else if (!player.inventory.add(filled)) {
                        player.drop(filled, false)
                    }
                    resetCaramelTimer()
                }
                return InteractionResult.sidedSuccess(level().isClientSide)
            }
        } else if (isTamingFood(stack) && !isAngry) {
            if (!player.abilities.instabuild) {
                stack.shrink(1)
            }

            if (!level().isClientSide) {
                if (random.nextInt(3) == 0) {
                    tame(player)
                    isOrderedToSit = true
                    navigation.stop()
                    target = null
                    health = maxHealth
                    resetCaramelTimer()
//                    if (player is ServerPlayer) {
//                        CCCriteriaTriggers.TAME_CANDY_WOLF.trigger(player)
//                    }
                    level().broadcastEntityEvent(this, 7.toByte())
                } else {
                    level().broadcastEntityEvent(this, 6.toByte())
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide)
        }

        return super.mobInteract(player, hand)
    }

    override fun getBreedOffspring(level: ServerLevel, partner: AgeableMob): CaramelWolf? {
        val child: CaramelWolf? = CEntityTypes.caramel_wolf.get().create(level)
        if (child != null && partner is TamableAnimal && isTame && partner.isTame) {
            child.ownerUUID = ownerUUID
            child.setTame(true)
            child.checkIsUnderCaramelLeaves()
            child.resetCaramelTimer()
        }
        return child
    }

    override fun wantsToAttack(target: LivingEntity, owner: LivingEntity): Boolean {
        return !(isTame && target is Sheep)
                && CandyTargeting.canAttackEntity(target, this)
                && super.wantsToAttack(target, owner)
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        caramelTime = tag.getInt(CARAMEL_TIME_TAG)
        isUnderCaramelLeaves = tag.getBoolean(CARAMEL_TIME_TAG)
        setTame(isTame)
    }


    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        tag.putInt(CARAMEL_TIME_TAG, caramelTime)
        tag.putBoolean(IS_UNDER_CARAMEL, isUnderCaramelLeaves)
    }

}

