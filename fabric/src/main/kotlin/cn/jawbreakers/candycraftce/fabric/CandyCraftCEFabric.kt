package cn.jawbreakers.candycraftce.fabric

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CLogUtils.logRegister
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.CUtils.register
import cn.jawbreakers.candycraftce.utils.PlatformInstance
import cn.jawbreakers.candycraftce.utils.registry.Accessor
import cn.jawbreakers.candycraftce.utils.registry.Entry
import cn.jawbreakers.candycraftce.utils.registry.LateInitAccessor
import net.fabricmc.api.EnvType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import java.util.function.Consumer
import java.util.function.Supplier

class CandyCraftCEFabric : ModInitializer, PlatformInstance {
    private val lateInits = mutableListOf<Runnable>()
    private val lateUsage = mutableListOf<Runnable>()

    override fun onInitialize() {
        clog.info("on Fabric Initializing...")
        CandyCraftCE.init(this) {
            lateInits.forEach { it.run() }
            clog.info("Running `whenInitialized`")
            lateUsage.forEach { it.run() }
            lateInits.clear()
            lateUsage.clear()
        }
    }

    override val isDev = FabricLoader.getInstance().isDevelopmentEnvironment
    override val isClient: Boolean = FabricLoader.getInstance().environmentType == EnvType.CLIENT

    override fun <T> whenInitialized(action: () -> T): Accessor<T> {
        val accessor = LateInitAccessor<T>()
        lateUsage.add { accessor.set(action()) }
        return accessor
    }

    private inline fun <E> register(
        registerTag: String,
        name: String,
        crossinline factory: () -> E,
        crossinline register: (ResourceLocation, E) -> Unit,
    ): Entry<E> {
        val id = name.modLoc()
        val entry = FabricEntry<E>(id)
        lateInits.add {
            logRegister(registerTag, id)
            factory().also {
                register(id, it)
                entry.set(it)
            }
        }
        return entry
    }

    override fun <E : Item> registerItem(name: String, factory: Supplier<E>): Entry<E> =
        register("Item", name, factory::get) { id, it ->
            Items.registerItem(id, it)
        }

    override fun setRenderLayer(block: Entry<out Block>, layer: RenderType) {
        ifClient {
            BlockRenderLayerMap.INSTANCE.putBlock(block.get(), layer)
        }
    }

    override fun <E : Block> registerBlock(name: String, factory: Supplier<E>): Entry<E> =
        register("Block", name, factory::get) { id, it ->
            BuiltInRegistries.BLOCK.register(id, it)
        }

    override fun registerCreativeTab(name: String, builder: Consumer<CreativeModeTab.Builder>): Entry<CreativeModeTab> {
        val id = name.modLoc()
        val entry = FabricEntry<CreativeModeTab>(id)
        lateInits.add {
            logRegister("CreativeModeTab", id)
            FabricItemGroup.builder().also(builder::accept).build().also {
                BuiltInRegistries.CREATIVE_MODE_TAB.register(id, it)
                entry.set(it)
            }
        }
        return entry
    }
}