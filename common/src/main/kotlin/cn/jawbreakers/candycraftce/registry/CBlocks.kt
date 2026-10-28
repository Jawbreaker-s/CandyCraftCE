package cn.jawbreakers.candycraftce.registry

import cn.breadnicecat.candycraftce.core.block.blocks.CandyLeavesBlock
import cn.jawbreakers.candycraftce.block.*
import cn.jawbreakers.candycraftce.registry.CItems.asItem
import cn.jawbreakers.candycraftce.utils.*
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.minable
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.whenInitialized
import cn.jawbreakers.candycraftce.utils.CUtils.never
import cn.jawbreakers.candycraftce.utils.ICPlatformDatagen.MinableLevel.*
import cn.jawbreakers.candycraftce.utils.ICPlatformDatagen.MinableType.*
import cn.jawbreakers.candycraftce.utils.registry.Entry
import cn.jawbreakers.candycraftce.utils.registry.LazyEntry
import net.minecraft.client.renderer.RenderType
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.Blocks.BEDROCK
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.state.properties.WoodType
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction
import java.util.function.Supplier
import java.util.stream.Stream

object CBlocks {
    init {
        CLogUtils.sign()
    }

    internal val blocks: LinkedHashMap<String, Entry<out Block>> = LinkedHashMap()
    private val noItem: MutableSet<Entry<out Block>> = mutableSetOf()
    val families: LinkedHashSet<BlockFamily> = LinkedHashSet()


    internal fun withItem(): Stream<Entry<out Block>> = blocks.values.stream().filter { it !in noItem }

    val custard_pudding_block = register("custard_pudding_block") {
        CustardPuddingBlock(pudding(MapColor.COLOR_PINK).randomTicks())
    }.cutout().minable(SHOVEL)

    val pudding_block = register("pudding_block", pudding(MapColor.SAND))
        .minable(SHOVEL)

    val pudding_farmland = register("pudding_farmland") {
        CandyFarmlandBlock(pudding(MapColor.SAND).randomTicks().dropsLike(pudding_block.get()))
    }.cutout().minable(SHOVEL)

    val sugar_sand = register("sugar_sand") {
        SugarSandBlock(properties(Blocks.SAND).mapColor(MapColor.SAND).strength(0.5F))
    }.minable(SHOVEL)

    val marshmallow_log = register("marshmallow_log") {
        RotatedPillarBlock(properties(Blocks.OAK_LOG).mapColor(MapColor.COLOR_PINK))
    }.minable(AXE)
    val dark_marshmallow_log = register("dark_marshmallow_log") {
        RotatedPillarBlock(properties(marshmallow_log.get()))
    }.minable(AXE)
    val light_marshmallow_log = register("light_marshmallow_log") {
        RotatedPillarBlock(properties(marshmallow_log.get()))
    }.minable(AXE)
    val stripped_marshmallow_log = register("stripped_marshmallow_log") {
        RotatedPillarBlock(properties(Blocks.STRIPPED_OAK_LOG).mapColor(MapColor.COLOR_PINK))
    }.minable(AXE)
    val stripped_dark_marshmallow_log = register("stripped_dark_marshmallow_log") {
        RotatedPillarBlock(properties(stripped_marshmallow_log.get()))
    }.minable(AXE)
    val stripped_light_marshmallow_log = register("stripped_light_marshmallow_log") {
        RotatedPillarBlock(properties(stripped_marshmallow_log.get()))
    }.minable(AXE)

    init {
        whenInitialized {
            CMixins.injectStrippable().apply {
                put(marshmallow_log.get(), stripped_marshmallow_log.get())
                put(light_marshmallow_log.get(), stripped_light_marshmallow_log.get())
                put(dark_marshmallow_log.get(), stripped_dark_marshmallow_log.get())
            }
        }
    }

    val marshmallow_planks = register("marshmallow_planks", marshmallow())
        .minable(AXE)

    val marshmallow_family = registerFamily(
        "marshmallow",
        marshmallow_planks,
        stairs = { properties(Blocks.OAK_STAIRS) },
        slab = { properties(Blocks.OAK_SLAB) },
        fence = { properties(Blocks.OAK_FENCE) },
        fenceGate = { properties(Blocks.OAK_FENCE_GATE) },
        door = { properties(Blocks.OAK_DOOR) },
        trapdoor = { properties(Blocks.OAK_TRAPDOOR) },
        sign = { properties(Blocks.OAK_SIGN) },
        woodType = WoodType.CHERRY,
        mapColor = MapColor.COLOR_PINK,
    ).minable(AXE)

