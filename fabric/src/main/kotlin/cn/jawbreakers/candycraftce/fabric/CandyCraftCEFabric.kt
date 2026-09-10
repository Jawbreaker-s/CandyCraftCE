package cn.jawbreakers.candycraftce.fabric

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.fabric.fluid.CFlowingFluid
import cn.jawbreakers.candycraftce.fabric.fluid.FabricLiquidBlock
import cn.jawbreakers.candycraftce.fluid.CFluidProperties
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CLogUtils.logRegister
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.CUtils.register
import cn.jawbreakers.candycraftce.utils.PlatformFluid
import cn.jawbreakers.candycraftce.utils.PlatformInstance
import cn.jawbreakers.candycraftce.utils.registry.Accessor
import cn.jawbreakers.candycraftce.utils.registry.Entry
import cn.jawbreakers.candycraftce.utils.registry.LateInitAccessor
import com.mojang.datafixers.types.Type
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler.WATER_FLOWING
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler.WATER_STILL
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes
import net.fabricmc.fabric.impl.client.rendering.DimensionRenderingRegistryImpl
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.color.block.BlockColor
import net.minecraft.client.color.item.ItemColor
import net.minecraft.client.renderer.DimensionSpecialEffects
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.FlowingFluid
import net.minecraft.world.level.material.Fluid
import java.util.function.Consumer
import java.util.function.Supplier

class CandyCraftCEFabric : ModInitializer, PlatformInstance, PlatformFluid {
    companion object {
        lateinit var instance: CandyCraftCEFabric
            private set
    }

    init {
        instance = this
    }

    private var lateInits: MutableList<Runnable>? = mutableListOf()
    private var lateUsage: MutableList<Runnable>? = mutableListOf()

    override val isDev by lazy { FabricLoader.getInstance().isDevelopmentEnvironment }
    override val isClient by lazy { FabricLoader.getInstance().environmentType == EnvType.CLIENT }
    override val fluids: PlatformFluid get() = this

    override fun <T> whenInitialized(action: () -> T): Accessor<T> {
        val accessor = LateInitAccessor<T>()
        lateUsage?.add { accessor.set(action()) } ?: throw IllegalStateException("Too late")
        return accessor
    }

    //=================================


    override fun setRenderLayer(block: Entry<out Block>, layer: RenderType) {
        ifClient {
            whenInitialized {
                BlockRenderLayerMap.INSTANCE.putBlock(block.get(), layer)
            }
        }
    }

    override fun registerBlockColor(vararg blocks: Entry<out Block>, color: BlockColor) {
        ifClient {
            whenInitialized {
                ColorProviderRegistry.BLOCK.register(color, *blocks.map { it.get() }.toTypedArray())
            }
        }
    }

    override fun registerItemColor(vararg items: Entry<out Item>, color: ItemColor) {
        ifClient {
            whenInitialized {
                ColorProviderRegistry.ITEM.register(color, *items.map { it.get() }.toTypedArray())
            }
        }
    }

    //=================================
    private inline fun <E> register(
        registerTag: String,
        name: String,
        crossinline factory: () -> E,
        crossinline register: (ResourceLocation, E) -> Unit,
    ): Entry<E> {
        val id = name.modLoc()
        val entry = FabricEntry<E>(id)
        lateInits?.add {
            logRegister(registerTag, id)
            factory().also {
                register(id, it)
                entry.set(it)
            }
        } ?: throw IllegalStateException("Too late")
        return entry
    }

    override fun <E : Item> registerItem(name: String, factory: Supplier<E>): Entry<E> =
        register("Item", name, factory::get) { id, it ->
            Items.registerItem(id, it)
        }


