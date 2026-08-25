package cn.jawbreakers.candycraftce.item

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level

class DynamiteItem(properties: Properties, private val glue: Boolean) : Item(properties) {
    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        player.startUsingItem(hand)
        return InteractionResultHolder.success(stack)
    }

    override fun onUseTick(level: Level, entity: LivingEntity, stack: ItemStack, remainingUseDuration: Int) {
        val usedTicks = getUseDuration(stack) - remainingUseDuration
        if (level.isClientSide && entity.getRandom().nextInt(10) == 0) {
            level.addParticle(
                ParticleTypes.SMOKE,
                entity.x + (entity.getRandom().nextDouble() - 0.5) * 0.4,
                entity.eyeY + 0.4,
                entity.z + (entity.getRandom().nextDouble() - 0.5) * 0.4,
                0.0, 0.1, 0.0
            )
        }

        if (usedTicks == HAND_EXPLOSION_TICKS && entity is Player) {
            if (!level.isClientSide) {
                level.explode(
                    null, entity.x, entity.y, entity.z, 3.0f,
                    Level.ExplosionInteraction.TNT
                )
                if (!entity.abilities.instabuild) {
                    stack.shrink(1)
                }
            }
            entity.stopUsingItem()
        }
    }

    override fun releaseUsing(stack: ItemStack, level: Level, entity: LivingEntity, timeLeft: Int) {
        if (entity !is Player) {
            return
        }

        val usedTicks = getUseDuration(stack) - timeLeft
        if (usedTicks !in MIN_THROW_TICKS..HAND_EXPLOSION_TICKS) {
            return
        }

        level.playSound(
            null, entity.x + 0.5, entity.y + 0.5, entity.z + 0.5,
            SoundEvents.CREEPER_PRIMED, SoundSource.PLAYERS, 0.5f,
            0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f)
        )

        if (!level.isClientSide) {
            //TODO DynamiteEntity
//            val dynamite: DynamiteEntity =
//                if (glue) GlueDynamiteEntity(level, entity) else DynamiteEntity(level, entity)
//            dynamite.setItem(stack.copyWithCount(1))
//            dynamite.setFuse(HAND_EXPLOSION_TICKS - usedTicks)
//            dynamite.shootFromRotation(entity, entity.xRot, entity.yRot, 0.0f, 1.5f, 1.0f)
//            level.addFreshEntity(dynamite)
        }

        if (!entity.abilities.instabuild) {
            stack.shrink(1)
        }
        entity.awardStat(Stats.ITEM_USED.get(this))
    }

    override fun getUseDuration(stack: ItemStack): Int {
        return 72000
    }

    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }

    companion object {
        private const val MIN_THROW_TICKS = 15
        private const val HAND_EXPLOSION_TICKS = 80
        const val MID_STAGE_TICKS: Int = 60
        fun modelStage(stack: ItemStack?, entity: LivingEntity?): Float {
            if (entity == null || entity.getUseItem() != stack) {
                return 0.0f
            }
            val usedTicks = stack.useDuration - entity.useItemRemainingTicks
            if (usedTicks >= MID_STAGE_TICKS) {
                return 2.0f
            }
            if (usedTicks > MIN_THROW_TICKS) {
                return 1.0f
            }
            return 0.0f
        }
    }
}
