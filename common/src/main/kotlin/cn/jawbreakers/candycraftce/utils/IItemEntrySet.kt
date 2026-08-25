package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.world.item.Item

interface IItemEntrySet {
    fun getAllItems(): List<Entry<out Item>>
}