    override fun <E : Block> registerBlock(name: String, factory: Supplier<E>): Entry<E> =
        register("Block", name, factory::get) { id, it ->
            BuiltInRegistries.BLOCK.register(id, it)
        }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun <E : BlockEntity> registerBlockEntity(
        key: String,
        builder: Supplier<BlockEntityType.Builder<E>>,
        dsl: Type<*>?,
    ): Entry<BlockEntityType<E>> =
        register("BlockEntity", key, { builder.get().build(dsl) }) { id, it ->
            BuiltInRegistries.BLOCK_ENTITY_TYPE.register(id, it)
        }

    override fun <E : Fluid> registerFluids(
        name: String,
        properties: CFluidProperties,
        factory: Supplier<E>,
    ): Entry<E> = register("Fluid", name, factory::get) { id, it ->
        BuiltInRegistries.FLUID.register(id, it)
    }

    override fun createLiquidBlock(
        fluid: Entry<out FlowingFluid>,
        properties: BlockBehaviour.Properties,
        overrides: PlatformFluid.LiquidOverrides?,
    ) = FabricLiquidBlock(fluid.get(), properties, overrides)

    override fun createBucketItem(
        entry: Entry<out FlowingFluid>,
        properties: Item.Properties,
    ): BucketItem = BucketItem(entry.get(), properties)

    @Suppress("UnstableApiUsage")
    override fun registerDimensionSpecialEffects(id: ResourceLocation, effects: DimensionSpecialEffects) =
        DimensionRenderingRegistryImpl.registerDimensionEffects(id, effects)

    override fun registerCreativeTab(name: String, builder: Consumer<CreativeModeTab.Builder>): Entry<CreativeModeTab> =
        register("CreativeModeTab", name, {
            FabricItemGroup.builder().also(builder::accept).build()
        }) { id, it ->
            BuiltInRegistries.CREATIVE_MODE_TAB.register(id, it)
        }

    //==================PLATFORM FLUID===================

    val initializedFluidProperties = mutableSetOf<CFluidProperties>()

    @Suppress("UnstableApiUsage")
    @Environment(EnvType.CLIENT)
    private fun applyFluidSettings(fluid: CFlowingFluid, isSource: Boolean, properties: CFluidProperties) {
        val type = properties.type
        //贴图
        FluidRenderHandlerRegistry.INSTANCE.register(
            fluid,
            SimpleFluidRenderHandler(
                type.stillTexture ?: WATER_STILL,
                type.flowingTexture ?: WATER_FLOWING,
                type.overlayTexture,
                type.tintColor
            )
        )
        //透明
        if (type.isTransparent) {
            BlockRenderLayerMap.INSTANCE.putFluid(fluid, RenderType.translucent())
        }
        FluidVariantAttributes.register(fluid, object : FluidVariantAttributeHandler {
            val descriptionId by lazy { Component.translatable(type.descriptionId) }
            override fun getName(fluidVariant: FluidVariant): Component = descriptionId
            override fun getViscosity(variant: FluidVariant?, world: Level?): Int = type.viscosity
            override fun getTemperature(variant: FluidVariant?): Int = type.temperature
            override fun getLuminance(variant: FluidVariant?): Int = type.lightLevel
            override fun isLighterThanAir(variant: FluidVariant?): Boolean = type.density <= 0
        })
        if (properties !in initializedFluidProperties) {
            initializedFluidProperties += properties
        }

    }

    override fun createSource(properties: CFluidProperties): FlowingFluid {
        return CFlowingFluid.Source(properties).also {
            ifClient { whenInitialized { applyFluidSettings(it, true, properties) } }
        }
    }

    override fun createFlowing(properties: CFluidProperties): FlowingFluid {
        return CFlowingFluid.Flowing(properties).also {
            ifClient { whenInitialized { applyFluidSettings(it, false, properties) } }
        }
    }
    //=================================

    override fun onInitialize() {
        clog.info("on Fabric Initializing...")
        CandyCraftCE.init(this) {
            lateInits!!.forEach { it.run() }
            lateInits = null

            clog.info("Running `whenInitialized`")
            lateUsage!!.forEach { it.run() }
            lateUsage = null
        }
    }

}