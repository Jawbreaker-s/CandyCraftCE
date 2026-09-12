package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.material.Fluid

object CItemTags {
}

object CBlockTags {
    val candy_soil = bind("candy_soil")
}

object CFluidTags {
    val caramel = bind("caramel")
    val grenadine = bind("grenadine")
    val liquid_chocolate = bind("liquid_chocolate")
    val liquid_candy = bind("liquid_candy")
}

private fun CItemTags.bind(name: String): TagKey<Item> {
    return TagKey.create(Registries.ITEM, name.modLoc())
}

private fun CBlockTags.bind(name: String): TagKey<Block> {
    return TagKey.create(Registries.BLOCK, name.modLoc())
}

private fun CFluidTags.bind(name: String): TagKey<Fluid> {
    return TagKey.create(Registries.FLUID, name.modLoc())
}
