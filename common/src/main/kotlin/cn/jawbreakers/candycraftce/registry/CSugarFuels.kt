package cn.jawbreakers.candycraftce.registry

import net.minecraft.world.item.ItemStack

/**
 * Created in 2026/10/24 18:57 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
object CSugarFuels {
    fun getBurnDuration(stack: ItemStack): Int {
        return 0
    }

    fun isFuel(stack: ItemStack): Boolean = false
}