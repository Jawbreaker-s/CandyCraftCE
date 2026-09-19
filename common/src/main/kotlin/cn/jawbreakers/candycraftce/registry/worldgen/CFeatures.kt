package cn.jawbreakers.candycraftce.registry.worldgen

import cn.jawbreakers.candycraftce.level.feature.*
import cn.jawbreakers.candycraftce.level.feature.CandyLiquidLakeFeature.FluidMode.CHOCOLATE
import cn.jawbreakers.candycraftce.level.feature.CandyLiquidLakeFeature.FluidMode.WATER_OR_GRENADINE
import cn.jawbreakers.candycraftce.registry.CBlockTags
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.data.worldgen.placement.PlacementUtils
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration
import net.minecraft.world.level.levelgen.placement.*
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter.forPredicate
import java.util.function.Supplier


object CFeatures {
    init {
        CLogUtils.sign()
    }
    //todo as structure
//    val honey_dungeon = register("honey_dungeon", ::HoneyDungeonFeature)

    //    val jelly_dungeon = register("jelly_dungeon", ::JellyDungeonFeature)
    //    val suguard_dungeon = register("suguard_dungeon", ::SuguardDungeonFeature)


    val candy_grass = register("candy_grass", ::CandyGrassFeature)
    val candy_seaweed = register("candy_seaweed", ::CandySeaweedFeature)
    val groundwater = register("candy_liquid_lake") { CandyLiquidLakeFeature(WATER_OR_GRENADINE) }
    val chocolate_groundwater = register("candy_chocolate_lake") { CandyLiquidLakeFeature(CHOCOLATE) }
    val marshmallow_waterlily = register("marshmallow_waterlily", ::MarshmallowSliceFeature)
    val gummy_worm = register("gummy_worm", ::GummyWormFeature)


//    val candy_plant_house =
//        register("candy_plant_house") { TemplateSurfaceStructureFeature(ResourceLocation("candy_plant_house")) }

//    val ice_tower = register("ice_tower") { LegacyStructureFeature(LegacyStructureFeature.Kind.ICE_TOWER) }
//    val ice_cream_dome =
//        register("ice_cream_dome") { LegacyStructureFeature(LegacyStructureFeature.Kind.ICE_CREAM_DOME) }
//    val water_temple = register("water_temple") { LegacyStructureFeature(LegacyStructureFeature.Kind.WATER_TEMPLE) }
//    val geyser = register("geyser") { LegacyStructureFeature(LegacyStructureFeature.Kind.GEYSER) }
//    val chewing_gum_totem = register("chewing_gum_totem") {
//        LegacyStructureFeature(LegacyStructureFeature.Kind.CHEWING_GUM_TOTEM)
//    }
//    val floating_island =
//        register("floating_island") { LegacyStructureFeature(LegacyStructureFeature.Kind.FLOATING_ISLAND) }
//    val underground_village = register("underground_village") {
//        LegacyStructureFeature(LegacyStructureFeature.Kind.UNDERGROUND_VILLAGE)
//    }

    fun <F : Feature<*>> register(name: String, factory: Supplier<F>): Entry<F> {
        return CPlatformUtils.levels.registerFeature(name, factory)
    }


    //data

    init {
        CPlatformUtils.datagen?.onBootstrap {
            add(Registries.CONFIGURED_FEATURE, ::bootstrapConfigured)
            add(Registries.PLACED_FEATURE, ::bootstrapPlaced)
        }
    }

    //===============================Configured Features===============================
////    val cotton_candy_tree = register("cotton_candy_tree", ::CottonCandyTreeFeature)
////    val chocolate_tree = register("sweetscape_chocolate_tree", ::SweetscapeChocolateTreeFeature)
////    val caramel_tree = register("caramel_tree") { LegacyCandyTreeFeature(LegacyCandyTreeFeature.Kind.CARAMEL) }
////    val caramel_forest_tree = register("caramel_forest_tree") { LegacyCandyTreeFeature(LegacyCandyTreeFeature.Kind.CARAMEL_FOREST) }
////    val cherry_tree = register("cherry_tree") { LegacyCandyTreeFeature(LegacyCandyTreeFeature.Kind.CHERRY) }
////    val enchanted_tree = register("enchanted_tree") { LegacyCandyTreeFeature(LegacyCandyTreeFeature.Kind.ENCHANTED) }
////    val white_chocolate_tree = register("white_chocolate_tree") { LegacyCandyTreeFeature(LegacyCandyTreeFeature.Kind.WHITE_CHOCOLATE) }
    val configured_candy_grass = configured(candy_grass)
    val configured_candy_seaweed = configured(candy_seaweed)
    val configured_chocolate_groundwater = configured(chocolate_groundwater)
    val configured_groundwater = configured(groundwater)
    val configured_marshmallow_waterlily = configured(marshmallow_waterlily)
    val configured_gummy_worm = configured(gummy_worm)

