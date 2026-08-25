package cn.jawbreakers.candycraftce.item

import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class GummyBallItem(properties: Properties) : Item(properties) {
    //todo GummyBallEntity
    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack?> {
        val stack = player.getItemInHand(hand)
//        level.playSound(
//            null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
//            SoundSource.NEUTRAL, 0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f)
//        )
//
//        if (!level.isClientSide) {
//            val gummyBall: GummyBallEntity = GummyBallEntity(level, player, 0)
//            gummyBall.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f)
//            level.addFreshEntity(gummyBall)
//        }
//
//        if (!player.getAbilities().instabuild) {
//            stack.shrink(1)
//        }
//        player.awardStat(Stats.ITEM_USED.get(this))
        return InteractionResultHolder.sidedSuccess<ItemStack?>(stack, level.isClientSide)
    }
}
