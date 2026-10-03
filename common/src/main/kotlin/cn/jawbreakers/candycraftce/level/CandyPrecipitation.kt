package cn.jawbreakers.candycraftce.level


import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.biome.Biome
import kotlin.jvm.optionals.getOrNull

object CandyPrecipitation {
    val milk_rain_biomes = listOf(
        CBiomes.chocolate_forest
    )
    val snow_biomes = listOf(
        CBiomes.ice_cream_plains,
        CBiomes.ice_cream_sky_mountains,
        CBiomes.white_chocolate_forest
    )

    fun getPrecipitation(level: LevelReader, pos: BlockPos): Biome.Precipitation =
        getPrecipitation(level.getBiome(pos).unwrapKey().getOrNull())

    fun getPrecipitation(biome: ResourceKey<Biome>?): Biome.Precipitation {
        return when (biome) {
            in milk_rain_biomes -> Biome.Precipitation.RAIN
            in snow_biomes -> Biome.Precipitation.SNOW
            else -> Biome.Precipitation.NONE
        }
    }

}