    val configured_tree_chocolate = configured("tree_chocolate")
    val configured_tree_chocolate_fancy = configured("tree_chocolate_fancy")
    val configured_tree_caramel = configured("tree_caramel")
    val configured_tree_caramel_forest = configured("tree_caramel_forest")
    val configured_tree_candied_cherry = configured("tree_candied_cherry")
    val configured_tree_enchanted = configured("tree_enchanted")
    val configured_tree_white_chocolate = configured("tree_white_chocolate")
    val configured_tree_candy_plains = configured("tree_candy_plains")
    val configured_tree_candy_forest = configured("tree_candy_forest")

    private fun configured(entry: Entry<out Feature<*>>): ResourceKey<ConfiguredFeature<*, *>> = configured(entry.id)
    private fun configured(name: String) = configured(name.modLoc())
    private fun configured(id: ResourceLocation): ResourceKey<ConfiguredFeature<*, *>> =
        ResourceKey.create(Registries.CONFIGURED_FEATURE, id)


    fun bootstrapConfigured(ctx: BootstapContext<ConfiguredFeature<*, *>>) {
        val lookup = ctx.lookup(Registries.PLACED_FEATURE)

        fun builtin(key: ResourceKey<ConfiguredFeature<*, *>>, feature: Feature<NoneFeatureConfiguration>) {
            ctx.register(key, ConfiguredFeature(feature, NoneFeatureConfiguration.INSTANCE))
        }

        fun choice(
            key: ResourceKey<ConfiguredFeature<*, *>>,
            defaultFeature: ResourceKey<PlacedFeature>,
            vararg checked: Pair<ResourceKey<PlacedFeature>, Float>,
        ) {
            ctx.register(key, ConfiguredFeature(Feature.RANDOM_SELECTOR, RandomFeatureConfiguration(checked.map {
                WeightedPlacedFeature(lookup.getOrThrow(it.first), it.second)
            }, lookup.getOrThrow(defaultFeature))))
        }
        builtin(configured_candy_grass, candy_grass.get())
        builtin(configured_candy_seaweed, candy_seaweed.get())
        builtin(configured_chocolate_groundwater, chocolate_groundwater.get())
        builtin(configured_groundwater, groundwater.get())
        builtin(configured_marshmallow_waterlily, marshmallow_waterlily.get())
        builtin(configured_gummy_worm, gummy_worm.get())
        ctx.register(configured_tree_chocolate, createChocolateTree().configure())
        ctx.register(configured_tree_caramel, createCaramelWild().configure())
        ctx.register(configured_tree_chocolate_fancy, createChocolateFancy().configure())
        ctx.register(configured_tree_caramel_forest, createCaramelForest().configure())
        ctx.register(configured_tree_candied_cherry, createCandiedCherryTree().configure())
        ctx.register(configured_tree_enchanted, createEnchantTree().configure())
        ctx.register(configured_tree_white_chocolate, createWhiteChocolateTree().configure())
        choice(
            configured_tree_candy_plains, checked_tree_chocolate,
            checked_tree_candied_cherry to 0.1f,
            checked_tree_caramel to 1 / 3f
        )
        choice(
            configured_tree_candy_forest, checked_tree_chocolate,
            checked_tree_candied_cherry to 0.01f,
            checked_tree_chocolate_fancy to 0.04f,
            checked_tree_caramel to 1 / 3f
        )

    }

    //===============================Placed Features===============================
    val placed_candy_grass = placed(configured_candy_grass)
    val placed_candy_seaweed = placed(configured_candy_seaweed)
    val placed_chocolate_groundwater = placed(configured_chocolate_groundwater)
    val placed_groundwater = placed(configured_groundwater)
    val placed_marshmallow_waterlily = placed(configured_marshmallow_waterlily)
    val placed_marshmallow_waterlily_dense = placed(configured_marshmallow_waterlily, "_dense")
    val placed_gummy_worm = placed(configured_gummy_worm)

    val checked_tree_chocolate = placed(configured_tree_chocolate, "_checked")
    val checked_tree_chocolate_fancy = placed(configured_tree_chocolate_fancy, "_checked")
    val checked_tree_caramel = placed(configured_tree_caramel, "_checked")
    val checked_tree_candied_cherry = placed(configured_tree_candied_cherry, "_checked")