    val light_marshmallow_planks = register("light_marshmallow_planks", marshmallow())
        .minable(AXE)
    val light_marshmallow_family = registerFamily(
        "light_marshmallow",
        light_marshmallow_planks,
        stairs = { properties(Blocks.BIRCH_STAIRS) },
        slab = { properties(Blocks.BIRCH_SLAB) },
        fence = { properties(Blocks.BIRCH_FENCE) },
        fenceGate = { properties(Blocks.BIRCH_FENCE_GATE) },
        door = { properties(Blocks.BIRCH_DOOR) },
        trapdoor = { properties(Blocks.BIRCH_TRAPDOOR) },
        sign = { properties(Blocks.BIRCH_SIGN) },
        woodType = WoodType.BIRCH,
        mapColor = MapColor.COLOR_PINK,
    ).minable(AXE)
    val dark_marshmallow_planks = register("dark_marshmallow_planks", marshmallow())
        .minable(AXE)
    val dark_marshmallow_family = registerFamily(
        "dark_marshmallow",
        dark_marshmallow_planks,
        stairs = { properties(Blocks.JUNGLE_STAIRS) },
        slab = { properties(Blocks.JUNGLE_SLAB) },
        fence = { properties(Blocks.JUNGLE_FENCE) },
        fenceGate = { properties(Blocks.JUNGLE_FENCE_GATE) },
        door = { properties(Blocks.JUNGLE_DOOR) },
        trapdoor = { properties(Blocks.JUNGLE_TRAPDOOR) },
        sign = { properties(Blocks.JUNGLE_SIGN) },
        woodType = WoodType.JUNGLE,
        mapColor = MapColor.COLOR_PINK,
    ).minable(AXE)

    val marshmallow_ladder = register("marshmallow_ladder") {
        LadderBlock(properties(Blocks.LADDER).mapColor(MapColor.COLOR_PINK))
    }.cutout().minable(AXE)


    val candy_cane_block = register("candy_cane_block", properties(Blocks.STONE_BRICKS).mapColor(MapColor.COLOR_RED))
        .minable(PICKAXE)
    val candy_cane_family = registerFamily(
        "candy_cane",
        candy_cane_block,
        stairs = { properties(Blocks.STONE_STAIRS) },
        slab = { properties(Blocks.STONE_BRICK_SLAB) },
        wall = { properties(Blocks.STONE_BRICK_WALL) },
        fence = { properties(Blocks.STONE_BRICKS) },
        mapColor = MapColor.COLOR_RED
    ).minable(PICKAXE)
    val sugar_block = register("sugar_block", properties(Blocks.SANDSTONE).mapColor(MapColor.SNOW))
        .minable(PICKAXE)
    val sugar_family = registerFamily(
        "sugar",
        sugar_block,
        stairs = { properties(Blocks.SANDSTONE_STAIRS) },
        slab = { properties(Blocks.SANDSTONE_SLAB) },
        wall = { properties(Blocks.SANDSTONE) },
        mapColor = MapColor.SNOW
    ).minable(PICKAXE)
    val sugar_bricks = register("sugar_bricks", properties(Blocks.SANDSTONE).mapColor(MapColor.SNOW))
        .minable(PICKAXE)
    val sugar_brick_family = registerFamily(
        "sugar_bricks",
        sugar_bricks,
        stairs = { properties(Blocks.SANDSTONE_STAIRS) },
        slab = { properties(Blocks.SANDSTONE_SLAB) },
        wall = { properties(Blocks.SANDSTONE) },
        mapColor = MapColor.SNOW
    ).minable(PICKAXE)
    val cotton_candy_block =
        register("cotton_candy_block", properties(Blocks.HAY_BLOCK).mapColor(MapColor.COLOR_PINK))

    val cotton_candy_family = registerFamily(
        "cotton_candy",
        cotton_candy_block,
        stairs = { properties(cotton_candy_block.get()) },
        slab = { properties(cotton_candy_block.get()) },
        mapColor = MapColor.COLOR_PINK
    )


    val caramel_block = register("caramel_block", stone(MapColor.COLOR_ORANGE))
        .minable(PICKAXE)
    val caramel_family = registerFamily(
        "caramel",
        caramel_block,
        stairs = { properties(caramel_block.get()) },
        slab = { properties(caramel_block.get()) },
        mapColor = MapColor.COLOR_ORANGE
    ).minable(PICKAXE)
    val caramel_bricks = register("caramel_bricks", stone(MapColor.COLOR_ORANGE))
        .minable(PICKAXE)
    val caramel_bricks_family = registerFamily(
        "caramel_bricks",
        caramel_bricks,
        stairs = { properties(caramel_bricks.get()) },
        slab = { properties(caramel_bricks.get()) },
        mapColor = MapColor.COLOR_ORANGE
    ).minable(PICKAXE)

    val licorice_block = register("licorice_block", stone(MapColor.COLOR_BLACK))
        .minable(PICKAXE, STONE)
    val licorice_family = registerFamily(
        "licorice",
        licorice_block,
        stairs = { properties(licorice_block.get()) },
        slab = { properties(licorice_block.get()) },
        mapColor = MapColor.COLOR_BLACK
    ).minable(PICKAXE, STONE)
    val licorice_bricks = register("licorice_bricks", stone(MapColor.COLOR_BLACK))
        .minable(PICKAXE, STONE)
    val licorice_brick_family = registerFamily(
        "licorice_bricks",
        licorice_bricks,
        stairs = { properties(licorice_bricks.get()) },
        slab = { properties(licorice_bricks.get()) },
        mapColor = MapColor.COLOR_BLACK
    ).minable(PICKAXE, STONE)

