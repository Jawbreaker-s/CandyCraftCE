package cn.jawbreakers.candycraftce.item

import cn.jawbreakers.candycraftce.entity.gummy.AbstractGummyBall
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class GummyBallItem(properties: Properties, val gummy: (owner: Player, level: Level) -> AbstractGummyBall) :
    Item(properties) {
    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack?> {
        val stack = player.getItemInHand(hand)
        level.playSound(
            null, player.x, player.y, player.z, SoundEvents.ARROW_SHOOT,
            SoundSource.NEUTRAL, 0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f)
        )

        if (!level.isClientSide) {
            val gummy = gummy(player, level)
            gummy.shootFromRotation(player, player.xRot, player.yRot, 0.0f, gummy.normalVelocity, 1.0f)
            level.addFreshEntity(gummy)
        }

        if (!player.abilities.instabuild) {
            stack.shrink(1)
        }
        player.awardStat(Stats.ITEM_USED.get(this))
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
    }
}
