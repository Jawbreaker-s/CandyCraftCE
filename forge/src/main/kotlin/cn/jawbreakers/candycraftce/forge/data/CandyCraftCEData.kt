package cn.jawbreakers.candycraftce.forge.data

import cn.jawbreakers.candycraftce.forge.data.providers.CBlockStateProvider
import cn.jawbreakers.candycraftce.forge.data.providers.CI18nProvider
import cn.jawbreakers.candycraftce.forge.data.providers.CItemModelProvider
import cn.jawbreakers.candycraftce.forge.data.providers.tags.CBlockTagsProvider
import cn.jawbreakers.candycraftce.forge.data.providers.tags.CFluidTagsProvider
import cn.jawbreakers.candycraftce.forge.data.providers.tags.CItemTagsProvider
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
object CandyCraftCEData {
    @SubscribeEvent
    fun initialize(event: GatherDataEvent) {
        val efHelper = event.existingFileHelper
        val generator = event.generator
        val container = event.modContainer
        val output = generator.packOutput
        val lookup = event.lookupProvider

        generator.addProvider(event.includeClient(), CI18nProvider(output))
        generator.addProvider(event.includeClient(), CBlockStateProvider(output, efHelper))
        generator.addProvider(event.includeClient(), CItemModelProvider(output, efHelper))

        val blocktag = generator.addProvider(event.includeServer(), CBlockTagsProvider(output, lookup, efHelper))
        generator.addProvider(
            event.includeServer(),
            CItemTagsProvider(output, lookup, blocktag.contentsGetter(), efHelper)
        )
        generator.addProvider(event.includeServer(), CFluidTagsProvider(output, lookup, efHelper))
    }
}