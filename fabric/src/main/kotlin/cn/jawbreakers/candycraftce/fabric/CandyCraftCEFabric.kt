package cn.jawbreakers.candycraftce.fabric

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CLogUtils.logRegister
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.CUtils.register
import cn.jawbreakers.candycraftce.utils.PlatformInstance
import cn.jawbreakers.candycraftce.utils.registry.Accessor
import cn.jawbreakers.candycraftce.utils.registry.Entry
import cn.jawbreakers.candycraftce.utils.registry.LateInitAccessor
import net.fabricmc.api.EnvType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import java.util.function.Consumer
import java.util.function.Supplier

class CandyCraftCEFabric : ModInitializer, PlatformInstance {
    private val lateInits = mutableListOf<Runnable>()
    private val lateUsage = mutableListOf<Runnable>()

    override fun onInitialize() {
        clog.info("on Fabric Initializing...")
        CandyCraftCE.init(this) {
            lateInits.forEach { it.run() }
            lateUsage.forEach { it.run() }
        }
    }

    override val isDev = FabricLoader.getInstance().isDevelopmentEnvironment
    override val isClient: Boolean = FabricLoader.getInstance().environmentType == EnvType.CLIENT

    override fun <T> whenInitialized(action: () -> T): Accessor<T> {
        val accessor = LateInitAccessor<T>()
        lateUsage.add { accessor.set(action()) }
        return accessor
    }

    override fun <I : Item> registerItem(name: String, item: Supplier<I>): Entry<I> {
        val id = name.modLoc()
        val entry = FabricEntry<I>(id)
        lateInits.add {
            logRegister("Item", id)
            item.get().also {
                Items.registerItem(id, it)
                entry.set(it)
            }
        }
        return entry
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