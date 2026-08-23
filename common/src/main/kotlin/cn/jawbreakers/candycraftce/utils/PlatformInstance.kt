package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import java.util.function.Consumer
import java.util.function.Supplier

object CPlatforms : PlatformInstance by CandyCraftCE.platform

interface PlatformInstance {
    fun <I : Item> registerItem(name: String, item: Supplier<I>): Entry<I>

    fun registerCreativeTab(name: String, builder: Consumer<CreativeModeTab.Builder>): Entry<CreativeModeTab>
}