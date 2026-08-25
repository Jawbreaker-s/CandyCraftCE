package cn.jawbreakers.candycraftce.item

import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemNameBlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block

class DragibusSeedFoodItem(crop: Block, properties: Properties) : ItemNameBlockItem(crop, properties) {
    override fun useOn(context: UseOnContext): InteractionResult {
        val placement = super.useOn(context)
        if (placement.consumesAction()) {
            return placement
        }

        val player = context.player ?: return placement

        player.startUsingItem(context.hand)
        return InteractionResult.CONSUME
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        player.startUsingItem(hand)
        return InteractionResultHolder.consume(stack)
    }

}
