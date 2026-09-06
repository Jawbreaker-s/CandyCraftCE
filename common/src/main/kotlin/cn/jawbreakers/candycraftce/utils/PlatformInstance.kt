package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.registry.CBlocks.asItemEntry
import cn.jawbreakers.candycraftce.utils.registry.Accessor
import cn.jawbreakers.candycraftce.utils.registry.Entry
import com.mojang.datafixers.types.Type
import net.minecraft.client.color.block.BlockColor
import net.minecraft.client.color.item.ItemColor
import net.minecraft.client.renderer.DimensionSpecialEffects
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import java.util.function.Consumer
import java.util.function.Supplier

object CPlatformUtils : PlatformInstance by CandyCraftCE.platform {
    inline fun ifClient(action: () -> Unit) {
        if (isClient) action()
    }

    inline fun ifDev(action: () -> Unit) {
        if (isDev) action()
    }

    fun registerBlockColor(vararg blocks: Entry<out Block>, color: Int) =
        registerBlockColor(*blocks) { _, _, _, _ -> color }

    fun registerItemColor(vararg items: Entry<out Item>, color: Int) =
        registerItemColor(*items) { _, _ -> color }

    fun registerBlockAndItemColor(vararg entries: Entry<out Block>, color: Int) {
        registerBlockColor(*entries, color = color)
        registerItemColor(*entries.map { it.asItemEntry() }.toTypedArray(), color = color)
    }
}

interface PlatformInstance {

    val isDev: Boolean
    val isClient: Boolean

    //当所有对象注册完毕后
    fun <T> whenInitialized(action: () -> T): Accessor<T>
    fun <E : Item> registerItem(name: String, factory: Supplier<E>): Entry<E>
    fun <E : Block> registerBlock(name: String, factory: Supplier<E>): Entry<E>
    fun <E : BlockEntity> registerBlockEntity(
        key: String,
        builder: Supplier<BlockEntityType.Builder<E>>,
        dsl: Type<*>?,
    ): Entry<BlockEntityType<E>>

    //    fun <E> Registry<in E>.register(name: String, factory: Supplier<E>): Entry<E>
    fun registerDimensionSpecialEffects(id: ResourceLocation, effects: DimensionSpecialEffects)
    fun registerCreativeTab(name: String, builder: Consumer<CreativeModeTab.Builder>): Entry<CreativeModeTab>

    //client part
    fun setRenderLayer(block: Entry<out Block>, layer: RenderType)
    fun registerBlockColor(vararg blocks: Entry<out Block>, color: BlockColor)
    fun registerItemColor(vararg items: Entry<out Item>, color: ItemColor)

}
