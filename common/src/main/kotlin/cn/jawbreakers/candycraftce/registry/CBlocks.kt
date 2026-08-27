package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.block.CandyFarmlandBlock
import cn.jawbreakers.candycraftce.block.CustardPuddingBlock
import cn.jawbreakers.candycraftce.block.SugarSandBlock
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CMixins.addStrippables
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.setRenderLayer
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.whenInitialized
import cn.jawbreakers.candycraftce.utils.IEntrySet
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.properties.WoodType
import net.minecraft.world.level.material.MapColor
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
    val sugar_block = register("sugar_block", properties(Blocks.STONE).mapColor(MapColor.SAND))

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
            addStrippables(marshmallow_log.get(), stripped_marshmallow_log.get())
            addStrippables(light_marshmallow_log.get(), stripped_light_marshmallow_log.get())
            addStrippables(dark_marshmallow_log.get(), stripped_dark_marshmallow_log.get())
        }
    }

    val marshmallow = registerFamily(
        register("marshmallow_planks", marshmallow()),
        "marshmallow",
        stair = { properties(Blocks.OAK_STAIRS) },
        slab = { properties(Blocks.OAK_SLAB) },
        fence = { properties(Blocks.OAK_FENCE) },
        fenceGate = { properties(Blocks.OAK_FENCE_GATE) },
        door = { properties(Blocks.OAK_DOOR) },
        trapdoor = { properties(Blocks.OAK_TRAPDOOR) },
        sign = { properties(Blocks.OAK_SIGN) },
        woodType = WoodType.CHERRY,
        mapColor = MapColor.COLOR_PINK,
    )
    val light_marshmallow = registerFamily(
        register("light_marshmallow_planks", marshmallow()),
        "light_marshmallow",
        stair = { properties(Blocks.BIRCH_STAIRS) },
        slab = { properties(Blocks.BIRCH_SLAB) },
        fence = { properties(Blocks.BIRCH_FENCE) },
        fenceGate = { properties(Blocks.BIRCH_FENCE_GATE) },
        door = { properties(Blocks.BIRCH_DOOR) },
        trapdoor = { properties(Blocks.BIRCH_TRAPDOOR) },
        sign = { properties(Blocks.BIRCH_SIGN) },
        woodType = WoodType.BIRCH,
        mapColor = MapColor.COLOR_PINK,
    )
    val dark_marshmallow = registerFamily(
        register("dark_marshmallow_planks", marshmallow()),
        "dark_marshmallow",
        stair = { properties(Blocks.JUNGLE_STAIRS) },
        slab = { properties(Blocks.JUNGLE_SLAB) },
        fence = { properties(Blocks.JUNGLE_FENCE) },
        fenceGate = { properties(Blocks.JUNGLE_FENCE_GATE) },
        door = { properties(Blocks.JUNGLE_DOOR) },
        trapdoor = { properties(Blocks.JUNGLE_TRAPDOOR) },
        sign = { properties(Blocks.JUNGLE_SIGN) },
        woodType = WoodType.JUNGLE,
        mapColor = MapColor.COLOR_PINK,
    )
    val marshmallow_planks = marshmallow.original
    val dark_marshmallow_planks = dark_marshmallow.original
    val light_marshmallow_planks = light_marshmallow.original

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

    fun register(name: String, properties: Properties = properties()): Entry<Block> =
        register(name) { Block(properties) }

    private fun <B : Block> register(name: String, factory: Supplier<B>): Entry<B> {
        val block = CPlatformUtils.registerBlock(name, factory)
//        contextTab?.addItem { item.defaultInstance }
        blocks[name] = block
        return block
    }

    private fun registerFamily(
        original: Entry<out Block>,
        name: String,
        stair: () -> Properties,
        slab: () -> Properties,
        fence: () -> Properties,
        fenceGate: () -> Properties,
        door: () -> Properties,
        trapdoor: () -> Properties,
        sign: () -> Properties,
        woodType: WoodType,
        mapColor: MapColor,
    ): BlockFamily {
        val stairs = register("${name}_stairs") {
            StairBlock(original.get().defaultBlockState(), stair().mapColor(mapColor))
        }
        val slab = register("${name}_slab") {
            SlabBlock(slab().mapColor(mapColor))
        }
        val fence = register("${name}_fence") {
            FenceBlock(fence().mapColor(mapColor))
        }
        val fenceGate = register("${name}_fence_gate") {
            FenceGateBlock(fenceGate().mapColor(mapColor), woodType)
        }
        val door = register("${name}_door") {
            DoorBlock(door().mapColor(mapColor), woodType.setType)
        }.cutout()
        val trapdoor = register("${name}_trapdoor") {
            TrapDoorBlock(trapdoor().mapColor(mapColor), woodType.setType)
        }.cutout()
        val sign = register("${name}_sign") {
            StandingSignBlock(sign().mapColor(mapColor), woodType)
        }.cutout()
        //todo sign需要重定向BlockEntity see:CandyStandingSignBlock
        val wallSign = register("${name}_wall_sign") {
            WallSignBlock(sign().mapColor(mapColor).dropsLike(sign.get()), woodType)
        }.cutout().noSimpleItem()
        return BlockFamily(original, stairs, slab, fence, fenceGate, door, trapdoor, sign, wallSign)
    }

    private fun <B : Block> Entry<B>.cutout() = apply { ifClient { cutouts.add(this) } }
    private fun <B : Block> Entry<B>.transparent() = apply { ifClient { transparent.add(this) } }
    private fun <B : Block> Entry<B>.noSimpleItem() = apply { noItem.add(this) }

    data class BlockFamily(
        val original: Entry<out Block>,
        val stairs: Entry<out StairBlock>,
        val slab: Entry<out SlabBlock>,
        val fence: Entry<out FenceBlock>,
        val fenceGate: Entry<out FenceGateBlock>,
        val door: Entry<out DoorBlock>,
        val trapdoor: Entry<out TrapDoorBlock>,
        val sign: Entry<out StandingSignBlock>,
        val wallSign: Entry<out WallSignBlock>,
    ) : IEntrySet<Block> {
        override fun entries(): List<Entry<out Block>> =
            listOf(original, stairs, slab, fence, fenceGate, door, trapdoor, sign, wallSign)
    }
}
