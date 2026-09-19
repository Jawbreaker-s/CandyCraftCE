package cn.jawbreakers.candycraftce.registry.worldgen

import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.core.HolderGetter
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeGenerationSettings
import net.minecraft.world.level.biome.BiomeSpecialEffects
import net.minecraft.world.level.biome.MobSpawnSettings
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.GenerationStep.Decoration.VEGETAL_DECORATION
import net.minecraft.world.level.levelgen.placement.PlacedFeature

object CBiomes {
    init {
        CLogUtils.sign()
    }

    val dungeon = bind("dungeon")

    //
    val caramel_forest = bind("caramel_forest")
    val chocolate_forest = bind("chocolate_forest")
    val cotton_candy_plains = bind("cotton_candy_plains")
    val gummy_swamp = bind("gummy_swamp")
    val ice_cream_plains = bind("ice_cream_plains")
    val ice_cream_sky_mountains = bind("ice_cream_sky_mountains")
    val white_chocolate_forest = bind("white_chocolate_forest")
    val enchanted_forest = bind("enchanted_forest")
    val sugar_forest = bind("sugar_forest")
    val pudding_hill = bind("pudding_hill")
    val pudding_plains = bind("pudding_plains")
    val sugar_oceans = bind("sugar_oceans")
    val sugar_river = bind("sugar_river")

    val allCandyBiomes = listOf(
        caramel_forest, chocolate_forest, cotton_candy_plains, gummy_swamp, ice_cream_plains, ice_cream_sky_mountains,
        white_chocolate_forest, enchanted_forest, sugar_forest, pudding_hill, pudding_plains, sugar_oceans, sugar_river
    )

    private fun bind(name: String): ResourceKey<Biome> {
        return ResourceKey.create(Registries.BIOME, name.modLoc())
    }


    //=======================Data=========================

    init {
        CPlatformUtils.datagen?.onBootstrap {
            add(Registries.BIOME, ::bootstrap)
        }
    }

