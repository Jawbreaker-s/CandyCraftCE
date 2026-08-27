package cn.jawbreakers.candycraftce.forge.data

import cn.jawbreakers.candycraftce.forge.data.providers.CBlockStateProvider
import cn.jawbreakers.candycraftce.forge.data.providers.CI18nProvider
import cn.jawbreakers.candycraftce.forge.data.providers.CItemModelProvider
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

        generator.addProvider(event.includeClient(), CI18nProvider(output))
        generator.addProvider(event.includeClient(), CBlockStateProvider(output, efHelper))
        generator.addProvider(event.includeClient(), CItemModelProvider(output, efHelper))
    }
}