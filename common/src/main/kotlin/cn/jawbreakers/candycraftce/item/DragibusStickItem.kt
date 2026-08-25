package cn.jawbreakers.candycraftce.item

import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class DragibusStickItem(properties: Properties) :
    Item(properties) {
    companion object {
        private const val BOOST_DAMAGE = 7
    }

    //todo boost candy pig
    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
//        val vehicle = player.vehicle
//        if (vehicle is CandyPigEntity && stack.maxDamage - stack.damageValue >= BOOST_DAMAGE && vehicle.boost()) {
//            player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this))
//            if (!level.isClientSide) {
//                stack.hurtAndBreak(
//                    BOOST_DAMAGE,
//                    player
//                ) { player ->
//                    player!!.broadcastBreakEvent(hand)
//                }
//            }
//            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
//        }

        return InteractionResultHolder.pass(stack)
    }
}
