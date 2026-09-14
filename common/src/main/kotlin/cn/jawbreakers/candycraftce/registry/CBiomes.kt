package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.Biome

object CBiomes {
    val dungeon = bind("dungeon")

    @Deprecated("wont generate in candyland")
    val hard_candy_plains = bind("hard_candy_plains")

    //
    val caramel_forest = bind("caramel_forest")
    val chocolate_forest = bind("chocolate_forest")
    val cotton_candy_plains = bind("cotton_candy_plains")
    val gummy_swamp = bind("gummy_swamp")
    val ice_cream_plains = bind("ice_cream_plains")
    val ice_cream_sky_mountains = bind("ice_cream_sky_mountains")
    val sugar_cold_forest = bind("sugar_cold_forest")
    val sugar_enchanted_forest = bind("sugar_enchanted_forest")
    val sugar_forest = bind("sugar_forest")
    val sugar_hell_mountains = bind("sugar_hell_mountains")
    val sugar_mountains = bind("sugar_mountains")
    val sugar_oceans = bind("sugar_oceans")
    val sugar_plains = bind("sugar_plains")
    val sugar_river = bind("sugar_river")
    private fun bind(name: String): ResourceKey<Biome> {
        return ResourceKey.create(Registries.BIOME, name.modLoc())
    }
}