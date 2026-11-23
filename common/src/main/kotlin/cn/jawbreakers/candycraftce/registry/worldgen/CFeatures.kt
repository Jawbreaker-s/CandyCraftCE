package cn.jawbreakers.candycraftce.registry.worldgen

import cn.jawbreakers.candycraftce.level.feature.*
import cn.jawbreakers.candycraftce.level.feature.CandyUnderLakeFeature.FluidMode.Companion.WATER_OR_GRENADINE
import cn.jawbreakers.candycraftce.registry.CBlockTags
import cn.jawbreakers.candycraftce.registry.CBlockTags.jelly_queen_spawnable_on
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifDatagen
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.data.worldgen.placement.PlacementUtils
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.VerticalAnchor.*
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration
import net.minecraft.world.level.levelgen.placement.*
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter.forPredicate
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement.triangle
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement.uniform
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
    val groundwater = register("candy_liquid_lake") { CandyUnderLakeFeature(WATER_OR_GRENADINE) }
    val marshmallow_waterlily = register("marshmallow_waterlily", ::MarshmallowSliceFeature)
    val jelly_queen = register("jelly_queen", ::JellyBossFeature)

//    val candy_plant_house = register("candy_plant_house") { TemplateSurfaceStructureFeature(ResourceLocation("candy_plant_house")) }
//    val ice_tower = register("ice_tower") { LegacyStructureFeature(LegacyStructureFeature.Kind.ICE_TOWER) }
//    val ice_cream_dome = register("ice_cream_dome") { LegacyStructureFeature(LegacyStructureFeature.Kind.ICE_CREAM_DOME) }
//    val water_temple = register("water_temple") { LegacyStructureFeature(LegacyStructureFeature.Kind.WATER_TEMPLE) }
//    val chewing_gum_totem = register("chewing_gum_totem") { LegacyStructureFeature(LegacyStructureFeature.Kind.CHEWING_GUM_TOTEM)

    fun <F : Feature<*>> register(name: String, factory: Supplier<F>): Entry<F> {
        return CPlatformUtils.levels.registerFeature(name, factory)
    }

    //data
    init {
        ifDatagen {
            onBootstrap {
                add(Registries.CONFIGURED_FEATURE, ::bootstrapConfigured)
                add(Registries.PLACED_FEATURE, ::bootstrapPlaced)
            }
        }
    }

    //===============================Configured Features===============================
    val configured_candy_grass = configured(candy_grass)
    val configured_candy_seaweed = configured(candy_seaweed)
    val configured_groundwater = configured(groundwater)
    val configured_marshmallow_waterlily = configured(marshmallow_waterlily)
    val configured_jelly_queen = configured(jelly_queen)
    val configured_ore_licorice = configured("ore_licorice")
    val configured_ore_licorice_small = configured("ore_licorice_small")
    val configured_ore_honey = configured("ore_honey")
    val configured_ore_honey_buried = configured("ore_honey_buried")
    val configured_ore_nougat = configured("ore_nougat")
    val configured_ore_jelly = configured("ore_jelly")
    val configured_ore_jelly_buried = configured("ore_jelly_buried")
    val configured_ore_pez_small = configured("ore_pez_small")
    val configured_ore_pez_large = configured("ore_pez_large")
    val configured_ore_pez_buried = configured("ore_pez_buried")
    val configured_ore_pudding = configured("ore_pudding")
    val configured_ore_sugar_sand = configured("ore_sugar_sand")
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
        builtin(configured_groundwater, groundwater.get())
        builtin(configured_marshmallow_waterlily, marshmallow_waterlily.get())
        builtin(configured_jelly_queen, jelly_queen.get())
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
        ctx.register(configured_ore_licorice, createLicoriceOre().configure())
        ctx.register(configured_ore_licorice_small, createLicoriceOreSmall().configure())
        ctx.register(configured_ore_honey, createHoneyOre().configure())
        ctx.register(configured_ore_honey_buried, createHoneyOreBuried().configure())
        ctx.register(configured_ore_nougat, createNougatOre().configure())
        ctx.register(configured_ore_jelly, createJellyOre().configure())
        ctx.register(configured_ore_jelly_buried, createJellyOreBuried().configure())
        ctx.register(configured_ore_pez_small, createPezOreSmall().configure())
        ctx.register(configured_ore_pez_large, createPezOreLarge().configure())
        ctx.register(configured_ore_pez_buried, createPezOreBuried().configure())
        ctx.register(configured_ore_pudding, createPudding().configure())
        ctx.register(configured_ore_sugar_sand, createSugarSand().configure())
    }

    //===============================Placed Features===============================
    val placed_candy_grass = placed(configured_candy_grass)
    val placed_candy_seaweed = placed(configured_candy_seaweed)
    val placed_groundwater = placed(configured_groundwater)
    val placed_marshmallow_waterlily = placed(configured_marshmallow_waterlily)
    val placed_marshmallow_waterlily_dense = placed(configured_marshmallow_waterlily, "_dense")
    val placed_jelly_queen = placed(configured_jelly_queen)

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

    val placed_ore_licorice_upper = placed(configured_ore_licorice, "_upper")
    val placed_ore_licorice_middle = placed(configured_ore_licorice, "_middle")
    val placed_ore_licorice_small = placed(configured_ore_licorice_small)
    val placed_ore_honey_upper = placed(configured_ore_honey)
    val placed_ore_honey_lower = placed(configured_ore_honey_buried)
    val placed_ore_nougat = placed(configured_ore_nougat)
    val placed_ore_jelly_extra = placed(configured_ore_jelly, "_extra")
    val placed_ore_jelly = placed(configured_ore_jelly_buried)
    val placed_ore_jelly_lower = placed(configured_ore_jelly_buried, "_lower")
    val placed_ore_pez = placed(configured_ore_pez_small)
    val placed_ore_pez_large = placed(configured_ore_pez_large)
    val placed_ore_pez_buried = placed(configured_ore_pez_buried)
    val placed_ore_pudding = placed(configured_ore_pudding)
    val placed_ore_sugar_sand = placed(configured_ore_sugar_sand)

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
            placed_jelly_queen, configured_jelly_queen,
            listOf(
                rarity(100), in_square, on_surface, on(jelly_queen_spawnable_on), biome
            )
        )
        register(
            placed_candy_grass, configured_candy_grass,
            listOf(count(6), in_square, biome)
        )
        register(
            placed_candy_seaweed, configured_candy_seaweed,
            listOf(count(8), in_square, heightIn(0..128), biome)
        )
        register(
            placed_groundwater, configured_groundwater,
            listOf(rarity(8), in_square, heightIn(5..60), biome)
        )
        register(
            placed_marshmallow_waterlily, configured_marshmallow_waterlily,
            listOf(rarity(5), in_square, on_surface, biome)
        )
        register(
            placed_marshmallow_waterlily_dense, configured_marshmallow_waterlily,
            listOf(count(3), in_square, on_surface, biome)
        )
        //trees
        checked(checked_tree_chocolate, configured_tree_chocolate)
        checked(checked_tree_chocolate_fancy, configured_tree_chocolate_fancy)
        checked(checked_tree_caramel, configured_tree_caramel)
        checked(checked_tree_candied_cherry, configured_tree_candied_cherry)
        register(
            placed_tree_candy_plains, configured_tree_candy_plains,
            listOf(rarity(10), in_square, motion_no_leaves, on_candy_soil, biome)
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
            listOf(count(12), in_square, motion_no_leaves, on_candy_soil, biome)
        )
        register(
            placed_tree_white_chocolate_forest, configured_tree_white_chocolate,
            listOf(count(9), in_square, motion_no_leaves, on_candy_soil, biome)
        )
        register(
            placed_tree_white_chocolate_mountain, configured_tree_white_chocolate,
            listOf(count(9), in_square, motion_no_leaves, on_candy_soil, biome)
        )
        //ores
        //iron
        register(
            placed_ore_licorice_upper, configured_ore_licorice,
            commonOrePlacement(90, triangle(aboveBottom(80), top()))
        )
        register(
            placed_ore_licorice_middle, configured_ore_licorice,
            commonOrePlacement(10, uniform(absolute(-24), absolute(56)))
        )
        register(
            placed_ore_licorice_small, configured_ore_licorice_small,
            commonOrePlacement(10, uniform(bottom(), absolute(72)))
        )
        //coal
        register(
            placed_ore_honey_upper, configured_ore_honey,
            commonOrePlacement(30, uniform(absolute(136), top()))
        )
        register(
            placed_ore_honey_lower, configured_ore_honey_buried,
            commonOrePlacement(200, triangle(absolute(0), absolute(192)))
        )
        //emerald
        register(
            placed_ore_nougat, configured_ore_nougat,
            commonOrePlacement(100, triangle(absolute(-16), absolute(480)))
        )
        //gold
        register(
            placed_ore_jelly_extra, configured_ore_jelly,
            commonOrePlacement(50, uniform(absolute(32), absolute(256)))
        )
        register(
            placed_ore_jelly, configured_ore_jelly_buried,
            commonOrePlacement(4, triangle(absolute(-64), absolute(32)))
        )
        register(
            placed_ore_jelly_lower, configured_ore_jelly_buried,
            orePlacement(countIn(0..1), uniform(absolute(-64), absolute(-48)))
        )
        //diamond
        //PlacementUtils.register(context, ORE_DIAMOND, holder19, commonOrePlacement(7, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))));
        register(
            placed_ore_pez, configured_ore_pez_small,
            commonOrePlacement(7, triangle(aboveBottom(-80), aboveBottom(80)))
        )
        register(
            placed_ore_pez_large, configured_ore_pez_large,
            rareOrePlacement(9, triangle(aboveBottom(-80), aboveBottom(80)))
        )
        register(
            placed_ore_pez_buried, configured_ore_pez_buried,
            commonOrePlacement(4, triangle(aboveBottom(-80), aboveBottom(80)))
        )
        register(
            placed_ore_pudding, configured_ore_pudding,
            commonOrePlacement(7, triangle(absolute(0), absolute(160)))
        )
        register(
            placed_ore_sugar_sand, configured_ore_sugar_sand,
            commonOrePlacement(14, triangle(bottom(), top()))
        )

    }

    private fun orePlacement(
        countPlacement: PlacementModifier,
        heightRange: PlacementModifier,
    ) = listOf(
        countPlacement,
        in_square,
        heightRange,
        biome
    )

    private fun commonOrePlacement(count: Int, heightRange: PlacementModifier) =
        orePlacement(count(count), heightRange)

    private fun rareOrePlacement(chance: Int, heightRange: PlacementModifier) =
        orePlacement(rarity(chance), heightRange)

    private fun count(count: Int) = CountPlacement.of(count)
    private fun countIn(count: IntRange) = CountPlacement.of(UniformInt.of(count.first, count.last))
    private fun rarity(chance: Int) = RarityFilter.onAverageOnceEvery(chance)
    private val in_square get() = InSquarePlacement.spread()
    private val biome get() = BiomeFilter.biome()
    private fun heightIn(range: IntRange) =
        uniform(absolute(range.first), absolute(range.last))

    private val on_surface get() = HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG)
    private val motion_no_leaves get() = HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES)
    private val on_candy_soil get() = on(CBlockTags.candy_soil)
    private fun on(tag: TagKey<Block>) =
        forPredicate(BlockPredicate.matchesTag(Vec3i.ZERO.below(), tag))
}
