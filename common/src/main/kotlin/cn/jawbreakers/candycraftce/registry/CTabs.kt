package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatforms
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack

object CTabs {
    init {
        CLogUtils.sign()
    }

    private fun title(key: String): Component {
        return Component.translatable("itemGroup.$MOD_ID.$key")
    }

    val blocks: Entry<CreativeModeTab> = CPlatforms.registerCreativeTab("blocks") {
        it.title(title("blocks"))
            .icon(TODO())
            .displayItems { _, output ->
                entries[blocks]!!.forEach { stack -> output.accept(stack) }
            }
    }

    val toolsArmor: Entry<CreativeModeTab> = CPlatforms.registerCreativeTab("tools_armor") {
        it.title(title("tools_armor"))
            .icon(TODO())
            .displayItems { _, output ->
                entries[toolsArmor]!!.forEach { stack -> output.accept(stack) }
            }
    }

    val misc: Entry<CreativeModeTab> = CPlatforms.registerCreativeTab("misc") {
        it.title(title("misc"))
            .icon(TODO())
            .displayItems { _, output ->
                entries[misc]!!.forEach { stack -> output.accept(stack) }
            }
    }

    fun Entry<CreativeModeTab>.addItem(stack: ItemStack) {
        entries[this]?.add(stack) ?: throw IllegalArgumentException("Creative Mode Tab $this does not exist")
    }

    fun Entry<CreativeModeTab>.addItems(vararg stacks: ItemStack) {
        stacks.forEach { addItem(it) }
    }

    val entries = arrayOf(blocks, toolsArmor, misc)
        .associateWith { mutableListOf<ItemStack>() }
}