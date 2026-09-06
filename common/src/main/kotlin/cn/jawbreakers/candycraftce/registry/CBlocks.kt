package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.block.*
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CMixins
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.setRenderLayer
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.whenInitialized
import cn.jawbreakers.candycraftce.utils.CUtils.never
import cn.jawbreakers.candycraftce.utils.IEntrySet
import cn.jawbreakers.candycraftce.utils.registry.Entry
import cn.jawbreakers.candycraftce.utils.registry.LazyEntry
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.*
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
    private val cutouts: MutableList<Entry<out Block>> = mutableListOf()
    private val transparent: MutableList<Entry<out Block>> = mutableListOf()
    private val noItem: MutableSet<Entry<out Block>> = mutableSetOf()
    internal val families: LinkedHashSet<BlockFamily> = LinkedHashSet()


    internal fun withItem(): Stream<Entry<out Block>> = blocks.values.stream().filter { it !in noItem }

    val custard_pudding_block = register("custard_pudding_block") {
        CustardPuddingBlock(pudding(MapColor.COLOR_PINK).randomTicks())
    }.cutout()

    val strawberry_filled_pudding = register("strawberry_filled_pudding", pudding(MapColor.COLOR_PINK))
        .cutout()

    val pudding_block = register("pudding_block", pudding(MapColor.SAND))

    val pudding_farmland = register("pudding_farmland") {
        CandyFarmlandBlock(pudding(MapColor.SAND))
    }.cutout()
    val sugar_sand = register("sugar_sand") {
        SugarSandBlock(properties(Blocks.SAND).mapColor(MapColor.SAND).strength(0.5F))
    }

    val marshmallow_log = register("marshmallow_log") {
        RotatedPillarBlock(properties(Blocks.OAK_LOG).mapColor(MapColor.COLOR_PINK))
    }
    val dark_marshmallow_log = register("dark_marshmallow_log") {
        RotatedPillarBlock(properties(marshmallow_log.get()))
    }
    val light_marshmallow_log = register("light_marshmallow_log") {
        RotatedPillarBlock(properties(marshmallow_log.get()))
    }
    val stripped_marshmallow_log = register("stripped_marshmallow_log") {
        RotatedPillarBlock(properties(Blocks.STRIPPED_OAK_LOG).mapColor(MapColor.COLOR_PINK))
    }
    val stripped_dark_marshmallow_log = register("stripped_dark_marshmallow_log") {
        RotatedPillarBlock(properties(stripped_marshmallow_log.get()))
    }
    val stripped_light_marshmallow_log = register("stripped_light_marshmallow_log") {
        RotatedPillarBlock(properties(stripped_marshmallow_log.get()))
    }

    init {
        whenInitialized {
            CMixins.addStrippables(marshmallow_log.get(), stripped_marshmallow_log.get())
            CMixins.addStrippables(light_marshmallow_log.get(), stripped_light_marshmallow_log.get())
            CMixins.addStrippables(dark_marshmallow_log.get(), stripped_dark_marshmallow_log.get())
        }
    }

    val marshmallow_planks = register("marshmallow_planks", marshmallow())
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
    )
    val light_marshmallow_planks = register("light_marshmallow_planks", marshmallow())
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
    )
    val dark_marshmallow_planks = register("dark_marshmallow_planks", marshmallow())
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
    )

    val marshmallow_ladder = register("marshmallow_ladder") {
        LadderBlock(properties(Blocks.LADDER).mapColor(MapColor.COLOR_PINK))
    }.cutout()


    val candy_cane_block = register("candy_cane_block", properties(Blocks.STONE_BRICKS).mapColor(MapColor.COLOR_RED))
    val candy_cane_family = registerFamily(
        "candy_cane",
        candy_cane_block,
        stairs = { properties(Blocks.STONE_STAIRS) },
        slab = { properties(Blocks.STONE_BRICK_SLAB) },
        wall = { properties(Blocks.STONE_BRICK_WALL) },
        fence = { properties(Blocks.STONE_BRICKS) },
        mapColor = MapColor.COLOR_RED
    )
    val sugar_block = register("sugar_block", properties(Blocks.SANDSTONE).mapColor(MapColor.SNOW))
    val sugar_family = registerFamily(
        "sugar",
        sugar_block,
        stairs = { properties(Blocks.SANDSTONE_STAIRS) },
        slab = { properties(Blocks.SANDSTONE_SLAB) },
        wall = { properties(Blocks.SANDSTONE) },
        mapColor = MapColor.SNOW
    )
    val sugar_brick = register("sugar_brick", properties(Blocks.SANDSTONE).mapColor(MapColor.SNOW))
    val sugar_brick_family = registerFamily(
        "sugar_brick",
        sugar_brick,
        stairs = { properties(Blocks.SANDSTONE_STAIRS) },
        slab = { properties(Blocks.SANDSTONE_SLAB) },
        wall = { properties(Blocks.SANDSTONE) },
        mapColor = MapColor.SNOW
    )

    val cotton_candy_block =
        register("cotton_candy_block", properties(Blocks.HAY_BLOCK).mapColor(MapColor.COLOR_PINK))
    val cotton_candy_family = registerFamily(
        "cotton_candy",
        cotton_candy_block,
        stairs = { properties(cotton_candy_block.get()) },
        slab = { properties(cotton_candy_block.get()) },
        mapColor = MapColor.COLOR_PINK
    )

    val raspberry_cotton_candy_block =
        register("raspberry_cotton_candy_block", properties(Blocks.HAY_BLOCK).mapColor(MapColor.TERRACOTTA_PINK))
    val raspberry_cotton_candy_family = registerFamily(
        "raspberry_cotton_candy",
        raspberry_cotton_candy_block,
        stairs = { properties(raspberry_cotton_candy_block.get()) },
        slab = { properties(raspberry_cotton_candy_block.get()) },
        mapColor = MapColor.COLOR_PINK
    )


    val crystallized_sugar = register("crystallized_sugar", stone(MapColor.TERRACOTTA_WHITE))
    val pink_crystallized_sugar = register("pink_crystallized_sugar", stone(MapColor.COLOR_PINK))

    val smooth_pink_sugar = register("smooth_pink_sugar", stone(MapColor.COLOR_PINK))
    val smooth_pink_sugar_family = registerFamily(
        "smooth_pink_sugar",
        smooth_pink_sugar,
        stairs = { properties(smooth_pink_sugar.get()) },
        slab = { properties(smooth_pink_sugar.get()) },
        mapColor = MapColor.COLOR_PINK
    )
    val pink_sugar_brick = register("pink_sugar_brick", stone(MapColor.COLOR_PINK))
    val pink_sugar_brick_family = registerFamily(
        "pink_sugar_brick",
        pink_sugar_brick,
        stairs = { properties(pink_sugar_brick.get()) },
        slab = { properties(pink_sugar_brick.get()) },
        mapColor = MapColor.COLOR_PINK
    )

    val caramel_block = register("caramel_block", stone(MapColor.COLOR_ORANGE))
    val caramel_family = registerFamily(
        "caramel",
        caramel_block,
        stairs = { properties(caramel_block.get()) },
        slab = { properties(caramel_block.get()) },
        mapColor = MapColor.COLOR_ORANGE
    )
    val caramel_brick = register("caramel_brick", stone(MapColor.COLOR_ORANGE))
    val caramel_brick_family = registerFamily(
        "caramel_brick",
        caramel_brick,
        stairs = { properties(caramel_brick.get()) },
        slab = { properties(caramel_brick.get()) },
        mapColor = MapColor.COLOR_ORANGE
    )

    val cookie_block = register("cookie_block", cookie())
    val cookie_family = registerFamily(
        "cookie",
        cookie_block,
        stairs = { properties(cookie_block.get()) },
        slab = { properties(cookie_block.get()) },
        mapColor = MapColor.TERRACOTTA_ORANGE
    )
    val waffle_block = register("waffle_block", cookie())
    val wafer_cone_block = register("wafer_cone_block", cookie())
    val solid_wafer_block = register("solid_wafer_block", cookie(MapColor.TERRACOTTA_ORANGE))

    val licorice_block = register("licorice_block", stone(MapColor.COLOR_BLACK))
    val licorice_family = registerFamily(
        "licorice",
        licorice_block,
        stairs = { properties(licorice_block.get()) },
        slab = { properties(licorice_block.get()) },
        mapColor = MapColor.COLOR_BLACK
    )
    val licorice_brick = register("licorice_brick", stone(MapColor.COLOR_BLACK))
    val licorice_brick_family = registerFamily(
        "licorice_brick",
        licorice_brick,
        stairs = { properties(licorice_brick.get()) },
        slab = { properties(licorice_brick.get()) },
        mapColor = MapColor.COLOR_BLACK
    )
    val nougat_block = register("nougat_block", iron(MapColor.COLOR_BROWN))
    val chiseled_nougat_block = register("chiseled_nougat_block", iron(MapColor.COLOR_BROWN))
    val square_pattern_nougat_block = register("square_pattern_nougat_block", iron(MapColor.COLOR_BROWN))
    val nougat_head = register("nougat_head") { NougatHeadBlock(iron(MapColor.COLOR_BROWN)) }
    val nougat_family = registerFamily(
        "nougat",
        nougat_block,
        stairs = { properties(nougat_block.get()) },
        slab = { properties(nougat_block.get()) },
        mapColor = MapColor.COLOR_BROWN
    )
    val wafer_stick_block = register("wafer_stick_block") {
        WaferStickBlock(marshmallow(MapColor.TERRACOTTA_ORANGE).noOcclusion())
    }
    val milk_chocolate_block = register("milk_chocolate_block", pudding(MapColor.COLOR_BROWN))
    val milk_chocolate_family = registerFamily(
        "milk_chocolate",
        milk_chocolate_block,
        stairs = { properties(milk_chocolate_block.get()) },
        slab = { properties(milk_chocolate_block.get()) },
        sign = { properties(milk_chocolate_block.get()) },
        door = { properties(milk_chocolate_block.get()) },
        trapdoor = { properties(milk_chocolate_block.get()) },
        mapColor = MapColor.COLOR_BROWN,
        woodType = WoodType.OAK
    )
    val milk_chocolate_brick = register("milk_chocolate_brick", pudding(MapColor.COLOR_BROWN))
    val milk_chocolate_brick_family = registerFamily(
        "milk_chocolate_brick",
        milk_chocolate_brick,
        stairs = { properties(milk_chocolate_brick.get()) },
        slab = { properties(milk_chocolate_brick.get()) },
        mapColor = MapColor.COLOR_BROWN,
    )
    val milk_chocolate_bar_block = register("milk_chocolate_bar_block") {
        ChocolateBarBlock(chocolate(MapColor.COLOR_BROWN).strength(0.7F).noOcclusion())
    }
    val white_chocolate_block = register("white_chocolate_block", pudding(MapColor.TERRACOTTA_WHITE))
    val white_chocolate_family = registerFamily(
        "white_chocolate",
        white_chocolate_block,
        stairs = { properties(white_chocolate_block.get()) },
        slab = { properties(white_chocolate_block.get()) },
        sign = { properties(white_chocolate_block.get()) },
        door = { properties(white_chocolate_block.get()) },
        trapdoor = { properties(white_chocolate_block.get()) },
        mapColor = MapColor.TERRACOTTA_WHITE,
        woodType = WoodType.BIRCH,
    )
    val white_chocolate_brick = register("white_chocolate_brick", pudding(MapColor.TERRACOTTA_WHITE))
    val white_chocolate_brick_family = registerFamily(
        "white_chocolate_brick",
        white_chocolate_brick,
        stairs = { properties(white_chocolate_brick.get()) },
        slab = { properties(white_chocolate_brick.get()) },
        mapColor = MapColor.TERRACOTTA_WHITE,
    )
    val white_chocolate_bar_block = register("white_chocolate_bar_block") {
        ChocolateBarBlock(properties(milk_chocolate_bar_block.get()).mapColor(MapColor.TERRACOTTA_WHITE))
    }
    val dark_chocolate_block = register("dark_chocolate_block", pudding(MapColor.COLOR_BLACK))
    val dark_chocolate_family = registerFamily(
        "dark_chocolate",
        dark_chocolate_block,
        stairs = { properties(dark_chocolate_block.get()) },
        slab = { properties(dark_chocolate_block.get()) },
        sign = { properties(dark_chocolate_block.get()) },
        door = { properties(dark_chocolate_block.get()) },
        trapdoor = { properties(dark_chocolate_block.get()) },
        mapColor = MapColor.COLOR_BLACK,
        woodType = WoodType.JUNGLE
    )
    val dark_chocolate_brick = register("dark_chocolate_brick", pudding(MapColor.COLOR_BLACK))
    val dark_chocolate_brick_family = registerFamily(
        "dark_chocolate_brick",
        dark_chocolate_brick,
        stairs = { properties(dark_chocolate_brick.get()) },
        slab = { properties(dark_chocolate_brick.get()) },
        mapColor = MapColor.COLOR_BLACK,
    )
    val dark_chocolate_bar_block = register("dark_chocolate_bar_block") {
        ChocolateBarBlock(properties(milk_chocolate_bar_block.get()).mapColor(MapColor.COLOR_BLACK))
    }

    //gummy
    val red_gummy_family = registerGummyFamily("red", MapColor.COLOR_RED)
    val orange_gummy_family = registerGummyFamily("orange", MapColor.COLOR_ORANGE)
    val yellow_gummy_family = registerGummyFamily("yellow", MapColor.COLOR_YELLOW)
    val white_gummy_family = registerGummyFamily("white", MapColor.TERRACOTTA_WHITE)
    val green_gummy_family = registerGummyFamily("green", MapColor.COLOR_GREEN)

    //hard_candy
    val white_hard_candy_block = register("white_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.TERRACOTTA_WHITE))
    }
    val red_hard_candy_block = register("red_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.COLOR_RED))
    }
    val green_hard_candy_block = register("green_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.COLOR_GREEN))
    }
    val yellow_hard_candy_block = register("yellow_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.COLOR_YELLOW))
    }
    val orange_hard_candy_block = register("orange_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.COLOR_ORANGE))
    }
    val light_blue_hard_candy_block = register("light_blue_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.COLOR_LIGHT_BLUE))
    }
    val pink_hard_candy_block = register("pink_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.COLOR_PINK))
    }
    val purple_hard_candy_block = register("purple_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.COLOR_PURPLE))
    }
    val white_red_hard_candy_block = register("white_red_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.COLOR_RED))
    }
    val white_green_hard_candy_block = register("white_green_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.COLOR_GREEN))
    }
    val white_yellow_hard_candy_block = register("white_yellow_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.COLOR_YELLOW))
    }
    val white_orange_hard_candy_block = register("white_orange_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.COLOR_ORANGE))
    }
    val white_light_blue_hard_candy_block = register("white_light_blue_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.COLOR_LIGHT_BLUE))
    }
    val white_pink_hard_candy_block = register("white_pink_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.COLOR_PINK))
    }
    val white_purple_hard_candy_block = register("white_purple_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.COLOR_PURPLE))
    }
    val red_green_hard_candy_block = register("red_green_hard_candy_block") {
        RotatedPillarBlock(hardCandy(MapColor.COLOR_RED))
    }


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
    val strawberry_ice_cream_family = registerFamily(
        "strawberry_ice_cream",
        strawberry_ice_cream,
        stairs = { properties(strawberry_ice_cream.get()) },
        slab = { properties(strawberry_ice_cream.get()) },
        mapColor = MapColor.COLOR_RED,
    )
    val mint_ice_cream = register("mint_ice_cream", iceCream(MapColor.COLOR_LIGHT_GREEN))
    val mint_ice_cream_family = registerFamily(
        "mint_ice_cream",
        mint_ice_cream,
        stairs = { properties(mint_ice_cream.get()) },
        slab = { properties(mint_ice_cream.get()) },
        mapColor = MapColor.COLOR_LIGHT_GREEN,
    )
    val blueberry_ice_cream = register("blueberry_ice_cream", iceCream(MapColor.COLOR_BLUE))
    val blueberry_ice_cream_family = registerFamily(
        "blueberry_ice_cream",
        blueberry_ice_cream,
        stairs = { properties(blueberry_ice_cream.get()) },
        slab = { properties(blueberry_ice_cream.get()) },
        mapColor = MapColor.COLOR_BLUE,
    )
    val chocolate_ice_cream = register("chocolate_ice_cream", iceCream(MapColor.COLOR_BROWN))
    val chocolate_ice_cream_family = registerFamily(
        "chocolate_ice_cream",
        chocolate_ice_cream,
        stairs = { properties(chocolate_ice_cream.get()) },
        slab = { properties(chocolate_ice_cream.get()) },
        mapColor = MapColor.COLOR_BROWN,
    )
    val banana_ice_cream = register("banana_ice_cream", iceCream(MapColor.COLOR_YELLOW))
    val banana_ice_cream_family = registerFamily(
        "banana_ice_cream",
        banana_ice_cream,
        stairs = { properties(banana_ice_cream.get()) },
        slab = { properties(banana_ice_cream.get()) },
        mapColor = MapColor.COLOR_YELLOW,
    )

    val purple_trampojelly = register("purple_trampojelly") {
        JellyBlock(JellyType.PURPLE, jelly(MapColor.COLOR_PURPLE).lightLevel { _ -> 13 })
    }.transparent()
    val trampojelly = register("trampojelly") {
        JellyBlock(JellyType.GREEN, jelly(MapColor.COLOR_GREEN))
    }.transparent()
    val red_trampojelly = register("red_trampojelly") {
        JellyBlock(JellyType.RED, jelly(MapColor.COLOR_RED))
    }.transparent()
    val yellow_trampojelly = register("yellow_trampojelly") {
        JellyBlock(JellyType.YELLOW, jelly(MapColor.COLOR_YELLOW))
    }.transparent()
    val jelly_shock_absorber = register("jelly_shock_absorber") {
        JellyBlock(JellyType.BLUE, jelly(MapColor.COLOR_LIGHT_BLUE))
    }.transparent()
    val grenadine_ice = register("grenadine_ice") { IceBlock(properties(Blocks.ICE).mapColor(MapColor.COLOR_RED)) }
        .transparent()
    val banana_block = register("banana_block", hay(MapColor.COLOR_YELLOW))
    val chewing_gum_block = register("chewing_gum_block", jelly(MapColor.COLOR_PINK))
    val mint_block = register("mint_block", hay(MapColor.COLOR_LIGHT_GREEN))
    val raspberry_block = register("raspberry_block", hay(MapColor.COLOR_RED))
    val honey_lamp =
        register("honey_lamp", iron(MapColor.COLOR_YELLOW).strength(1.0F).sound(SoundType.GLASS).lightLevel { 15 })
    val honeycomb_block = register("honeycomb_block", stone(MapColor.COLOR_YELLOW))
    val pez_block = register("pez_block", iron(MapColor.COLOR_RED).strength(5.0F, 10.0F))

    val jawbreaker_block = register("jawbreaker_block", properties(Blocks.BEDROCK))
    val jawbreaker_light = register("jawbreaker_light", properties(Blocks.BEDROCK).lightLevel { 15 })

    val chocolate_stone = register("chocolate_stone", stone(MapColor.COLOR_BROWN))
    val chocolate_stone_family = registerFamily(
        "chocolate_stone",
        chocolate_stone,
        stairs = { properties(chocolate_stone.get()) },
        slab = { properties(chocolate_stone.get()) },
        mapColor = MapColor.COLOR_BROWN,
    )
    val chocolate_cobblestone = register("chocolate_cobblestone", stone(MapColor.COLOR_BROWN))
    val chocolate_cobblestone_family = registerFamily(
        "chocolate_cobblestone",
        chocolate_cobblestone,
        stairs = { properties(chocolate_cobblestone.get()) },
        slab = { properties(chocolate_cobblestone.get()) },
        wall = { properties(chocolate_cobblestone.get()) },
        mapColor = MapColor.COLOR_BROWN,
    )

    val chocolate_covered_white_brownie =
        register("chocolate_covered_white_brownie", properties(Blocks.DIRT).mapColor(MapColor.SAND))

    val milk_brownie_block = register("milk_brownie_block", cake(MapColor.DIRT))
    val milk_chiffon_cake_block = register("milk_chiffon_cake_block") {
        RotatedPillarBlock(properties(milk_brownie_block.get()))
    }
    val milk_brownie_cake_roll_block = register("milk_brownie_cake_roll_block") {
        RotatedPillarBlock(properties(milk_brownie_block.get()))
    }

    val white_brownie_block = register("white_brownie_block", cake(MapColor.SAND))
    val white_chiffon_cake_block = register("white_chiffon_cake_block") {
        RotatedPillarBlock(properties(white_brownie_block.get()))
    }
    val white_brownie_cake_roll_block = register("white_brownie_cake_roll_block") {
        RotatedPillarBlock(properties(white_brownie_block.get()))
    }

    val dark_brownie_block = register("dark_brownie_block", cake(MapColor.TERRACOTTA_BROWN))
    val dark_chiffon_cake_block = register("dark_chiffon_cake_block") {
        RotatedPillarBlock(properties(dark_brownie_block.get()))
    }
    val dark_brownie_cake_roll_block = register("dark_brownie_cake_roll_block") {
        RotatedPillarBlock(properties(dark_brownie_block.get()))
    }

    val cake_block = register("cake_block", cake(MapColor.TERRACOTTA_BROWN))
    val candy_cotton_grass_block =
        register("candy_cotton_grass_block", properties(Blocks.GRASS_BLOCK).mapColor(MapColor.TERRACOTTA_BROWN))


    val licorice_ore = register("licorice_ore", stone(MapColor.TERRACOTTA_WHITE))
    val honey_ore = register("honey_ore", stone(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F))
    val pez_ore = register("pez_ore", stone(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F))
    val jelly_ore = register("jelly_ore", stone(MapColor.TERRACOTTA_WHITE))
    val nougat_ore = register("nougat_ore", stone(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F))
    val cookie_ore = register("cookie_ore", stone(MapColor.TERRACOTTA_WHITE))
    val crystallized_cookie_ore = register("crystallized_cookie_ore", stone(MapColor.TERRACOTTA_WHITE))
    val magic_candy_ore = register("magic_candy_ore", stone(MapColor.TERRACOTTA_WHITE))

    //plants

    val sweet_grass_pink = register("sweet_grass_pink") { CandyPlantBlock(plant(MapColor.COLOR_PINK)) }
    val sweet_grass_pale = register("sweet_grass_pale") { CandyPlantBlock(plant(MapColor.COLOR_LIGHT_GRAY)) }
    val sweet_grass_yellow = register("sweet_grass_yellow") { CandyPlantBlock(plant(MapColor.COLOR_YELLOW)) }
    val sweet_grass_red = register("sweet_grass_red") { CandyPlantBlock(plant(MapColor.COLOR_RED)) }

    val chocolate_leaves = register("chocolate_leaves") { LeavesBlock(leaves(MapColor.COLOR_BROWN)) }
        .cutout()
    val ice_cream_leaves = register("ice_cream_leaves") { LeavesBlock(leaves(MapColor.TERRACOTTA_WHITE)) }
        .cutout()
    val candied_cherry_leaves = register("candied_cherry_leaves") { LeavesBlock(leaves(MapColor.COLOR_RED)) }
        .cutout()
    val caramel_leaves = register("caramel_leaves") { LeavesBlock(leaves(MapColor.COLOR_ORANGE)) }
        .cutout()
    val enchant_candy_leaves = register("enchant_candy_leaves") { LeavesBlock(leaves(MapColor.COLOR_PURPLE)) }
        .cutout()
    val milk_chocolate_leaves = register("milk_chocolate_leaves") { LeavesBlock(leaves(MapColor.COLOR_BROWN)) }
        .cutout()
    val white_chocolate_leaves = register("white_chocolate_leaves") { LeavesBlock(leaves(MapColor.SAND)) }
        .cutout()
    val dark_chocolate_leaves = register("dark_chocolate_leaves") { LeavesBlock(leaves(MapColor.TERRACOTTA_BROWN)) }
        .cutout()

    val chocolate_sapling = register("chocolate_sapling") {
        CandySaplingBlock(null, plant(MapColor.COLOR_BROWN))
    }.cutout()
    val caramel_sapling = register("caramel_sapling") {
        CandySaplingBlock(null, plant(MapColor.COLOR_ORANGE))
    }.cutout()
    val ice_cream_sapling = register("ice_cream_sapling") {
        CandySaplingBlock(null, plant(MapColor.TERRACOTTA_WHITE))
    }.cutout()
    val candied_cherry_sapling = register("candied_cherry_sapling") {
        CandySaplingBlock(null, plant(MapColor.COLOR_RED))
    }.cutout()
    val wafer_chocolate_sapling = register("wafer_chocolate_sapling") {
        CandySaplingBlock(null, plant(MapColor.COLOR_BROWN))
    }.cutout()
    val cotton_candy_sapling = register("cotton_candy_sapling") {
        CandySaplingBlock(null, plant(MapColor.COLOR_PINK))
    }.cutout()
    val fraise_tagada_flower = register("fraise_tagada_flower") { CandyPlantBlock(plant(MapColor.COLOR_PINK)) }
        .cutout()
    val acid_mint_flower = register("acid_mint_flower") { CandyPlantBlock(plant(MapColor.GRASS)) }
        .cutout()
    val sugar_essence_flower = register("sugar_essence_flower") { CandyPlantBlock(plant(MapColor.GOLD)) }
        .cutout()

    val marshmallow_slice = register("marshmallow_slice") {
        CandyWaterlilyBlock(false, properties(Blocks.LILY_PAD).mapColor(MapColor.COLOR_PINK).randomTicks())
    }
    val marshmallow_slice_flower = register("marshmallow_slice_flower") {
        CandyWaterlilyBlock(true, properties(marshmallow_slice.get()))
    }
    val milk_chocolate_mushroom =
        register("milk_chocolate_mushroom") { CandyPlantBlock(plant(MapColor.COLOR_BROWN)) }.cutout()
    val white_chocolate_mushroom =
        register("white_chocolate_mushroom") { CandyPlantBlock(plant(MapColor.SAND)) }.cutout()
    val dark_chocolate_mushroom =
        register("dark_chocolate_mushroom") { CandyPlantBlock(plant(MapColor.TERRACOTTA_BROWN)) }.cutout()

    val rope_licorice = register("rope_licorice") { SeaweedBlock(true, plant(MapColor.TERRACOTTA_RED)) }
    val mint = register("mint") { SeaweedBlock(false, plant(MapColor.COLOR_GREEN)) }
    val banana_seaweed = register("banana_seaweed") { SeaweedBlock(false, plant(MapColor.COLOR_YELLOW)) }

    init {
        ifClient {
            cutouts.forEach { setRenderLayer(it, RenderType.cutoutMipped()) }
            transparent.forEach { setRenderLayer(it, RenderType.translucent()) }
        }
    }

    private fun properties(copyFrom: BlockBehaviour? = null) =
        if (copyFrom != null) Properties.copy(copyFrom) else Properties.of()

    private fun pudding(color: MapColor) = properties().sound(SoundType.WOOL).strength(0.6f).mapColor(color)
    private fun marshmallow(color: MapColor = MapColor.COLOR_PINK) = properties(Blocks.OAK_STAIRS).mapColor(color)
    private fun chocolate(color: MapColor) =
        properties(Blocks.STONE).mapColor(color).strength(0.7F)

    private fun hay(color: MapColor) = properties(Blocks.HAY_BLOCK).mapColor(color)
    private fun plant(color: MapColor) = properties(Blocks.GRASS).mapColor(color)
    private fun stone(color: MapColor) = properties(Blocks.STONE).mapColor(color)
    private fun iron(color: MapColor) = properties(Blocks.IRON_BLOCK).mapColor(color)
    private fun iceCream(color: MapColor) = properties(Blocks.SNOW).mapColor(color)
    private fun cookie(color: MapColor = MapColor.TERRACOTTA_ORANGE) = properties(Blocks.OAK_PLANKS).mapColor(color)
        .sound(SoundType.CALCITE)

    private fun leaves(color: MapColor) = properties(Blocks.OAK_LEAVES).mapColor(color)

    private fun cake(color: MapColor) = properties().sound(SoundType.WOOL).strength(0.5f).mapColor(color)
    private fun jelly(color: MapColor) = properties(Blocks.SLIME_BLOCK).mapColor(color)
        .strength(3.0F, 2000.0F)/*todo sound .sound(CCSoundTypes.JELLY)*/.noOcclusion()

    private fun gummy(color: MapColor) = properties(Blocks.SLIME_BLOCK).mapColor(color).strength(0.4f).friction(0.6f)
    private fun hardCandy(color: MapColor) = properties(Blocks.STONE).mapColor(color).strength(1.2F)

    //=================================
    fun register(name: String, properties: Properties = properties()): Entry<Block> =
        register(name) { Block(properties) }

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
        }
        val trapdoor = trapdoor?.let {
            register("${name}_trapdoor") {
                TrapDoorBlock(
                    trapdoor().noOcclusion().isValidSpawn(::never).mapColor(mapColor), setType
                )
            }
        }
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
        }?.cutout()?.noSimpleItem()

        return BlockFamily(
            original, stairs, slab, wall, fence, fenceGate, door, trapdoor, sign, wallSign
        ).also { families.add(it) }
    }

    private fun registerGummyFamily(color: String, mapColor: MapColor): GummyFamily {
        return GummyFamily(
            register("${color}_gummy_block") {
                JellyBlock(JellyType.NONE, gummy(mapColor).noOcclusion())
            }.transparent(),
            register("${color}_hardened_gummy_block", gummy(mapColor)),
            register("${color}_worm_gummy_block") {
                RotatedPillarBlock(gummy(mapColor))
            },
        )
    }

    //===============Render Types==================
    private fun <B : Block> Entry<B>.cutout() = apply { ifClient { cutouts.add(this) } }
    private fun <B : Block> Entry<B>.transparent() = apply { ifClient { transparent.add(this) } }
    private fun <B : Block> Entry<B>.noSimpleItem() = apply { noItem.add(this) }

    //=================================
    fun Entry<out ItemLike>.asItemEntry() =
        LazyEntry(id) { get().asItem() ?: throw RuntimeException("Item not found for $id") }

    fun Entry<out Block>.defaultBlockState(): BlockState = get().defaultBlockState()

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
    ) : IEntrySet<Block> {
        init {
            if (sign == null && wallSign != null || sign != null && wallSign == null) {
                throw IllegalArgumentException("sign and wallSign must be both null or both not null")
            }
        }

        override fun entries() = listOfNotNull(original, stairs, slab, fence, fenceGate, door, trapdoor, sign, wallSign)
    }

    data class GummyFamily(
        val block: Entry<out JellyBlock>,
        val hardened: Entry<out Block>,
        val worm: Entry<out RotatedPillarBlock>,
    ) :
        IEntrySet<Block> {
        override fun entries() = listOf(block, hardened, worm)
    }
}
