package cn.jawbreakers.candycraftce.item

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class EmblemItem(properties: Properties) : Item(properties) {
    companion object {
        val emblemTooltip: Component = Component.translatable("tooltip.$MOD_ID.emblem").withStyle(ChatFormatting.GREEN)
        val emblemCount: Int by lazy {
            BuiltInRegistries.ITEM.count { it is EmblemItem }
        }
    }

    val tooltipKey: Component by lazy {
        Component.translatable(
            "tooltip.$MOD_ID.${BuiltInRegistries.ITEM.getKey(this).path}"
        ).withStyle(ChatFormatting.AQUA)
    }

    override fun appendHoverText(stack: ItemStack, level: Level?, tooltip: MutableList<Component?>, flag: TooltipFlag) {
        tooltip.add(tooltipKey)
        tooltip.add(emblemTooltip)
    }

}
