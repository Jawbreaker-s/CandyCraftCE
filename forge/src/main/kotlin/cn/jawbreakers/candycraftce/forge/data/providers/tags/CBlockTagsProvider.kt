package cn.jawbreakers.candycraftce.forge.data.providers.tags

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraftforge.common.data.BlockTagsProvider
import net.minecraftforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class CBlockTagsProvider(
    output: PackOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider>,
    efHelper: ExistingFileHelper,
) : BlockTagsProvider(output, lookupProvider, MOD_ID, efHelper) {
    override fun addTags(provider: HolderLookup.Provider) {
    }
}

