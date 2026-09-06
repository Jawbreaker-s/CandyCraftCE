package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.registry.CItems.asItem
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import java.util.function.Supplier

object CTabs {
    init {
        CLogUtils.sign()
    }

    private fun title(key: String): Component {
        return Component.translatable("itemGroup.$MOD_ID.$key")
    }

    val blocks: Entry<CreativeModeTab> = CPlatformUtils.registerCreativeTab("blocks") {
        it.title(title("blocks"))
            .icon { CBlocks.custard_pudding_block.asItem().defaultInstance }
            .displayItems { _, output ->
                entries[blocks]!!.forEach { stack -> output.accept(stack.get()) }
            }
    }

    val toolsArmors: Entry<CreativeModeTab> = CPlatformUtils.registerCreativeTab("tools_armor") {
        it.title(title("tools_armors"))
            .icon { CItems.jelly_wand.defaultInstance }
            .displayItems { _, output ->
                entries[toolsArmors]!!.forEach { stack -> output.accept(stack.get()) }
            }
    }

    val misc: Entry<CreativeModeTab> = CPlatformUtils.registerCreativeTab("misc") {
        it.title(title("misc"))
            .icon { CItems.gummy_ball.defaultInstance }
            .displayItems { _, output ->
                entries[misc]!!.forEach { stack -> output.accept(stack.get()) }
            }
    }

    fun Entry<CreativeModeTab>.addItem(stack: Supplier<ItemStack>) {
        entries[this]?.add(stack) ?: throw IllegalArgumentException("Creative Mode Tab $this does not exist")
    }


    val entries = listOf(blocks, toolsArmors, misc)
        .associateWith { mutableListOf<Supplier<ItemStack>>() }
}