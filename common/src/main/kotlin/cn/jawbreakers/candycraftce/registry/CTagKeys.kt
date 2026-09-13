package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.material.Fluid

object CItemTags {
    val chocolate_bar = bind("chocolate_bar")
    internal fun bind(name: String): TagKey<Item> {
        return TagKey.create(Registries.ITEM, name.modLoc())
    }
}

object CBlockTags {
    val candy_soil = bind("candy_soil")
    val marshmallow_planks = withItem("marshmallow_planks")
    private fun bind(name: String): TagKey<Block> {
        return TagKey.create(Registries.BLOCK, name.modLoc())
    }

    private fun withItem(name: String): ItemBlockTagkey = ItemBlockTagkey(CItemTags.bind(name), bind(name))

}

object CFluidTags {
    val caramel = bind("caramel")
    val grenadine = bind("grenadine")
    val liquid_chocolate = bind("liquid_chocolate")
    val liquid_candy = bind("liquid_candy")
    private fun bind(name: String): TagKey<Fluid> {
        return TagKey.create(Registries.FLUID, name.modLoc())
    }

}

data class ItemBlockTagkey(
    val item: TagKey<Item>,
    val block: TagKey<Block>,
)

