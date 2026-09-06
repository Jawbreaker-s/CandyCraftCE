package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

object CTagKeys {
    object Item {
    }

    object Block {
        val candy_soil = bind("candy_soil")
    }
}


private fun CTagKeys.Item.bind(name: String): TagKey<Item> {
    return TagKey.create(Registries.ITEM, name.modLoc())
}

private fun CTagKeys.Block.bind(name: String): TagKey<Block> {
    return TagKey.create(Registries.BLOCK, name.modLoc())
}