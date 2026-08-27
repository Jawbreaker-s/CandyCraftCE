package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.utils.registry.Accessor
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import java.util.function.Consumer
import java.util.function.Supplier

object CPlatformUtils : PlatformInstance by CandyCraftCE.platform {
    inline fun ifClient(action: () -> Unit) {
        if (isClient) action()
    }

    inline fun ifDev(action: () -> Unit) {
        if (isDev) action()
    }
}

interface PlatformInstance {

    val isDev: Boolean
    val isClient: Boolean

    //当所有对象注册完毕后
    fun <T> whenInitialized(action: () -> T): Accessor<T>
    fun <E : Item> registerItem(name: String, factory: Supplier<E>): Entry<E>
    fun <E : Block> registerBlock(name: String, factory: Supplier<E>): Entry<E>
    fun setRenderLayer(block: Entry<out Block>, layer: RenderType)

    fun registerCreativeTab(name: String, builder: Consumer<CreativeModeTab.Builder>): Entry<CreativeModeTab>
}
