package cn.jawbreakers.candycraftce.fabric

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.fabric.fluid.CFabricFluids
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CLogUtils.logRegister
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.CUtils.register
import cn.jawbreakers.candycraftce.utils.ICPlatForm
import cn.jawbreakers.candycraftce.utils.ICPlatformFluids
import cn.jawbreakers.candycraftce.utils.registry.Accessor
import cn.jawbreakers.candycraftce.utils.registry.Entry
import cn.jawbreakers.candycraftce.utils.registry.LateInitAccessor
import com.mojang.datafixers.types.Type
import net.fabricmc.api.EnvType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.impl.client.rendering.DimensionRenderingRegistryImpl
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.color.block.BlockColor
import net.minecraft.client.color.item.ItemColor
import net.minecraft.client.renderer.DimensionSpecialEffects
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import java.util.function.Consumer
import java.util.function.Supplier

class CandyCraftCEFabric : ModInitializer, ICPlatForm {
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
    override val fluids: ICPlatformFluids get() = CFabricFluids

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
        register("CItemTags", name, factory::get) { id, it ->
            Items.registerItem(id, it)
        }


    override fun <E : Block> registerBlock(name: String, factory: Supplier<E>): Entry<E> =
        register("CBlockTags", name, factory::get) { id, it ->
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


    @Suppress("UnstableApiUsage")
    override fun registerDimensionSpecialEffects(id: ResourceLocation, effects: DimensionSpecialEffects) =
        DimensionRenderingRegistryImpl.registerDimensionEffects(id, effects)

    override fun registerCreativeTab(name: String, builder: Consumer<CreativeModeTab.Builder>): Entry<CreativeModeTab> =
        register("CreativeModeTab", name, {
            FabricItemGroup.builder().also(builder::accept).build()
        }) { id, it ->
            BuiltInRegistries.CREATIVE_MODE_TAB.register(id, it)
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