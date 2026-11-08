package cn.jawbreakers.candycraftce.registry.worldgen

import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifDatagen
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.core.HolderGetter
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
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
        caramel_forest, ice_cream_plains, ice_cream_sky_mountains,
        white_chocolate_forest, enchanted_forest, sugar_forest, pudding_hill, pudding_plains, sugar_oceans, sugar_river
    )

    private fun bind(name: String): ResourceKey<Biome> {
        return ResourceKey.create(Registries.BIOME, name.modLoc())
    }


    //=======================Data=========================

    init {
        ifDatagen {
            onBootstrap {
                add(Registries.BIOME, ::bootstrap)
            }
        }
    }

    fun bootstrap(ctx: BootstapContext<Biome>) {
        val lookup = ctx.lookup(Registries.PLACED_FEATURE)
        fun registerBiome(
            id: ResourceKey<Biome>,
//            hasPrecipitation: Boolean, //we use [CandyPrecipitation]
            temperature: Float,
            downfall: Float,
            spawns: MobSpawnSettings.Builder.() -> Unit = {},
            effects: BiomeSpecialEffects.Builder.() -> Unit = {},
            generation: BiomeGenerationSettings.PlainBuilder.() -> Unit = {},
        ) {
            ctx.register(
                id, Biome.BiomeBuilder()
                    .hasPrecipitation(false)
                    .temperature(temperature)
                    .downfall(downfall)
                    .mobSpawnSettings(MobSpawnSettings.Builder().apply(spawns).build())
                    .specialEffects(BiomeSpecialEffects.Builder().apply(effects).build())
                    .generationSettings(BiomeGenerationSettings.PlainBuilder().apply(generation).build())
                    .build()
            )
        }

        with(CEntityTypes) {
            with(CFeatures) {
                //dungeon
                registerBiome(
                    dungeon, 0.8f, 0f,
                    effects = {
                        skyColor(0)
                        fogColor(16767191)
                        waterColor(13473279)
                        waterFogColor(13473279)
                    }
                )
                //caramel_forest
                registerBiome(
                    caramel_forest, 0.5f, 0.5f,
                    {
                        creatureGenerationProbability(0.1f)
                        category(MobCategory.CREATURE) {
                            add(caramel_wolf, 50, 4, 4)
                            add(candy_pig, 21, 4, 6)
                            add(gummy_bunny, 21, 4, 6)
                        }
                        category(MobCategory.MONSTER) {
                            add(cotton_candy_spider, 100, 4, 4)
                            add(cookie_creeper, 100, 8, 15)
                            add(suguard, 160, 4, 4)
                        }
                        category(MobCategory.WATER_AMBIENT) {
                            add(cranfish, 50, 4, 4)
                        }
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
                //ice_cream_plains
                registerBiome(
                    ice_cream_plains, 0.0f, 0.5f,
                    {
                        creatureGenerationProbability(0.1f)
                        category(MobCategory.CREATURE) {
                            add(pingouin, 10, 2, 4)
                        }
                        category(MobCategory.MONSTER) {
                            add(cotton_candy_spider, 100, 4, 4)
                            add(cookie_creeper, 100, 8, 15)
                            add(suguard, 160, 4, 4)
                        }
                        category(MobCategory.WATER_AMBIENT) {
                            add(cranfish, 50, 4, 4)
                        }
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
                    ice_cream_sky_mountains, 0.0f, 0.5f,
                    {
                        creatureGenerationProbability(0.1f)
                        category(MobCategory.CREATURE) {
                            add(pingouin, 10, 2, 4)
                        }
                        category(MobCategory.MONSTER) {
                            add(cotton_candy_spider, 100, 4, 4)
                            add(cookie_creeper, 100, 8, 15)
                            add(suguard, 160, 4, 4)
                        }
                        category(MobCategory.WATER_AMBIENT) {
                            add(cranfish, 50, 4, 4)
                        }
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
                    white_chocolate_forest, 0.0f, 0.5f,
                    {
                        creatureGenerationProbability(0.1f)
                        category(MobCategory.CREATURE) {
                            add(caramel_wolf, 54, 4, 4)
                            add(waffle_sheep, 18, 2, 3)
                            add(candy_pig, 28, 3, 5)
                            add(gummy_bunny, 12, 4, 6)
                        }
                        category(MobCategory.MONSTER) {
                            add(cotton_candy_spider, 100, 4, 4)
                            add(cookie_creeper, 100, 8, 15)
                            add(suguard, 160, 4, 4)
                        }
                        category(MobCategory.WATER_AMBIENT) {
                            add(cranfish, 50, 4, 4)
                        }
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
                    pudding_hill, 0.0f, 0.5f, {
                        creatureGenerationProbability(0.1f)
                        category(MobCategory.CREATURE) {
                            add(waffle_sheep, 14, 2, 3)
                            add(candy_pig, 31, 3, 5)
                            add(gummy_bunny, 8, 4, 6)
                        }
                        category(MobCategory.MONSTER) {
                            add(cotton_candy_spider, 100, 4, 4)
                            add(cookie_creeper, 100, 8, 15)
                            add(suguard, 104, 4, 4)
                        }
                        category(MobCategory.WATER_AMBIENT) {
                            add(cranfish, 50, 4, 4)
                        }
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
                    pudding_plains, 0.0f, 0.5f, {
                        creatureGenerationProbability(0.1f)
                        category(MobCategory.CREATURE) {
                            add(candy_pig, 40, 4, 6)
                            add(gummy_bunny, 55, 4, 6)
                            add(waffle_sheep, 8, 2, 3)
                        }
                        category(MobCategory.MONSTER) {
                            add(caramel_bee, 40, 2, 3)
                            add(cotton_candy_spider, 100, 4, 4)
                            add(cookie_creeper, 100, 8, 15)
                            add(suguard, 100, 4, 4)
                        }
                        category(MobCategory.WATER_AMBIENT) {
                            add(cranfish, 50, 4, 4)
                        }
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
                    sugar_forest, 0.5f, 0.5f,
                    {
                        creatureGenerationProbability(0.1f)
                        category(MobCategory.CREATURE) {
                            add(candy_pig, 21, 4, 6)
                            add(gummy_bunny, 21, 4, 6)
                        }
                        category(MobCategory.MONSTER) {
                            add(cotton_candy_spider, 100, 4, 4)
                            add(cookie_creeper, 100, 8, 15)
                            add(suguard, 160, 4, 4)
                        }
                        category(MobCategory.WATER_AMBIENT) {
                            add(cranfish, 50, 4, 4)
                        }
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
                    sugar_river, 0.5f, 0.5f,
                    {
                        creatureGenerationProbability(0.1f)
                        category(MobCategory.MONSTER) {
                            add(cotton_candy_spider, 100, 4, 4)
                            add(cookie_creeper, 100, 8, 15)
                            add(suguard, 160, 4, 4)
                        }
                        category(MobCategory.WATER_AMBIENT) {
                            add(cranfish, 50, 4, 4)
                        }
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
                    sugar_oceans, 0.5f, 0.5f,
                    {
                        creatureGenerationProbability(0.1f)
                        category(MobCategory.WATER_AMBIENT) {
                            add(cranfish, 15, 3, 6)
                        }
//                        category(MobCategory.WATER_CREATURE) {

//                      //TODO      add(nessie, 15, 3, 6)
//                        }
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
                    enchanted_forest, 0.5f, 0.5f,
                    {
                        creatureGenerationProbability(0.1f)
                        category(MobCategory.CREATURE) {
                            add(candy_pig, 50, 4, 6)
                            add(gummy_bunny, 60, 4, 6)
                        }
                        category(MobCategory.MONSTER) {
                            add(cotton_candy_spider, 60, 4, 6)
                            add(cotton_candy_spider, 100, 4, 4)
                            add(cookie_creeper, 100, 8, 15)
                            add(suguard, 100, 4, 4)
                        }
                        category(MobCategory.WATER_AMBIENT) {
                            add(cranfish, 50, 4, 4)
                        }
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
            }
        }
    }
}

private fun MobSpawnSettings.Builder.category(
    category: MobCategory,
    action: CategoryScope.() -> Unit,
) {
    CategoryScope(this, category).apply(action)
}

private class CategoryScope(
    private val builder: MobSpawnSettings.Builder,
    val category: MobCategory,
) {
    fun add(entity: Entry<out EntityType<*>>, weight: Int, minCount: Int, maxCount: Int) {
        builder.addSpawn(category, MobSpawnSettings.SpawnerData(entity.value, weight, minCount, maxCount))
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