    val nougat_block = register("nougat_block", iron(MapColor.COLOR_BROWN))
        .minable(PICKAXE, IRON)
    val chiseled_nougat_block = register("chiseled_nougat_block", iron(MapColor.COLOR_BROWN))
        .minable(PICKAXE, IRON)
    val square_pattern_nougat_block = register("square_pattern_nougat_block", iron(MapColor.COLOR_BROWN))
        .minable(PICKAXE, IRON)
    val nougat_head = register("nougat_head") { NougatHeadBlock(iron(MapColor.COLOR_BROWN)) }
        .minable(PICKAXE, IRON)
    val nougat_family = registerFamily(
        "nougat",
        nougat_block,
        stairs = { properties(nougat_block.get()) },
        slab = { properties(nougat_block.get()) },
        mapColor = MapColor.COLOR_BROWN
    ).minable(PICKAXE, IRON)

    //glass
    val caramel_glass = register("caramel_glass") {
        GlassBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_ORANGE).strength(0.3F))
    }.transparent()
    val caramel_glass_round = register("caramel_glass_round") {
        GlassBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_ORANGE).strength(0.5F))
    }.transparent()
    val caramel_glass_diamond = register("caramel_glass_diamond") {
        GlassBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_ORANGE).strength(0.7F))
    }.transparent()
    val caramel_pane = register("caramel_pane") {
        IronBarsBlock(properties(Blocks.GLASS_PANE).mapColor(MapColor.COLOR_ORANGE).strength(0.3F))
    }.transparent()
    val caramel_pane_round = register("caramel_pane_round") {
        IronBarsBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_ORANGE).strength(0.5F))
    }.transparent()
    val caramel_pane_diamond = register("caramel_pane_diamond") {
        IronBarsBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_ORANGE).strength(0.7F))
    }.transparent()
    val dark_caramel_glass = register("dark_caramel_glass") {
        GlassBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_BROWN).strength(0.3F))
    }.transparent()
    val dark_caramel_glass_round = register("dark_caramel_glass_round") {
        GlassBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_BROWN).strength(0.5F))
    }.transparent()
    val dark_caramel_glass_diamond = register("dark_caramel_glass_diamond") {
        GlassBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_BROWN).strength(0.7F))
    }.transparent()
    val dark_caramel_pane = register("dark_caramel_pane") {
        IronBarsBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_BROWN).strength(0.3F))
    }.transparent()
    val dark_caramel_pane_round = register("dark_caramel_pane_round") {
        IronBarsBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_BROWN).strength(0.5F))
    }.transparent()
    val dark_caramel_pane_diamond = register("dark_caramel_pane_diamond") {
        IronBarsBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_BROWN).strength(0.7F))
    }.transparent()
    val honey_glass = register("honey_glass") {
        GlassBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_YELLOW).strength(0.3F))
    }.transparent()
    val honey_glass_round = register("honey_glass_round") {
        GlassBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_YELLOW).strength(0.5F))
    }.transparent()
    val honey_glass_diamond = register("honey_glass_diamond") {
        GlassBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_YELLOW).strength(0.7F))
    }.transparent()
    val honey_pane = register("honey_pane") {
        IronBarsBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_YELLOW).strength(0.3F))
    }.transparent()
    val honey_pane_round = register("honey_pane_round") {
        IronBarsBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_YELLOW).strength(0.5F))
    }.transparent()
    val honey_pane_diamond = register("honey_pane_diamond") {
        IronBarsBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_YELLOW).strength(0.7F))
    }.transparent()
    val sugar_glass = register("sugar_glass") {
        GlassBlock(properties(Blocks.GLASS).mapColor(MapColor.SNOW).strength(0.3F))
    }.transparent()
    val sugar_glass_round = register("sugar_glass_round") {
        GlassBlock(properties(Blocks.GLASS).mapColor(MapColor.SNOW).strength(0.5F))
    }.transparent()
    val sugar_glass_diamond = register("sugar_glass_diamond") {
        GlassBlock(properties(Blocks.GLASS).mapColor(MapColor.SNOW).strength(0.7F))
    }.transparent()
    val sugar_pane = register("sugar_pane") {
        IronBarsBlock(properties(Blocks.GLASS).mapColor(MapColor.SNOW).strength(0.3F))
    }.transparent()
    val sugar_pane_round = register("sugar_pane_round") {
        IronBarsBlock(properties(Blocks.GLASS).mapColor(MapColor.SNOW).strength(0.5F))
    }.transparent()
    val sugar_pane_diamond = register("sugar_pane_diamond") {
        IronBarsBlock(properties(Blocks.GLASS).mapColor(MapColor.SNOW).strength(0.7F))
    }.transparent()
    val grenadine_glass = register("grenadine_glass") {
        GlassBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_RED).strength(0.3F))
    }.transparent()
    val grenadine_glass_grid = register("grenadine_glass_grid") {
        GlassBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_RED).strength(0.5F))
    }.transparent()
    val grenadine_pane = register("grenadine_pane") {
        IronBarsBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_RED).strength(0.3F))
    }.transparent()
    val grenadine_pane_grid = register("grenadine_pane_grid") {
        IronBarsBlock(properties(Blocks.GLASS).mapColor(MapColor.COLOR_RED).strength(0.5F))
    }.transparent()

    //glass conclusion
    val glassTypes = listOf(
        listOf(
            caramel_glass to caramel_pane,
            caramel_glass_round to caramel_pane_round,
            caramel_glass_diamond to caramel_pane_diamond,
        ),
        listOf(
            honey_glass to honey_pane,
            honey_glass_round to honey_pane_round,
            honey_glass_diamond to honey_pane_diamond,
        ),
        listOf(
            sugar_glass to sugar_pane,
            sugar_glass_round to sugar_pane_round,
            sugar_glass_diamond to sugar_pane_diamond,
        ),
        listOf(
            dark_caramel_glass to dark_caramel_pane,
            dark_caramel_glass_round to dark_caramel_pane_round,
            dark_caramel_glass_diamond to dark_caramel_pane_diamond,
        ),
        listOf(
            grenadine_glass to grenadine_pane,
            grenadine_glass_grid to grenadine_pane_grid,
        )
    )

    //ice_cream
    val ice_cream = register("ice_cream", iceCream(MapColor.SNOW))
    val ice_cream_family = registerFamily(
        "ice_cream",
        ice_cream,
        stairs = { properties(ice_cream.get()) },
        slab = { properties(ice_cream.get()) },
        mapColor = MapColor.SNOW,
    )
    val strawberry_ice_cream = register("strawberry_ice_cream", iceCream(MapColor.COLOR_RED))
        .minable(SHOVEL)
    val strawberry_ice_cream_family = registerFamily(
        "strawberry_ice_cream",
        strawberry_ice_cream,
        stairs = { properties(strawberry_ice_cream.get()) },
        slab = { properties(strawberry_ice_cream.get()) },
        mapColor = MapColor.COLOR_RED,
    ).minable(SHOVEL)
    val mint_ice_cream = register("mint_ice_cream", iceCream(MapColor.COLOR_LIGHT_GREEN))
        .minable(SHOVEL)
    val mint_ice_cream_family = registerFamily(
        "mint_ice_cream",
        mint_ice_cream,
        stairs = { properties(mint_ice_cream.get()) },
        slab = { properties(mint_ice_cream.get()) },
        mapColor = MapColor.COLOR_LIGHT_GREEN,
    ).minable(SHOVEL)
    val blueberry_ice_cream = register("blueberry_ice_cream", iceCream(MapColor.COLOR_BLUE))
        .minable(SHOVEL)
    val blueberry_ice_cream_family = registerFamily(
        "blueberry_ice_cream",
        blueberry_ice_cream,
        stairs = { properties(blueberry_ice_cream.get()) },
        slab = { properties(blueberry_ice_cream.get()) },
        mapColor = MapColor.COLOR_BLUE,
    ).minable(SHOVEL)
    val chocolate_ice_cream = register("chocolate_ice_cream", iceCream(MapColor.COLOR_BROWN))
        .minable(SHOVEL)
    val chocolate_ice_cream_family = registerFamily(
        "chocolate_ice_cream",
        chocolate_ice_cream,
        stairs = { properties(chocolate_ice_cream.get()) },
        slab = { properties(chocolate_ice_cream.get()) },
        mapColor = MapColor.COLOR_BROWN,
    ).minable(SHOVEL)
    val banana_ice_cream = register("banana_ice_cream", iceCream(MapColor.COLOR_YELLOW))
        .minable(SHOVEL)
    val banana_ice_cream_family = registerFamily(
        "banana_ice_cream",
        banana_ice_cream,
        stairs = { properties(banana_ice_cream.get()) },
        slab = { properties(banana_ice_cream.get()) },
        mapColor = MapColor.COLOR_YELLOW,
    ).minable(SHOVEL)
    val sugar_spikes = register("sugar_spikes") {
        SpikesBlock(properties().mapColor(MapColor.TERRACOTTA_WHITE).noCollission(), 4)
    }.cutout().minable(PICKAXE)
    val cranberry_spikes = register("cranberry_spikes") {
        SpikesBlock(properties(sugar_spikes.get()).mapColor(MapColor.TERRACOTTA_RED), 2, true)
    }.cutout().minable(PICKAXE)
    val cotton_candy_web = register("cotton_candy_web") {
        WebBlock(properties(Blocks.COBWEB))
    }.cutout()
    val marshmallow_rope = register("marshmallow_rope") {
        MarshmallowRopeBlock(properties(marshmallow_ladder.get()))
    }.cutout()
    val trampojelly = register("trampojelly") {
        JellyBlock(JellyType.GREEN, hardJelly(MapColor.COLOR_GREEN))
    }.transparent()
    val purple_trampojelly = register("purple_trampojelly") {
        JellyBlock(JellyType.PURPLE, hardJelly(MapColor.COLOR_PURPLE).lightLevel { _ -> 13 })
    }.transparent()
    val red_trampojelly = register("red_trampojelly") {
        JellyBlock(JellyType.RED, hardJelly(MapColor.COLOR_RED))
    }.transparent()
    val yellow_trampojelly = register("yellow_trampojelly") {
        JellyBlock(JellyType.YELLOW, hardJelly(MapColor.COLOR_YELLOW))
    }.transparent()
    val jelly_shock_absorber = register("jelly_shock_absorber") {
        JellyBlock(JellyType.BLUE, hardJelly(MapColor.COLOR_LIGHT_BLUE))
    }.transparent()
    val grenadine_ice = register("grenadine_ice") {
        IceBlock(properties(Blocks.ICE).mapColor(MapColor.COLOR_RED))
    }.transparent().minable(PICKAXE)

    val fragile_grenadine_ice = register("fragile_grenadine_ice") {
        FragileGrenadineIce(properties(Blocks.ICE).mapColor(MapColor.COLOR_RED).noLootTable())
    }.transparent().noSimpleItem().minable(PICKAXE)
    val banana_block = register("banana_block", hay(MapColor.COLOR_YELLOW))
        .minable(HOE)
    val chewing_gum_block = register("chewing_gum_block") {
        ChewingGumBlock(jelly(MapColor.COLOR_PINK).strength(1f))
    }.minable(HOE)
    val candied_cherry_sack = register("candied_cherry_sack", hay(MapColor.COLOR_RED))
        .minable(HOE)
    val mint_block = register("mint_block", hay(MapColor.COLOR_LIGHT_GREEN))
        .minable(HOE)
    val mint_family = registerFamily(
        "mint", mint_block,
        door = { properties(mint_block.get()) },
        setType = BlockSetType.OAK,
        mapColor = MapColor.COLOR_LIGHT_GREEN,
    ).minable(HOE)
    val honey_lantern = register("honey_lantern") {
        LanternBlock(properties(Blocks.LANTERN).mapColor(MapColor.COLOR_YELLOW))
    }.cutout().minable(PICKAXE)
    val marshmallow_lantern = register("marshmallow_lantern") {
        LanternBlock(properties(Blocks.LANTERN).mapColor(MapColor.COLOR_PINK))
    }.cutout().minable(PICKAXE)
    val honey_torch = register("honey_torch") {
        LazyParticleTorchBlock(properties(Blocks.TORCH), CParticleTypes.liquid_candy_flame)
    }.cutout().noSimpleItem()
    val honey_wall_torch = register("honey_wall_torch") {
        LazyParticleTorchBlock.Wall(
            properties(Blocks.WALL_TORCH).dropsLike(honey_torch.get()),
            CParticleTypes.liquid_candy_flame
        )
    }.cutout().noSimpleItem()
    val raspberry_block = register("raspberry_block", hay(MapColor.COLOR_RED))
        .minable(HOE)
    val honey_lamp = register(
        "honey_lamp",
        iron(MapColor.COLOR_YELLOW).strength(1.0F).sound(SoundType.GLASS).lightLevel { 15 }
    ).minable(PICKAXE)
    val honeycomb_block = register("honeycomb_block", stone(MapColor.COLOR_YELLOW))
        .minable(PICKAXE)
    val pez_block = register("pez_block", iron(MapColor.COLOR_RED).strength(5.0F, 10.0F))
        .minable(PICKAXE)
    val jawbreaker_block = register("jawbreaker_block", properties(BEDROCK).noLootTable())
    val jawbreaker_light = register("jawbreaker_light", properties(BEDROCK).lightLevel { 15 }.noLootTable())
    val jelly_sentry_key_hole = register("jelly_sentry_key_hole") {
        DungeonLockBlock(CItems.jelly_sentry_key, properties(BEDROCK).noLootTable())
    }
    val jelly_boss_key_hole = register("jelly_boss_key_hole") {
        DungeonLockBlock(CItems.jelly_boss_key, properties(BEDROCK).noLootTable())
    }

    val chocolate_stone = register("chocolate_stone", stone(MapColor.COLOR_BROWN))
        .minable(PICKAXE)
    val chocolate_stone_family = registerFamily(
        "chocolate_stone",
        chocolate_stone,
        stairs = { properties(chocolate_stone.get()) },
        slab = { properties(chocolate_stone.get()) },
        mapColor = MapColor.COLOR_BROWN,
    ).minable(PICKAXE)
    val chocolate_bricks = register("chocolate_bricks", stone(MapColor.COLOR_BROWN))
        .minable(PICKAXE)
    val chocolate_cobblestone = register("chocolate_cobblestone", stone(MapColor.COLOR_BROWN))
        .minable(PICKAXE)
    val chocolate_cobblestone_family = registerFamily(
        "chocolate_cobblestone",
        chocolate_cobblestone,
        stairs = { properties(chocolate_cobblestone.get()) },
        slab = { properties(chocolate_cobblestone.get()) },
        wall = { properties(chocolate_cobblestone.get()) },
        mapColor = MapColor.COLOR_BROWN,
    ).minable(PICKAXE)
    val white_chocolate_stone = register("white_chocolate_stone", stone(MapColor.TERRACOTTA_WHITE))
        .minable(PICKAXE)
    val white_chocolate_bricks = register("white_chocolate_bricks", stone(MapColor.COLOR_BROWN))
        .minable(PICKAXE)
    val white_chocolate_stone_family = registerFamily(
        "white_chocolate_stone",
        white_chocolate_stone,
        stairs = { properties(white_chocolate_stone.get()) },
        slab = { properties(white_chocolate_stone.get()) },
        mapColor = MapColor.TERRACOTTA_WHITE,
    ).minable(PICKAXE)
    val white_chocolate_cobblestone = register("white_chocolate_cobblestone", stone(MapColor.COLOR_BROWN))
        .minable(PICKAXE)
    val white_chocolate_cobblestone_family = registerFamily(
        "white_chocolate_cobblestone",
        white_chocolate_cobblestone,
        stairs = { properties(white_chocolate_cobblestone.get()) },
        slab = { properties(white_chocolate_cobblestone.get()) },
        wall = { properties(white_chocolate_cobblestone.get()) },
        mapColor = MapColor.TERRACOTTA_WHITE,
    ).minable(PICKAXE)


    val licorice_ore = register("licorice_ore", stone(MapColor.TERRACOTTA_BROWN))
        .minable(PICKAXE, STONE)
    val honey_ore = register("honey_ore") {
        DropExperienceBlock(stone(MapColor.TERRACOTTA_BROWN).strength(3.0F, 5.0F), UniformInt.of(0, 2))
    }.minable(PICKAXE)
    val pez_ore = register("pez_ore", stone(MapColor.TERRACOTTA_BROWN).strength(3.0F, 5.0F))
        .minable(PICKAXE, DIAMOND)
    val jelly_ore = register("jelly_ore", stone(MapColor.TERRACOTTA_BROWN))
        .minable(PICKAXE, IRON)
    val nougat_ore = register("nougat_ore") {
        DropExperienceBlock(stone(MapColor.TERRACOTTA_BROWN).strength(3.0F, 5.0F), UniformInt.of(1, 2))
    }.minable(PICKAXE, IRON)

    val white_licorice_ore = register("white_licorice_ore", stone(MapColor.TERRACOTTA_WHITE))
        .minable(PICKAXE, STONE)
    val white_honey_ore = register("white_honey_ore") {
        DropExperienceBlock(stone(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F), UniformInt.of(0, 2))
    }.minable(PICKAXE)
    val white_pez_ore = register("white_pez_ore", stone(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F))
        .minable(PICKAXE, DIAMOND)
    val white_jelly_ore = register("white_jelly_ore", stone(MapColor.TERRACOTTA_WHITE))
        .minable(PICKAXE, IRON)
    val white_nougat_ore = register("white_nougat_ore") {
        DropExperienceBlock(stone(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F), UniformInt.of(1, 2))
    }.minable(PICKAXE, IRON)
    val sugar_factory = register("sugar_factory") {
        SugarFactoryBlock(properties(candy_cane_block.get()))
    }.minable(PICKAXE)
    val advanced_sugar_factory = register("advanced_sugar_factory") {
        AdvancedSugarFactoryBlock(properties(sugar_factory.get()))
    }.minable(PICKAXE)

    val chocolate_furnace = register("chocolate_furnace") {
        ChocolateFurnaceBlock(false, properties(chocolate_stone.get()))
    }.minable(PICKAXE)
    val white_chocolate_furnace = register("white_chocolate_furnace") {
        ChocolateFurnaceBlock(true, properties(white_chocolate_stone.get()))
    }.minable(PICKAXE)
    val licorice_furnace = register("licorice_furnace") {
        LicoriceFurnaceBlock(properties(licorice_block.get()))
    }.minable(PICKAXE, STONE)

    //plants
    val sweet_grass_pink = register("sweet_grass_pink") { CandyPlantBlock(plant(MapColor.COLOR_PINK)) }
        .cutout()
    val sweet_grass_pale = register("sweet_grass_pale") { CandyPlantBlock(plant(MapColor.COLOR_LIGHT_GRAY)) }
        .cutout()
    val sweet_grass_yellow = register("sweet_grass_yellow") { CandyPlantBlock(plant(MapColor.COLOR_YELLOW)) }
        .cutout()
    val sweet_grass_red = register("sweet_grass_red") { CandyPlantBlock(plant(MapColor.COLOR_RED)) }
        .cutout()

    val chocolate_leaves = register("chocolate_leaves") { CandyLeavesBlock(leaves(MapColor.COLOR_BROWN)) }
        .cutout()
    val white_chocolate_leaves =
        register("white_chocolate_leaves") { CandyLeavesBlock(leaves(MapColor.TERRACOTTA_WHITE)) }
            .cutout()
    val candied_cherry_leaves = register("candied_cherry_leaves") { CandyLeavesBlock(leaves(MapColor.COLOR_RED)) }
        .cutout()
    val caramel_leaves = register("caramel_leaves") { CandyLeavesBlock(leaves(MapColor.COLOR_ORANGE)) }
        .cutout()
    val enchant_candy_leaves = register("enchant_candy_leaves") { CandyLeavesBlock(leaves(MapColor.COLOR_PURPLE)) }
        .cutout()

    val chocolate_sapling = register("chocolate_sapling") {
        CandySaplingBlock(null, plant(MapColor.COLOR_BROWN))
    }.cutout()
    val caramel_sapling = register("caramel_sapling") {
        CandySaplingBlock(null, plant(MapColor.COLOR_ORANGE))
    }.cutout()
    val white_chocolate_sapling = register("white_chocolate_sapling") {
        CandySaplingBlock(null, plant(MapColor.TERRACOTTA_WHITE))
    }.cutout()
    val candied_cherry_sapling = register("candied_cherry_sapling") {
        CandySaplingBlock(null, plant(MapColor.COLOR_RED))
    }.cutout()

    val fraise_tagada_flower = register("fraise_tagada_flower") {
        CandyPlantBlock(plant(MapColor.COLOR_PINK))
    }.cutout()
    val acid_mint_flower = register("acid_mint_flower") {
        AcidMintFlowerBlock(plant(MapColor.GRASS))
    }.cutout()
    val sugar_essence_flower = register("sugar_essence_flower") {
        IceCandyPlantBlock(plant(MapColor.GOLD))
    }.cutout()

    val chewing_gum_puddle = register("chewing_gum_puddle") {
        ChewingGumPuddleBlock(jelly(MapColor.COLOR_PURPLE).strength(1.0f).noCollission().noOcclusion())
    }.cutout()
    val marshmallow_slice = register("marshmallow_slice") {
        CandyWaterlilyBlock(false, properties(Blocks.LILY_PAD).mapColor(MapColor.COLOR_PINK).randomTicks())
    }.cutout()
    val marshmallow_slice_flower = register("marshmallow_slice_flower") {
        CandyWaterlilyBlock(true, properties(marshmallow_slice.get()))
    }.cutout()
    val rope_raspberry = register("rope_raspberry") { SeaweedBlock(true, plant(MapColor.TERRACOTTA_RED)) }
        .cutout()
    val mint = register("mint") { SeaweedBlock(false, plant(MapColor.COLOR_GREEN)) }
        .cutout()
    val banana_seaweed = register("banana_seaweed") { SeaweedBlock(false, plant(MapColor.COLOR_YELLOW)) }
        .cutout()

    val lollipop_fruit = register("lollipop_fruit") {
        LollipopBlock(plant(MapColor.COLOR_PINK))
    }.cutout()
    val lollipop_stem = register("lollipop_stem") {
        LollipopStemBlock(plant(MapColor.COLOR_PINK).randomTicks())
    }.noSimpleItem().cutout()
    val dragibus_crops = register("dragibus_crops") {
        DragibusCropsBlock(plant(MapColor.COLOR_RED).randomTicks())
    }.cutout().noSimpleItem()

    val caramel_portal = register("caramel_portal") {
        CaramelPortalBlock(properties(Blocks.NETHER_PORTAL).noLootTable().mapColor(MapColor.COLOR_ORANGE))
    }.transparent().noSimpleItem()

    val caramel_jelly_block = register("caramel_jelly_block") {
        PlacedCutableJellyBlock(jelly(MapColor.COLOR_ORANGE), CItems.caramel_jelly_slice)
    }.transparent().noSimpleItem()
    val strawberry_jelly_block = register("strawberry_jelly_block") {
        PlacedCutableJellyBlock(jelly(MapColor.COLOR_ORANGE), CItems.strawberry_jelly_slice)
    }.transparent().noSimpleItem()
    val royal_rations_block = register("royal_rations_block") {
        PlacedCutableJellyBlock(jelly(MapColor.SNOW), CItems.royal_rations_slice)
    }.transparent().noSimpleItem()
    val lemon_jelly_block = register("lemon_jelly_block") {
        PlacedJellyBlock(jelly(MapColor.COLOR_YELLOW))
    }.transparent().noSimpleItem()
    val raspberry_jelly_block = register("raspberry_jelly_block") {
        PlacedJellyBlock(jelly(MapColor.COLOR_RED))
    }.transparent().noSimpleItem()
    val mint_jelly_block = register("mint_jelly_block") {
        PlacedJellyBlock(jelly(MapColor.COLOR_GREEN))
    }.transparent().noSimpleItem()

    val caramel = CFluids.caramel.block
    val grenadine = CFluids.grenadine.block
    val static_water = register("static_water") {
        StaticWaterBlock(properties(Blocks.WATER).noLootTable())
    }.noSimpleItem()

    //======================

    private fun properties(copyFrom: BlockBehaviour? = null) =
        if (copyFrom != null) Properties.copy(copyFrom) else Properties.of()

    private fun pudding(color: MapColor) = properties().sound(SoundType.WOOL).strength(0.6f).mapColor(color)
    private fun marshmallow(color: MapColor = MapColor.COLOR_PINK) = properties(Blocks.OAK_STAIRS).mapColor(color)
    private fun hay(color: MapColor) = properties(Blocks.HAY_BLOCK).mapColor(color)
    private fun plant(color: MapColor) = properties(Blocks.GRASS).mapColor(color)
    private fun stone(color: MapColor) = properties(Blocks.STONE).mapColor(color)
    private fun iron(color: MapColor) = properties(Blocks.IRON_BLOCK).mapColor(color)
    private fun iceCream(color: MapColor) = properties(Blocks.SNOW_BLOCK).mapColor(color)
    private fun leaves(color: MapColor) = properties(Blocks.OAK_LEAVES).mapColor(color)
    private fun jelly(color: MapColor) =
        properties(Blocks.SLIME_BLOCK).mapColor(color).strength(0.5f)/*todo sound .sound(CCSoundTypes.JELLY)*/
            .noOcclusion()

    private fun hardJelly(color: MapColor) = jelly(color).strength(2.5F, 1000.0F)

    //=================================
    fun register(name: String, properties: Properties = properties()): Entry<Block> =
        register(name) { Block(properties) }

    fun register(name: String, propertiesProvider: Entry<out Block>): Entry<Block> =
        register(name) { Block(properties(propertiesProvider.get())) }

    private fun <B : Block> register(name: String, factory: Supplier<B>): Entry<B> {
        val block = CPlatformUtils.registerBlock(name, factory)
        blocks[name] = block
        return block
    }

    /**
     * Register a block family.
     * @param woodType required if [fenceGate], [sign] notnull
     * @param setType required if [door] [trapdoor] notnull ,while optional if [woodType] is notnull
     * */
    private fun registerFamily(
        name: String,
        original: Entry<out Block>,
        stairs: (() -> Properties)? = null,
        slab: (() -> Properties)? = null,
        fence: (() -> Properties)? = null,
        fenceGate: (() -> Properties)? = null,
        wall: (() -> Properties)? = null,
        door: (() -> Properties)? = null,
        trapdoor: (() -> Properties)? = null,
        sign: (() -> Properties)? = null,
        woodType: WoodType? = null,//
        setType: BlockSetType? = woodType?.setType,
        mapColor: MapColor,
    ): BlockFamily {
        val woodType by lazy { woodType ?: throw RuntimeException("`woodType` is undefined.") }
        val setType by lazy { setType ?: throw RuntimeException("both `setType` and `woodType` is undefined.") }

        val stairs = stairs?.let {
            register("${name}_stairs") { StairBlock(original.get().defaultBlockState(), stairs().mapColor(mapColor)) }
        }
        val slab = slab?.let {
            register("${name}_slab") { SlabBlock(slab().mapColor(mapColor)) }
        }
        val wall = wall?.let {
            register("${name}_wall") { WallBlock(wall().forceSolidOn().mapColor(mapColor)) }
        }
        val fence = fence?.let {
            register("${name}_fence") { FenceBlock(fence().forceSolidOn().mapColor(mapColor)) }
        }
        val fenceGate = fenceGate?.let {
            register("${name}_fence_gate") { FenceGateBlock(fenceGate().mapColor(mapColor), woodType) }
        }
        val door = door?.let {
            register("${name}_door") {
                DoorBlock(
                    door().mapColor(mapColor).noOcclusion().pushReaction(PushReaction.DESTROY), setType
                )
            }
        }?.cutout()
        val trapdoor = trapdoor?.let {
            register("${name}_trapdoor") {
                TrapDoorBlock(
                    trapdoor().noOcclusion().isValidSpawn(::never).mapColor(mapColor), setType
                )
            }
        }?.cutout()
        val sign = sign?.let {
            Blocks.OAK_SIGN
            register("${name}_sign") {
                StandingSignBlock(
                    sign().forceSolidOn().noCollission().mapColor(mapColor),
                    woodType
                )
            }
        }
        //todo sign需要重定向BlockEntity see:CandyStandingSignBlock
        val wallSign = sign?.let {
            register("${name}_wall_sign") { WallSignBlock(sign().mapColor(mapColor).dropsLike(sign.get()), woodType) }
        }?.noSimpleItem()

        return BlockFamily(
            original, stairs, slab, wall, fence, fenceGate, door, trapdoor, sign, wallSign
        ).also { families.add(it) }
    }


    //===============Render Types==================
    private fun <B : Block> Entry<B>.cutout(mipped: Boolean = true) = apply {
        ifClient {
            setRenderLayer(this@cutout, if (mipped) RenderType.cutoutMipped() else RenderType.cutout())
        }
    }

    private fun <B : Block> Entry<B>.transparent() = apply {
        ifClient {
            setRenderLayer(this@transparent, RenderType.translucent())
        }
    }

    private fun <B : Block> Entry<B>.noSimpleItem() = apply { noItem.add(this) }

    //=================================
    fun Entry<out ItemLike>.asItemEntry() =
        LazyEntry(id) { get().asItem().takeIf { it != Items.AIR } ?: error("Item not found for $id") }

    fun Entry<out Block>.defaultBlockState(): BlockState = get().defaultBlockState()
    val Entry<out Block>.defaultInstance: ItemStack get() = asItem().defaultInstance
    fun <T : EntrySet<Block>> T.minable(
        type: ICPlatformDatagen.MinableType,
        level: ICPlatformDatagen.MinableLevel? = null,
    ) =
        apply {
            entries.forEach { it.minable(type, level) }
        }

    data class BlockFamily(
        val original: Entry<out Block>,
        val stairs: Entry<out StairBlock>?,
        val slab: Entry<out SlabBlock>?,
        val wall: Entry<out WallBlock>?,
        val fence: Entry<out FenceBlock>?,
        val fenceGate: Entry<out FenceGateBlock>?,
        val door: Entry<out DoorBlock>?,
        val trapdoor: Entry<out TrapDoorBlock>?,
        val sign: Entry<out StandingSignBlock>?,
        val wallSign: Entry<out WallSignBlock>?,
    ) : EntrySet<Block>(setOfNotNull(stairs, slab, wall, fence, fenceGate, door, trapdoor, sign, wallSign)) {
        init {
            if (sign == null && wallSign != null || sign != null && wallSign == null) {
                throw IllegalArgumentException("sign and wallSign must be both null or both not null")
            }
        }
    }
}
