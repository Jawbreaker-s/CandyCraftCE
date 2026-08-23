package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatforms
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.world.item.Item
import java.util.function.Supplier


object CItems {
    init {
        CLogUtils.sign()
    }

    internal val items: MutableMap<String, Entry<out Item>> = mutableMapOf()


    private fun <I : Item> register(name: String, factory: Supplier<I>): Entry<I> {
        val item = CPlatforms.registerItem(name, factory)
        items[name] = item
        return item
    }
}