    fun bootstrap(ctx: BootstapContext<Biome>) {
        val lookup = ctx.lookup(Registries.PLACED_FEATURE)
        fun registerBiome(
            id: ResourceKey<Biome>,
            hasPrecipitation: Boolean,
            temperature: Float,
            downfall: Float,
            spawns: MobSpawnSettings.Builder.() -> Unit = {},
            effects: BiomeSpecialEffects.Builder.() -> Unit = {},
            generation: BiomeGenerationSettings.PlainBuilder.() -> Unit = {},
        ) {
            ctx.register(
                id, Biome.BiomeBuilder()
                    .hasPrecipitation(hasPrecipitation)
                    .temperature(temperature)
                    .downfall(downfall)
                    .mobSpawnSettings(MobSpawnSettings.Builder().apply(spawns).build())
                    .specialEffects(BiomeSpecialEffects.Builder().apply(effects).build())
                    .generationSettings(BiomeGenerationSettings.PlainBuilder().apply(generation).build())
                    .build()
            )
        }

        with(CFeatures) {
            //dungeon
            registerBiome(
                dungeon, false, 0.8f, 0f,
                effects = {
                    skyColor(0)
                    fogColor(16767191)
                    waterColor(13473279)
                    waterFogColor(13473279)
                }
            )
            //caramel_forest
            registerBiome(
                caramel_forest, false, 0.5f, 0.5f,
                {
                    creatureGenerationProbability(0.1f)
                },
                {
                    skyColor(16764032)
                    fogColor(16764032)
                    waterColor(13473279)
                    waterFogColor(13473279)
                },
                {
                    step(lookup, VEGETAL_DECORATION) {
                        +placed_tree_caramel_forest
                        +placed_candy_grass
                    }
                }
            )
            //gummy_swamp
            registerBiome(
                gummy_swamp, false, 0.9f, 0.8f,
                {
                    creatureGenerationProbability(3f / 17)
                },
                {
                    skyColor(16776684)
                    fogColor(16767130)
                    waterColor(14608288)
                    waterFogColor(8409728)
                },
                {
                    step(lookup, VEGETAL_DECORATION) {
                        +placed_gummy_worm
                    }
                }
            )
            //ice_cream_plains
            registerBiome(
                ice_cream_plains, true, 0.0f, 0.5f,
                {
                    creatureGenerationProbability(0.1f)
                },
                {
                    skyColor(14474460)
                    fogColor(16767191)
                    waterColor(16777215)
                    waterFogColor(16777215)
                },
                {
                    step(lookup, VEGETAL_DECORATION) {
                        +placed_candy_grass
                    }
                }
            )
            //ice_cream_sky_mountains
            registerBiome(
                ice_cream_sky_mountains, true, 0.0f, 0.5f,
                {
                    creatureGenerationProbability(0.1f)
                },
                {
                    skyColor(16777215)
                    fogColor(16777215)
                    waterColor(16777215)
                    waterFogColor(16777215)
                },
                {
                    step(lookup, VEGETAL_DECORATION) {
                        +placed_tree_white_chocolate_mountain
                    }
                }
            )
            //white_chocolate_forest
            registerBiome(
                white_chocolate_forest, false, 0.0f, 0.5f,
                {
                    creatureGenerationProbability(0.1f)
                },
                {
                    skyColor(14474460)
                    fogColor(16767191)
                    waterColor(13473279)
                    waterFogColor(13473279)
                },
                {
                    step(lookup, VEGETAL_DECORATION) {
                        +placed_tree_white_chocolate_forest
                        +placed_candy_grass
                    }
                }
            )
            //pudding_hill
            registerBiome(
                pudding_hill, true, 0.0f, 0.5f, {
                    creatureGenerationProbability(0.1f)
                },
                {
                    skyColor(16112860)
                    fogColor(16767191)
                    waterColor(13473279)
                    waterFogColor(13473279)
                },
                {
                    step(lookup, VEGETAL_DECORATION) {
                        +placed_candy_grass
                        +placed_tree_candy_plains
                    }
                }
            )
            //pudding_plains
            registerBiome(
                pudding_plains, true, 0.0f, 0.5f, {
                    creatureGenerationProbability(0.1f)
                },
                {
                    skyColor(16636119)
                    fogColor(16767191)
                    waterColor(13473279)
                    waterFogColor(13473279)
                },
                {
                    step(lookup, VEGETAL_DECORATION) {
                        +placed_candy_grass
                        +placed_tree_candy_plains
                    }
                }
            )
            //sugar_forest
            registerBiome(
                sugar_forest, false, 0.5f, 0.5f,
                {
                    creatureGenerationProbability(0.1f)
                },
                {
                    skyColor(16636119)
                    fogColor(16767191)
                    waterColor(13473279)
                    waterFogColor(13473279)
                },
                {
                    step(lookup, VEGETAL_DECORATION) {
                        +placed_candy_grass
                        +placed_tree_candy_forest
                        +placed_marshmallow_waterlily_dense
                    }
                }
            )
            //sugar_river
            registerBiome(
                sugar_river, true, 0.5f, 0.5f,
                {
                    creatureGenerationProbability(0.1f)
                },
                {
                    skyColor(16636119)
                    fogColor(16767191)
                    waterColor(13473279)
                    waterFogColor(13473279)
                },
                {
                    step(lookup, VEGETAL_DECORATION) {
                        +placed_candy_grass
                        +placed_candy_seaweed
                        +placed_marshmallow_waterlily
                    }
                }
            )
            //sugar_oceans
            registerBiome(
                sugar_oceans, true, 0.5f, 0.5f,
                {
                    creatureGenerationProbability(0.1f)
                },
                {
                    skyColor(15986175)
                    fogColor(13473279)
                    waterColor(13473279)
                    waterFogColor(13473279)
                },
                {
                    step(lookup, VEGETAL_DECORATION) {
                        +placed_candy_grass
                        +placed_candy_seaweed
                        +placed_marshmallow_waterlily
                    }
                }
            )
            //enchanted_forest
            registerBiome(
                enchanted_forest, false, 0.5f, 0.5f,
                {
                    creatureGenerationProbability(0.1f)
                },
                {
                    skyColor(14207743)
                    fogColor(11587794)
                    waterColor(13473279)
                    waterFogColor(13473279)
                },
                {
                    step(lookup, VEGETAL_DECORATION) {
                        +placed_candy_grass
                        +placed_tree_enchanted
                    }
                }
            )
            //wip
            //chocolate_forest
            registerBiome(
                chocolate_forest, true, 0.8f, 0.3f,
                {
                    creatureGenerationProbability(0.1f)
                },
                {
                    skyColor(15784376)
                    fogColor(15784376)
                    waterColor(13473279)
                    waterFogColor(13473279)
                },
                {
                    //TODO
                }
            )
            registerBiome(
                cotton_candy_plains, false, 0.8f, 0.3f,
                {
                    creatureGenerationProbability(0.1f)
                },
                {
                    skyColor(16774650)
                    fogColor(16774650)
                    waterColor(13473279)
                    waterFogColor(13473279)
                },
                {
                    step(lookup, VEGETAL_DECORATION) {
                        //todo
                    }
                }
            )


        }
    }
}

private fun BiomeGenerationSettings.PlainBuilder.step(
    lookup: HolderGetter<PlacedFeature>,
    step: GenerationStep.Decoration,
    action: StepScope.() -> Unit,
) {
    StepScope(this, step, lookup).apply(action)
}

private class StepScope(
    private val builder: BiomeGenerationSettings.PlainBuilder,
    val step: GenerationStep.Decoration,
    private val lookup: HolderGetter<PlacedFeature>,
) {
    fun add(key: ResourceKey<PlacedFeature>) {
        builder.addFeature(step, lookup.getOrThrow(key))
    }

    operator fun ResourceKey<PlacedFeature>.unaryPlus() = add(this)

}