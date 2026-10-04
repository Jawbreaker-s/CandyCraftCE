package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.mixin.item.AxeItemAccessor
import cn.jawbreakers.candycraftce.mixin.item.ItemPropertiesAccessor
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.whenInitialized
import cn.jawbreakers.candycraftce.utils.CUtils.mcLoc
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

object CMixins {
    fun injectStrippable(): MutableMap<Block, Block> {
        val map = AxeItemAccessor.getStrippedBlocks()
        if (map is HashMap) {
            return map
        } else {
            val replaced = HashMap(map)
            AxeItemAccessor.setStrippedBlocks(replaced)
            return replaced
        }
    }

    @ClientOnly
    fun Entry<out Item>.buildProperties(action: ItemPropertiesBuilder.() -> Unit) {
        whenInitialized {
            action(ItemPropertiesBuilder(get()))
        }
    }

    fun List<Entry<out Item>>.buildProperties(action: ItemPropertiesBuilder.() -> Unit) {
        for (entry in this) {
            entry.buildProperties(action)
        }
    }
}

@ClientOnly
class ItemPropertiesBuilder internal constructor(private val item: Item) {
    infix fun String.by(function: ClampedItemPropertyFunction) {
        this.mcLoc() by function
    }

    infix fun ResourceLocation.by(function: ClampedItemPropertyFunction) {
        ItemPropertiesAccessor.callRegister(item, this, function)
    }
}