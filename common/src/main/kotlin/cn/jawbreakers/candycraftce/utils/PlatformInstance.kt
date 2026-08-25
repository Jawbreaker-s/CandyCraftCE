package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.utils.registry.Accessor
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import java.util.function.Consumer
import java.util.function.Supplier

object CPlatformUtils : PlatformInstance by CandyCraftCE.platform {
    inline fun ifDev(action: () -> Unit) {
        if (isDev) action()
    }

    inline fun ifClient(action: () -> Unit) {
        if (isClient) action()
    }

}

interface PlatformInstance {
    val isDev: Boolean
    val isClient: Boolean

    //当所有对象注册完毕后
    fun <T> whenInitialized(action: () -> T): Accessor<T>
    fun <I : Item> registerItem(name: String, item: Supplier<I>): Entry<I>

    fun registerCreativeTab(name: String, builder: Consumer<CreativeModeTab.Builder>): Entry<CreativeModeTab>
}