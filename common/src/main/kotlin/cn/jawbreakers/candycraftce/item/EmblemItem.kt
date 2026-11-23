package cn.jawbreakers.candycraftce.item

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.whenInitialized
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class EmblemItem(
    properties: Properties,
    val ordinal: Int,
) : Item(properties) {
    companion object {
        val ultimateEmblemTooltip: Component =
            Component.translatable("tooltip.$MOD_ID.emblem").withStyle(ChatFormatting.GREEN)
        val totalEmblemCount: Int by whenInitialized {
            BuiltInRegistries.ITEM.count { it is EmblemItem }
        }
        const val MAX_EMBLEM_COUNT = Long.SIZE_BITS//64
        private val by_ordinal = Array<EmblemItem?>(MAX_EMBLEM_COUNT) { null }
    }

    val id: String by lazy { BuiltInRegistries.ITEM.getKey(this).path }

    init {
        require(ordinal in by_ordinal.indices) { "emblem ordinal out of range [0,$MAX_EMBLEM_COUNT]" }
        require(by_ordinal[ordinal] == null) { "emblem ordinal $ordinal has been used" }
        by_ordinal[ordinal] = this
    }

    val tooltip: Component by lazy {
        Component.translatable("tooltip.$MOD_ID.$id")
            .withStyle(ChatFormatting.AQUA)
    }

    override fun appendHoverText(stack: ItemStack, level: Level?, tooltip: MutableList<Component?>, flag: TooltipFlag) {
        tooltip.add(this.tooltip)
        tooltip.add(ultimateEmblemTooltip)
    }
}
