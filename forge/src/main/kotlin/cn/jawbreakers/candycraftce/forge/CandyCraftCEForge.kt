package cn.jawbreakers.candycraftce.forge

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.forge.ForgeEntry.Companion.asEntry
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CLogUtils.logRegister
import cn.jawbreakers.candycraftce.utils.PlatformInstance
import cn.jawbreakers.candycraftce.utils.registry.Accessor
import cn.jawbreakers.candycraftce.utils.registry.Entry
import cn.jawbreakers.candycraftce.utils.registry.LateInitAccessor
import kotlinx.coroutines.Runnable
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.FMLLoader
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.DeferredRegister.create
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.IdMappingEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import java.util.function.Consumer
import java.util.function.Supplier

@Mod(CandyCraftCE.MOD_ID)
class CandyCraftCEForge : PlatformInstance {
    val items: DeferredRegister<Item> = create(ForgeRegistries.ITEMS, CandyCraftCE.MOD_ID)
    val tabs: DeferredRegister<CreativeModeTab> = create(Registries.CREATIVE_MODE_TAB, CandyCraftCE.MOD_ID)
    private val lateUsage = mutableListOf<Runnable>()

    init {
        clog.info("on Forge Initializing...")
        items.register(MOD_BUS)
        tabs.register(MOD_BUS)
        FORGE_BUS.addListener(::onFreezingData)

        CandyCraftCE.init(this)
    }

    override val isDev = !FMLLoader.isProduction();
    override val isClient = FMLLoader.getDist() == Dist.CLIENT

    fun onFreezingData(event: IdMappingEvent) {
        if (event.isFrozen) lateUsage.forEach { it.run() }
    }

    override fun <T> whenInitialized(action: () -> T): Accessor<T> {
        val accessor = LateInitAccessor<T>()
        lateUsage.add { accessor.set(action()) }
        return accessor
    }

    override fun <I : Item> registerItem(name: String, item: Supplier<I>): Entry<I> {
        return items.register(name, item)
            .also { logRegister("Item", it.id) }
            .asEntry()
    }

    override fun registerCreativeTab(
        name: String,
        builder: Consumer<CreativeModeTab.Builder>,
    ): Entry<CreativeModeTab> {
        return tabs.register(name) { CreativeModeTab.builder().also(builder::accept).build() }
            .also { logRegister("CreativeModeTab", it.id) }
            .asEntry()
    }
}