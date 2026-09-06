package cn.jawbreakers.candycraftce.level

import cn.jawbreakers.candycraftce.registry.CBiomes
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.resources.RegistryOps
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeSource
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.Climate
import net.minecraft.world.level.dimension.LevelStem
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

class CandyBiomeSource(
    private val overworldSource: BiomeSource,
    val biomes: List<Holder<Biome>>,
) : BiomeSource() {
    companion object {
        val codec: Codec<CandyBiomeSource> = RecordCodecBuilder.create { instance ->
            instance.group(
                RegistryOps.retrieveElement(LevelStem.OVERWORLD),
                Biome.CODEC.listOf().fieldOf("biomes").forGetter { it.biomes }
            )
                .apply(instance) { overworld, biomes ->
                    CandyBiomeSource(overworld.value().generator.biomeSource, biomes)
                }
        }
    }

    val mappings = buildMap {
        fun transfer(source: List<ResourceKey<Biome>>, target: ResourceKey<Biome>) = source.forEach { put(it, target) }
        transfer(
            listOf(
                Biomes.DEEP_FROZEN_OCEAN,
                Biomes.DEEP_COLD_OCEAN,
                Biomes.DEEP_OCEAN,
                Biomes.DEEP_LUKEWARM_OCEAN,
                Biomes.WARM_OCEAN,
                Biomes.FROZEN_OCEAN,
                Biomes.COLD_OCEAN,
                Biomes.OCEAN,
                Biomes.LUKEWARM_OCEAN
            ),
            CBiomes.sugar_oceans
        )
    }

    val byPath = biomes.associateBy { it.unwrapKey().getOrNull() }

    init {
        val unknown = overworldSource.possibleBiomes()
            .map { it.unwrapKey().getOrNull() }
            .filter { it !in mappings }
            .toList()
        if (unknown.isNotEmpty()) clog.error("Unknown overworld biomes: $unknown")
    }

    override fun codec() = codec
    override fun collectPossibleBiomes(): Stream<Holder<Biome>> = biomes.stream()

    override fun getNoiseBiome(quartX: Int, quartY: Int, quartZ: Int, sampler: Climate.Sampler): Holder<Biome> {
        return mapOverworldBiomes(overworldSource.getNoiseBiome(quartX, quartY, quartZ, sampler))
    }

    /**
     * Biomes.DEEP_FROZEN_OCEAN
     * Biomes.DEEP_COLD_OCEAN
     * Biomes.DEEP_OCEAN
     * Biomes.DEEP_LUKEWARM_OCEAN
     * Biomes.WARM_OCEAN
     * Biomes.FROZEN_OCEAN
     * Biomes.COLD_OCEAN
     * Biomes.OCEAN
     * Biomes.LUKEWARM_OCEAN
     * 陆地生物群系
     * Biomes.SNOWY_PLAINS
     * Biomes.SNOWY_TAIGA
     * Biomes.TAIGA
     * Biomes.PLAINS
     * Biomes.FOREST
     * Biomes.OLD_GROWTH_SPRUCE_TAIGA
     * Biomes.FLOWER_FOREST
     * Biomes.BIRCH_FOREST
     * Biomes.DARK_FOREST
     * Biomes.SAVANNA
     * Biomes.JUNGLE
     * Biomes.DESERT
     * Biomes.ICE_SPIKES
     * Biomes.OLD_GROWTH_PINE_TAIGA
     * Biomes.SUNFLOWER_PLAINS
     * Biomes.OLD_GROWTH_BIRCH_FOREST
     * Biomes.SPARSE_JUNGLE
     * Biomes.BAMBOO_JUNGLE
     * 高原/山地相关
     * Biomes.MEADOW
     * Biomes.SAVANNA_PLATEAU
     * Biomes.BADLANDS
     * Biomes.WOODED_BADLANDS
     * Biomes.CHERRY_GROVE
     * Biomes.ERODED_BADLANDS
     * 破碎地形相关
     * Biomes.WINDSWEPT_GRAVELLY_HILLS
     * Biomes.WINDSWEPT_HILLS
     * Biomes.WINDSWEPT_FOREST
     * Biomes.WINDSWEPT_SAVANNA
     * 特殊地形
     * Biomes.MUSHROOM_FIELDS
     * Biomes.STONY_SHORE
     * Biomes.SWAMP
     * Biomes.MANGROVE_SWAMP
     * Biomes.FROZEN_RIVER
     * Biomes.RIVER
     * Biomes.SNOWY_BEACH
     * Biomes.BEACH
     * 山峰相关
     * Biomes.JAGGED_PEAKS
     * Biomes.FROZEN_PEAKS
     * Biomes.STONY_PEAKS
     * Biomes.SNOWY_SLOPES
     * Biomes.GROVE
     * 洞穴相关
     * Biomes.DRIPSTONE_CAVES
     * Biomes.LUSH_CAVES
     * Biomes.DEEP_DARK
     * 调试用
     * Biomes.SNOWY_TAIGA（调试模式中作为占位符）
     *
     * */
    fun mapOverworldBiomes(biome: Holder<Biome>): Holder<Biome> {
        return byPath[mappings[biome.unwrapKey().getOrNull()]!!]!!
    }


}