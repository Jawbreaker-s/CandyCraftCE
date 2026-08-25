package cn.jawbreakers.candycraftce.item

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack


class LollipopItem(properties: Properties) : Item(properties) {
    override fun interactLivingEntity(
        stack: ItemStack,
        player: Player,
        target: net.minecraft.world.entity.LivingEntity,
        hand: net.minecraft.world.InteractionHand,
    ): net.minecraft.world.InteractionResult {
        //TODO 需要实现CandyWolfEntity CandyCreeperEntity
//        if (target is CandyWolfEntity && target.health < target.maxHealth) {
//            if (!player.level().isClientSide) {
//                target.heal(WOLF_HEAL_AMOUNT)
//                consumeOne(stack, player)
//                playUseEffects(player, target)
//                if (player is net.minecraft.server.level.ServerPlayer) {
//                    CCCriteriaTriggers.HEAL_CANDY_WOLF.trigger(player)
//                }
//            }
//            return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide)
//        }
//
//        if (target is CandyCreeperEntity) {
//            if (!player.level().isClientSide) {
//                target.stallWithLollipop(CREEPER_STALL_TICKS)
//                consumeOne(stack, player)
//                playUseEffects(player, target)
//                if (player is net.minecraft.server.level.ServerPlayer) {
//                    CCCriteriaTriggers.STALL_CANDY_CREEPER.trigger(player)
//                }
//            }
//            return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide)
//        }

        return super.interactLivingEntity(stack, player, target, hand)
    }

    companion object {
        private const val WOLF_HEAL_AMOUNT = 8.0f
        private const val CREEPER_STALL_TICKS = 20 * 8

//        private fun consumeOne(stack: ItemStack, player: Player) {
//            if (!player.abilities.instabuild) {
//                stack.shrink(1)
//            }
//        }

//        private fun playUseEffects(
//            player: Player?,
//            target: net.minecraft.world.entity.LivingEntity,
//        ) {
//            target.level().playSound(
//                null,
//                target.blockPosition(),
//                net.minecraft.sounds.SoundEvents.GENERIC_EAT,
//                net.minecraft.sounds.SoundSource.NEUTRAL,
//                0.7f,
//                1.1f
//            )
//            if (target.level() is net.minecraft.server.level.ServerLevel) {
//                serverLevel.sendParticles<net.minecraft.core.particles.SimpleParticleType?>(
//                    net.minecraft.core.particles.ParticleTypes.HEART,
//                    target.x,
//                    target.y + target.bbHeight + 0.2,
//                    target.z,
//                    6,
//                    target.bbWidth * 0.35,
//                    0.25,
//                    target.bbWidth * 0.35,
//                    0.02
//                )
//            }
//        }
    }
}
