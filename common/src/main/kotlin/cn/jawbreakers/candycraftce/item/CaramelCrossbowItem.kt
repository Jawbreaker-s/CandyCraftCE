package cn.jawbreakers.candycraftce.item

import cn.jawbreakers.candycraftce.registry.CItems
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

class CaramelCrossbowItem(properties: Properties) : CrossbowItem(properties) {
    companion object {
        val honey_bolts: Predicate<ItemStack> = Predicate { it.`is`(CItems.honey_arrow.get()) }
    }

    override fun getAllSupportedProjectiles() = honey_bolts

    override fun getSupportedHeldProjectiles() = honey_bolts

    override fun getUseDuration(stack: ItemStack): Int {
        return (super.getUseDuration(stack) / 1.5).toInt()
    }
}
