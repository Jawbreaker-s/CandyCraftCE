package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.material.Fluid

/**
 * @see [cn.jawbreakers.candycraftce.forge.data.providers.tags]
 * */
object CItemTags {
    val chocolate_bar = bind("chocolate_bar")
    internal fun bind(name: String): TagKey<Item> {
        return TagKey.create(Registries.ITEM, name.modLoc())
    }
}

object CBlockTags {
    val ice_soil = bind("ice_soil")
    val candy_soil = bind("candy_soil")
    val seaweed_soil = bind("seaweed_soil")
    val marshmallow_planks = withItem("marshmallow_planks")
    val ice_cream = bind("ice_cream")
    val worm_blocks = bind("worm_blocks")
    val marshmallow_logs = bind("marshmallow_logs")
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

object CBiomeTags {
    val candy_biomes = bind("candy_biomes")
    val is_cold = bind("is_cold")
    val has_essence_flower = bind("has_essence_flower")
    val has_mint_flower = bind("has_mint_flower")

    //
    val has_floating_island = bind("has_floating_island")
    private fun bind(name: String): TagKey<Biome> {
        return TagKey.create(Registries.BIOME, name.modLoc())
    }
}

data class ItemBlockTagkey(
    val item: TagKey<Item>,
    val block: TagKey<Block>,
)

