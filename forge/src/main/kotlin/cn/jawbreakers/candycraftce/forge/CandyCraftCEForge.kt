package cn.jawbreakers.candycraftce.forge

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.forge.ForgeEntry.Companion.asEntry
import cn.jawbreakers.candycraftce.forge.fluid.CForgeFluids
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CLogUtils.logRegister
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.ICPlatForm
import cn.jawbreakers.candycraftce.utils.ICPlatformFluids
import cn.jawbreakers.candycraftce.utils.registry.Accessor
import cn.jawbreakers.candycraftce.utils.registry.Entry
import cn.jawbreakers.candycraftce.utils.registry.LateInitAccessor
import com.mojang.datafixers.types.Type
import kotlinx.coroutines.Runnable
import net.minecraft.client.color.block.BlockColor
import net.minecraft.client.color.item.ItemColor
import net.minecraft.client.renderer.DimensionSpecialEffects
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.client.event.RegisterColorHandlersEvent
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fluids.FluidType
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.loading.FMLLoader
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.DeferredRegister.create
import net.minecraftforge.registries.ForgeRegistries
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import java.util.function.Consumer
import java.util.function.Supplier

@Mod(CandyCraftCE.MOD_ID)
class CandyCraftCEForge : ICPlatForm {
    companion object {
        lateinit var instance: CandyCraftCEForge
            private set
    }

    init {
        instance = this
    }

    override val isDev by lazy { !FMLLoader.isProduction(); }

    override val isClient by lazy { FMLLoader.getDist().isClient }
    override val fluids: ICPlatformFluids get() = CForgeFluids

    val items: DeferredRegister<Item> = create(ForgeRegistries.ITEMS, CandyCraftCE.MOD_ID)
    val blocks: DeferredRegister<Block> = create(ForgeRegistries.BLOCKS, CandyCraftCE.MOD_ID)
    val be: DeferredRegister<BlockEntityType<*>> = create(ForgeRegistries.BLOCK_ENTITY_TYPES, CandyCraftCE.MOD_ID)
    val tabs: DeferredRegister<CreativeModeTab> = create(Registries.CREATIVE_MODE_TAB, CandyCraftCE.MOD_ID)
    val fluid: DeferredRegister<Fluid> = create(ForgeRegistries.FLUIDS, CandyCraftCE.MOD_ID)
    val fluidType: DeferredRegister<FluidType> = create(ForgeRegistries.Keys.FLUID_TYPES, CandyCraftCE.MOD_ID)


    //=================================
    private var lateUsage: MutableList<Runnable>? = mutableListOf()
    fun onMinecraftSetup(event: FMLCommonSetupEvent) {
        clog.info("Running `whenInitialized`")
        lateUsage!!.forEach { it.run() }
        lateUsage = null
    }

    override fun <T> whenInitialized(action: () -> T): Accessor<T> {
        val accessor = LateInitAccessor<T>()
        lateUsage?.add { accessor.set(action()) } ?: error("Too late")
        return accessor
    }
    //=================================

    private fun <E> register(register: DeferredRegister<in E>, name: String, factory: Supplier<E>): Entry<E> {
        return register.register(name, factory)
            .also { logRegister(register.registryName.path, it.id) }
            .asEntry()
    }

    override fun <I : Item> registerItem(name: String, factory: Supplier<I>): Entry<I> =
        register(items, name, factory)

    override fun <E : Block> registerBlock(name: String, factory: Supplier<E>): Entry<E> =
        register(blocks, name, factory)

    override fun <E : BlockEntity> registerBlockEntity(
        key: String,
        builder: Supplier<BlockEntityType.Builder<E>>,
        dsl: Type<*>?,
    ): Entry<BlockEntityType<E>> = register(be, key, { builder.get().build(dsl) })


    override fun registerCreativeTab(
        name: String,
        builder: Consumer<CreativeModeTab.Builder>,
    ): Entry<CreativeModeTab> {
        return register(tabs, name) { CreativeModeTab.builder().also(builder::accept).build() }
    }

    //=================================
    override fun setRenderLayer(block: Entry<out Block>, layer: RenderType) {
        ifClient {
            whenInitialized {
                @Suppress("DEPRECATION")
                ItemBlockRenderTypes.setRenderLayer(block.get(), layer)
            }
        }
    }

    //=================================
    val specialEffects: MutableMap<ResourceLocation, DimensionSpecialEffects> = mutableMapOf()

    @SubscribeEvent
    fun onRegisterDimensionSpecialEffects(event: RegisterDimensionSpecialEffectsEvent) {
        specialEffects.forEach(event::register)
    }

    override fun registerDimensionSpecialEffects(id: ResourceLocation, effects: DimensionSpecialEffects) {
        specialEffects[id] = effects
    }

    //=================================
    private val blockColors: MutableMap<BlockColor, List<Entry<out Block>>> = mutableMapOf()
    private val itemColors: MutableMap<ItemColor, List<Entry<out Item>>> = mutableMapOf()

    fun onRegisterBlockColorHandlers(event: RegisterColorHandlersEvent.Block) {
        clog.info("onRegisterBlockColorHandlers")
        blockColors.forEach { (color, blocks) ->
            event.register(color, *blocks.map { it.get() }.toTypedArray())
        }
    }

    fun onRegisterItemColorHandlers(event: RegisterColorHandlersEvent.Item) {
        clog.info("onRegisterItemColorHandlers")
        itemColors.forEach { (color, items) ->
            event.register(color, *items.map { it.get() }.toTypedArray())
        }
    }

    override fun registerBlockColor(vararg blocks: Entry<out Block>, color: BlockColor) {
        blockColors[color] = blocks.toList()
    }

    override fun registerItemColor(vararg items: Entry<out Item>, color: ItemColor) {
        itemColors[color] = items.toList()
    }


    //=================================
    init {
        clog.info("on Forge Initializing...")

        //Registers
        listOf(
            items, tabs, blocks, fluid, fluidType
        ).forEach { it.register(MOD_BUS) }

        MOD_BUS.apply {
            addListener(::onMinecraftSetup)
            addListener(::onRegisterBlockColorHandlers)
            addListener(::onRegisterItemColorHandlers)
            addListener(::onRegisterDimensionSpecialEffects)
        }


        CandyCraftCE.init(this)
    }
}
