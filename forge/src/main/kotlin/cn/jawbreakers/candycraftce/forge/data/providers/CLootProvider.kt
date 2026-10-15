package cn.jawbreakers.candycraftce.forge.data.providers

import cn.jawbreakers.candycraftce.forge.data.providers.loots.CBlockLoots
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.ValidationContext
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets

/**
 * Created in 2026/10/10 21:07 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class CLootProvider(output: PackOutput) : LootTableProvider(
    output,
    setOf(),
    mapOf(
        ::CBlockLoots to LootContextParamSets.BLOCK
    ).map { SubProviderEntry(it.key, it.value) }
) {
    override fun validate(map: Map<ResourceLocation?, LootTable?>, validationcontext: ValidationContext) {
    }
}