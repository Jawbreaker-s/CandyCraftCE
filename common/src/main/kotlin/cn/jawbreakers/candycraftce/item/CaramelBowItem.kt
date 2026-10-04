package cn.jawbreakers.candycraftce.item

import cn.jawbreakers.candycraftce.registry.CItems
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

class CaramelBowItem(properties: Properties) : BowItem(properties) {

    companion object {
        val honey_arrows: Predicate<ItemStack> = Predicate { it.`is`(CItems.honey_arrow.get()) }
    }

    override fun getAllSupportedProjectiles() = honey_arrows

    override fun getSupportedHeldProjectiles() = honey_arrows

    // 焦糖弓只需要一半的时间
    override fun getUseDuration(stack: ItemStack): Int {
        return super.getUseDuration(stack) / 2
    }
}
