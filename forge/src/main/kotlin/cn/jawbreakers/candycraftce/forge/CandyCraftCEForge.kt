package cn.jawbreakers.candycraftce.forge

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.forge.ForgeEntry.Companion.asEntry
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CLogUtils.logRegister
import cn.jawbreakers.candycraftce.utils.PlatformInstance
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraftforge.fml.common.Mod
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

    init {
        clog.info("on Forge Initializing...")
        items.register(MOD_BUS)
        tabs.register(MOD_BUS)

        CandyCraftCE.init(this)
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