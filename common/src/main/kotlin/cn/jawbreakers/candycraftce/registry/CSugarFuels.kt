package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.registry.CItems.asItem
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.whenInitialized
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

/**
 * Created in 2026/10/24 18:57 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
object CSugarFuels {
    private val registry by whenInitialized {
        Object2IntOpenHashMap<Item>().also {
            onFillRegistry(it)
        }
    }

    fun getBurnDuration(stack: ItemStack): Int {
        return registry.getInt(stack.item)
    }

    fun isFuel(stack: ItemStack): Boolean = getBurnDuration(stack) > 0
    fun listFuels() = registry.object2IntEntrySet().asSequence()
    private fun onFillRegistry(registry: Object2IntOpenHashMap<Item>) {
        val baseSugar = 200
        registry.apply {
            addTo(Items.SUGAR, baseSugar)
            addTo(CBlocks.sugar_sand.asItem(), baseSugar * 4)
            addTo(CBlocks.sugar_block.asItem(), baseSugar * 8)
        }
    }
}