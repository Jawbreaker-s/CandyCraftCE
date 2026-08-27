package cn.jawbreakers.candycraftce.forge

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.forge.ForgeEntry.Companion.asEntry
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CLogUtils.logRegister
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.PlatformInstance
import cn.jawbreakers.candycraftce.utils.registry.Accessor
import cn.jawbreakers.candycraftce.utils.registry.Entry
import cn.jawbreakers.candycraftce.utils.registry.LateInitAccessor
import kotlinx.coroutines.Runnable
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
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
class CandyCraftCEForge : PlatformInstance {
    val items: DeferredRegister<Item> = create(ForgeRegistries.ITEMS, CandyCraftCE.MOD_ID)
    val tabs: DeferredRegister<CreativeModeTab> = create(Registries.CREATIVE_MODE_TAB, CandyCraftCE.MOD_ID)
    val blocks: DeferredRegister<Block> = create(ForgeRegistries.BLOCKS, CandyCraftCE.MOD_ID)
    private val lateUsage = mutableListOf<Runnable>()

    override val isDev = !FMLLoader.isProduction();
    override val isClient = FMLLoader.getDist().isClient

    init {
        clog.info("on Forge Initializing...")
        listOf(
            items,
            tabs,
            blocks
        ).forEach { it.register(MOD_BUS) }

        MOD_BUS.addListener(::onMinecraftSetup)

        CandyCraftCE.init(this)
    }

    fun onMinecraftSetup(event: FMLCommonSetupEvent) {
        clog.info("Running `whenInitialized`")
        lateUsage.forEach { it.run() }
        lateUsage.clear()
    }

    override fun <T> whenInitialized(action: () -> T): Accessor<T> {
        val accessor = LateInitAccessor<T>()
        lateUsage.add { accessor.set(action()) }
        return accessor
    }

    private fun <E> register(register: DeferredRegister<in E>, name: String, factory: Supplier<E>): Entry<E> {
        return register.register(name, factory)
            .also { logRegister(register.registryName.path, it.id) }
            .asEntry()
    }

    override fun <I : Item> registerItem(name: String, factory: Supplier<I>): Entry<I> {
        return register(items, name, factory)
    }

    override fun <E : Block> registerBlock(name: String, factory: Supplier<E>): Entry<E> {
        return register(blocks, name, factory)
    }

    override fun setRenderLayer(block: Entry<out Block>, layer: RenderType) {
        ifClient {
            whenInitialized {
                @Suppress("DEPRECATION")
                ItemBlockRenderTypes.setRenderLayer(block.get(), layer)
            }
        }
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