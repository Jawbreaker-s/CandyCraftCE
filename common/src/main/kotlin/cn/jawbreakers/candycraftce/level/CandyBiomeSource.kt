package cn.jawbreakers.candycraftce.level

import cn.jawbreakers.candycraftce.registry.CBiomeTags
import cn.jawbreakers.candycraftce.registry.CBiomes
import cn.jawbreakers.candycraftce.registry.CBiomes.chocolate_forest
import cn.jawbreakers.candycraftce.registry.CBiomes.sugar_river
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.registries.Registries
import net.minecraft.resources.RegistryOps
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeSource
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.Climate
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

class CandyBiomeSource(
    private val delegate: BiomeSource,
    private val biomes: HolderSet<Biome>,
    private val err: Holder<Biome>,
) : BiomeSource() {
    companion object {
        val codec: Codec<CandyBiomeSource> = RecordCodecBuilder.create { instance ->
            instance.group(
                CODEC.fieldOf("delegate").forGetter { it.delegate },
                RegistryOps.retrieveGetter(Registries.BIOME),
            )
                .apply(instance) { delegate, biomes ->
                    CandyBiomeSource(
                        delegate,
                        biomes.getOrThrow(CBiomeTags.candy_biomes),
                        biomes.getOrThrow(CBiomes.dungeon)
                    )
                }
        }
    }

    val mappings = buildMap {
        fun transfer(source: List<ResourceKey<Biome>>, target: ResourceKey<Biome>) =
            source.forEach { put(it, target) }

        // 海洋
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

        // 冰原 / 雪原
        transfer(
            listOf(
                Biomes.SNOWY_PLAINS,
                Biomes.ICE_SPIKES,
                Biomes.SNOWY_BEACH
            ),
            CBiomes.ice_cream_plains
        )

        // 冷森林 / 针叶林
        transfer(
            listOf(
                Biomes.SNOWY_TAIGA,
                Biomes.TAIGA,
                Biomes.OLD_GROWTH_SPRUCE_TAIGA,
                Biomes.OLD_GROWTH_PINE_TAIGA,
                Biomes.GROVE
            ),
            CBiomes.white_chocolate_forest
        )

        // 平原  / 海滩
        transfer(
            listOf(
                Biomes.PLAINS,
                Biomes.SAVANNA,
                Biomes.BEACH,
                Biomes.SUNFLOWER_PLAINS,
            ),
            CBiomes.pudding_plains
        )

        transfer(
            listOf(
                Biomes.FLOWER_FOREST,
                Biomes.BIRCH_FOREST,
            ),
            CBiomes.cotton_candy_plains
        )
        transfer(
            listOf(
                Biomes.SWAMP,//沼泽
                Biomes.MANGROVE_SWAMP,
                Biomes.MUSHROOM_FIELDS,//蘑菇
            ),
            CBiomes.gummy_swamp
        )

        // 森林
        transfer(
            listOf(
                Biomes.FOREST,
            ),
            CBiomes.sugar_forest
        )
        transfer(
            listOf(
                Biomes.DARK_FOREST,
                Biomes.OLD_GROWTH_BIRCH_FOREST
            ),
            chocolate_forest
        )

        // 丛林
        transfer(
            listOf(
                Biomes.JUNGLE,
                Biomes.BAMBOO_JUNGLE,
                Biomes.SPARSE_JUNGLE
            ),
            CBiomes.enchanted_forest
        )

        // 沙漠
        transfer(
            listOf(
                Biomes.DESERT
            ),
            CBiomes.caramel_forest
        )

        // 山地 / 高原 / 风袭 / 石岸
        transfer(
            listOf(
                Biomes.BADLANDS,
                Biomes.WOODED_BADLANDS,
                Biomes.ERODED_BADLANDS,
                Biomes.MEADOW,
                Biomes.SAVANNA_PLATEAU,
                Biomes.CHERRY_GROVE,
                Biomes.WINDSWEPT_GRAVELLY_HILLS,
                Biomes.WINDSWEPT_HILLS,
                Biomes.WINDSWEPT_FOREST,
                Biomes.WINDSWEPT_SAVANNA,
                Biomes.STONY_SHORE
            ),
            CBiomes.pudding_hill
        )

        // 极山峰峦：地形起伏最强的一档
        transfer(
            listOf(
                Biomes.JAGGED_PEAKS,
                Biomes.FROZEN_PEAKS,
                Biomes.STONY_PEAKS,
                Biomes.SNOWY_SLOPES
            ),
            CBiomes.ice_cream_sky_mountains
        )

        transfer(
            listOf(
                Biomes.RIVER,
                Biomes.FROZEN_RIVER,
            ),
            sugar_river
        )
        // 其他
        transfer(
            listOf(
                Biomes.DRIPSTONE_CAVES,
                Biomes.LUSH_CAVES,
                Biomes.DEEP_DARK
            ),
            CBiomes.pudding_plains
        )
    }

    val byPath by lazy {
        biomes.associateBy { it.unwrapKey().getOrNull() }.also {
            check(it.isNotEmpty()) { "Empty biomes" }
        }
    }

    init {
        val all = delegate.possibleBiomes()
            .mapNotNull { it.unwrapKey().getOrNull() }
            .toList()
        val unknown = all.stream()
            .filter { it !in mappings }
            .map { it.location() }
            .toList()
        val overuse = mappings.keys.stream()
            .filter { !all.contains(it) }
            .map { it.location() }
            .toList()
        if (unknown.isNotEmpty()) clog.error("Unknown overworld biomes: \n${unknown.joinToString("\n")}")
        if (overuse.isNotEmpty()) clog.warn("Overused overworld biomes: \n${overuse.joinToString("\n")}")
    }


    override fun codec() = codec
    override fun collectPossibleBiomes(): Stream<Holder<Biome>> = biomes.stream()

    override fun getNoiseBiome(quartX: Int, quartY: Int, quartZ: Int, sampler: Climate.Sampler): Holder<Biome> {
        return mapOverworldBiomes(getRawNoiseBiome(quartX, quartY, quartZ, sampler))
    }

    private val biomeCache: Cache<Long, Holder<Biome>> = CacheBuilder.newBuilder()
        .maximumSize(10240)
        .weakValues()
        .build()

    fun getRawNoiseBiome(
        quartX: Int,
        quartY: Int,
        quartZ: Int,
        sampler: Climate.Sampler,
        cached: Boolean = true,
    ): Holder<Biome> {

        return if (cached) {
            val key = BlockPos.asLong(quartX, quartY, quartZ)
            biomeCache.get(key) {
                delegate.getNoiseBiome(quartX, quartY, quartZ, sampler)
            }
        } else delegate.getNoiseBiome(quartX, quartY, quartZ, sampler)
    }

    fun mapOverworldBiomes(biome: Holder<Biome>): Holder<Biome> {
        val target = biome.unwrapKey().getOrNull()?.let { mappings[it] }
        return byPath[target] ?: err
    }


}