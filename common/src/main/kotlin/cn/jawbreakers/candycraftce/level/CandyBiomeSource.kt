package cn.jawbreakers.candycraftce.level

import cn.jawbreakers.candycraftce.registry.CBiomes
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeSource
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.Climate
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

class CandyBiomeSource(
    private val delegate: BiomeSource,
    val biomes: List<Holder<Biome>>,
) : BiomeSource() {
    companion object {
        val codec: Codec<CandyBiomeSource> = RecordCodecBuilder.create { instance ->
            instance.group(
                CODEC.fieldOf("delegate").forGetter { it.delegate },
                Biome.CODEC.listOf().fieldOf("biomes").forGetter { it.biomes }
            )
                .apply(instance, ::CandyBiomeSource)
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
            CBiomes.sugar_cold_forest
        )

        // 平原 / 沼泽 / 河流 / 海滩 / 蘑菇 / 热带草原
        transfer(
            listOf(
                Biomes.PLAINS,
                Biomes.SUNFLOWER_PLAINS,
                Biomes.SWAMP,
                Biomes.MANGROVE_SWAMP,
                Biomes.MUSHROOM_FIELDS,
                Biomes.BEACH,
                Biomes.RIVER,
                Biomes.FROZEN_RIVER,
                Biomes.SAVANNA
            ),
            CBiomes.sugar_plains
        )

        // 森林
        transfer(
            listOf(
                Biomes.FOREST,
                Biomes.FLOWER_FOREST,
                Biomes.BIRCH_FOREST,
                Biomes.DARK_FOREST,
                Biomes.OLD_GROWTH_BIRCH_FOREST,
                Biomes.SPARSE_JUNGLE
            ),
            CBiomes.sugar_forest
        )

        // 丛林
        transfer(
            listOf(
                Biomes.JUNGLE,
                Biomes.BAMBOO_JUNGLE
            ),
            CBiomes.sugar_enchanted_forest
        )

        // 沙漠
        transfer(
            listOf(
                Biomes.DESERT
            ),
            CBiomes.caramel_forest
        )

        // 恶地
        transfer(
            listOf(
                Biomes.BADLANDS,
                Biomes.WOODED_BADLANDS,
                Biomes.ERODED_BADLANDS
            ),
            CBiomes.sugar_hell_mountains
        )

        // 山地 / 高原 / 风袭 / 石岸
        transfer(
            listOf(
                Biomes.MEADOW,
                Biomes.SAVANNA_PLATEAU,
                Biomes.CHERRY_GROVE,
                Biomes.WINDSWEPT_GRAVELLY_HILLS,
                Biomes.WINDSWEPT_HILLS,
                Biomes.WINDSWEPT_FOREST,
                Biomes.WINDSWEPT_SAVANNA,
                Biomes.STONY_SHORE
            ),
            CBiomes.sugar_mountains
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

        // 洞穴
        transfer(
            listOf(
                Biomes.DRIPSTONE_CAVES,
                Biomes.LUSH_CAVES,
                Biomes.DEEP_DARK
            ),
            CBiomes.sugar_plains
        )
    }

    val byPath = biomes.associateBy { it.unwrapKey().getOrNull() }

    /** delegate 的 holder 在解码阶段只绑定了 key，检查延迟到首次使用时进行。 */
    private val source: BiomeSource by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        delegate.also { source ->
            val unknown = source.possibleBiomes()
                .mapNotNull { it.unwrapKey().getOrNull() }
                .filter { it !in mappings }
                .map { it.location() }
                .joinToString("\n")
            if (unknown.isNotEmpty()) clog.error("Unknown overworld biomes: \n$unknown")
        }
    }

    override fun codec() = codec
    override fun collectPossibleBiomes(): Stream<Holder<Biome>> = biomes.stream()

    override fun getNoiseBiome(quartX: Int, quartY: Int, quartZ: Int, sampler: Climate.Sampler): Holder<Biome> {
        return mapOverworldBiomes(getRawNoiseBiome(quartX, quartY, quartZ, sampler))
    }

    fun getRawNoiseBiome(quartX: Int, quartY: Int, quartZ: Int, sampler: Climate.Sampler): Holder<Biome> {
//        val p = sampler.sample(quartX, quartY, quartZ)
//        if (quartX % 16 == 0 || quartZ % 16 == 0) {
//            clog.info("climate($quartX,$quartY,$quartZ) T=${p.temperature()} H=${p.humidity()} D=${p.depth()} W=${p.weirdness()} C=${p.continentalness()} E=${p.erosion()}")
//        }
        return source.getNoiseBiome(quartX, quartY, quartZ, sampler)
    }

    fun mapOverworldBiomes(biome: Holder<Biome>): Holder<Biome> {
        val target = biome.unwrapKey().getOrNull()?.let { mappings[it] }
        return byPath[target] ?: biome
    }


}