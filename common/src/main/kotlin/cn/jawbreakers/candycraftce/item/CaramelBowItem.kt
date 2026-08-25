package cn.jawbreakers.candycraftce.item

import cn.jawbreakers.candycraftce.mixin_stub.BowItemAddition
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.AbstractArrow
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import java.util.function.Predicate
import kotlin.math.max

class CaramelBowItem(properties: Properties) : BowItem(properties), BowItemAddition {

    override fun getAllSupportedProjectiles() = HONEY_ARROWS
    override fun getSupportedHeldProjectiles() = HONEY_ARROWS

    override fun releaseUsing(stack: ItemStack, level: Level, entity: LivingEntity, timeLeft: Int) {
        val duration = getUseDuration(stack)

        val usedTicks = duration - timeLeft
        val fasterTimeLeft = duration - usedTicks * 2
        super.releaseUsing(stack, level, entity, max(0, fasterTimeLeft))
    }

    override fun `candycraftce$customArrow`(arrow: AbstractArrow): AbstractArrow {
        //        if (arrow is HoneyArrowEntity) {
//            return arrow
//        }
//        if (arrow.getOwner() is LivingEntity) {
//            return HoneyArrowEntity(arrow.level(), living)
//        }
//        return HoneyArrowEntity(arrow.level(), arrow.getX(), arrow.getY(), arrow.getZ())
        return arrow
    }

    companion object {
        private val HONEY_ARROWS: Predicate<ItemStack> = Predicate { false }
//            Predicate { stack: ItemStack -> stack.`is`(CItems.HONEY_ARROW.get()) }
    }
}