    val placed_tree_candy_plains = placed(configured_tree_candy_plains)
    val placed_tree_candy_forest = placed(configured_tree_candy_forest)
    val placed_tree_caramel_forest = placed(configured_tree_caramel_forest)
    val placed_tree_enchanted = placed(configured_tree_enchanted)
    val placed_tree_white_chocolate_forest = placed(configured_tree_white_chocolate, "_forest")
    val placed_tree_white_chocolate_mountain = placed(configured_tree_white_chocolate, "_mountain")

    private fun placed(id: ResourceLocation): ResourceKey<PlacedFeature> =
        ResourceKey.create(Registries.PLACED_FEATURE, id)

    private fun placed(name: String) = placed(name.modLoc())
    private fun placed(configured: ResourceKey<ConfiguredFeature<*, *>>, suffix: String? = "") =
        placed(configured.location().let { if (suffix != null) it.withSuffix(suffix) else it })

    fun bootstrapPlaced(ctx: BootstapContext<PlacedFeature>) {
        val lookup = ctx.lookup(Registries.CONFIGURED_FEATURE)
        fun register(
            key: ResourceKey<PlacedFeature>,
            configured: ResourceKey<ConfiguredFeature<*, *>>,
            placements: List<PlacementModifier>,
        ) = PlacementUtils.register(ctx, key, lookup.get(configured).orElseThrow(), placements)

        fun checked(key: ResourceKey<PlacedFeature>, configured: ResourceKey<ConfiguredFeature<*, *>>) =
            register(key, configured, listOf())
        register(
            placed_candy_grass, configured_candy_grass,
            listOf(count(6), in_square, biome)
        )
        register(
            placed_candy_seaweed, configured_candy_seaweed,
            listOf(count(8), in_square, heightIn(0..128), biome)
        )
        register(
            placed_chocolate_groundwater, configured_chocolate_groundwater,
            listOf(rarity(20), in_square, heightIn(0..62), biome)
        )
        register(
            placed_groundwater, configured_groundwater,
            listOf(rarity(8), in_square, heightIn(5..60), biome)
        )
        register(
            placed_marshmallow_waterlily, configured_marshmallow_waterlily,
            listOf(rarity(5), count(1), in_square, on_surface, biome)
        )
        register(
            placed_marshmallow_waterlily_dense, configured_marshmallow_waterlily,
            listOf(count(3), in_square, on_surface, biome)
        )
        register(
            placed_gummy_worm, configured_gummy_worm,
            listOf(in_square, on_surface, biome)
        )
        //trees
        checked(checked_tree_chocolate, configured_tree_chocolate)
        checked(checked_tree_chocolate_fancy, configured_tree_chocolate_fancy)
        checked(checked_tree_caramel, configured_tree_caramel)
        checked(checked_tree_candied_cherry, configured_tree_candied_cherry)
        register(
            placed_tree_candy_plains, configured_tree_candy_plains,
            listOf(rarity(10), count(1), in_square, motion_no_leaves, on_candy_soil, biome)
        )
        register(
            placed_tree_candy_forest, configured_tree_candy_forest,
            listOf(count(9), in_square, motion_no_leaves, on_candy_soil, biome)
        )

        register(
            placed_tree_caramel_forest, configured_tree_caramel_forest,
            listOf(count(10), in_square, motion_no_leaves, on_candy_soil, biome)
        )
        register(
            placed_tree_enchanted, configured_tree_enchanted,
            listOf(count(11), in_square, motion_no_leaves, on_candy_soil, biome)
        )
        register(
            placed_tree_white_chocolate_forest, configured_tree_white_chocolate,
            listOf(count(9), in_square, motion_no_leaves, on_candy_soil, biome)
        )
        register(
            placed_tree_white_chocolate_mountain, configured_tree_white_chocolate,
            listOf(count(9), in_square, motion_no_leaves, on_candy_soil, biome)
        )

    }

    private fun count(count: Int) = CountPlacement.of(count)
    private fun rarity(chance: Int) = RarityFilter.onAverageOnceEvery(chance)
    private val in_square = InSquarePlacement.spread()
    private val biome = BiomeFilter.biome()
    private fun heightIn(range: IntRange) =
        HeightRangePlacement.uniform(VerticalAnchor.absolute(range.first), VerticalAnchor.absolute(range.last))

    private val on_surface = HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG)
    private val motion_no_leaves = HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES)
    private val on_candy_soil = forPredicate(BlockPredicate.matchesTag(Vec3i.ZERO.below(), CBlockTags.candy_soil))
}
