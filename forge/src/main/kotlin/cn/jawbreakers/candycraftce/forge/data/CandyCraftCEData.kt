package cn.jawbreakers.candycraftce.forge.data

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.forge.data.providers.CBlockStateProvider
import cn.jawbreakers.candycraftce.forge.data.providers.CI18nProvider
import cn.jawbreakers.candycraftce.forge.data.providers.CItemModelProvider
import cn.jawbreakers.candycraftce.forge.data.providers.tags.CBiomeTagsProvider
import cn.jawbreakers.candycraftce.forge.data.providers.tags.CBlockTagsProvider
import cn.jawbreakers.candycraftce.forge.data.providers.tags.CFluidTagsProvider
import cn.jawbreakers.candycraftce.forge.data.providers.tags.CItemTagsProvider
import cn.jawbreakers.candycraftce.utils.ICPlatformDatagen
import cn.jawbreakers.candycraftce.utils.ILanguageProvider
import net.minecraft.core.RegistrySetBuilder
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import java.util.*
import java.util.function.Consumer
import kotlin.concurrent.thread

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
object CandyCraftCEData : ICPlatformDatagen {
    private var status: Boolean = true
    private val bootstraps: LinkedList<Consumer<RegistrySetBuilder>> = LinkedList()
    private val i18nExtra: LinkedList<Consumer<ILanguageProvider>> = LinkedList()

    @SubscribeEvent
    fun initialize(event: GatherDataEvent) {
        status = false
        val efHelper = event.existingFileHelper
        val generator = event.generator
        val output = generator.packOutput
        //Client
        generator.addProvider(
            event.includeClient(), CI18nProvider(output).also { i18nExtra.forEach { consumer -> consumer.accept(it) } })
        generator.addProvider(event.includeClient(), CBlockStateProvider(output, efHelper))
        generator.addProvider(event.includeClient(), CItemModelProvider(output, efHelper))
        //Server
        val registries = RegistrySetBuilder()
        bootstraps.forEach { it.accept(registries) }
        val lookup = generator.addProvider(
            event.includeServer(),
            DatapackBuiltinEntriesProvider(output, event.lookupProvider, registries, setOf(MOD_ID))
        ).registryProvider

        val blocktag = generator.addProvider(event.includeServer(), CBlockTagsProvider(output, lookup, efHelper))
        generator.addProvider(
            event.includeServer(),
            CItemTagsProvider(output, lookup, blocktag.contentsGetter(), efHelper)
        )
        generator.addProvider(event.includeServer(), CFluidTagsProvider(output, lookup, efHelper))
        generator.addProvider(event.includeServer(), CBiomeTagsProvider(output, lookup, efHelper))
        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            try {
                CRedundantResourcesChecker.run()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun checkStatus() = require(status) { "Too late" }
    override fun onBootstrap(action: RegistrySetBuilder.() -> Unit) {
        checkStatus()
        bootstraps.add(action)
    }

    override fun onGatherLanguage(action: ILanguageProvider.() -> Unit) {
        checkStatus()
        i18nExtra.add(action)
    }
}