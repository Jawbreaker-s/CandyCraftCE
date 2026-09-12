package cn.jawbreakers.candycraftce.forge.data.providers.tags

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.registry.CFluidTags
import cn.jawbreakers.candycraftce.registry.CFluids
import cn.jawbreakers.candycraftce.utils.IEntrySet
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.FluidTagsProvider
import net.minecraft.data.tags.IntrinsicHolderTagsProvider
import net.minecraftforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class CFluidTagsProvider(
    output: PackOutput,
    lookup: CompletableFuture<HolderLookup.Provider>,
    efHelper: ExistingFileHelper,
) : FluidTagsProvider(
    output,
    lookup, MOD_ID, efHelper
) {
    override fun addTags(provider: HolderLookup.Provider) {
        CFluidTags.apply {
            tag(grenadine).add(CFluids.grenadine)
            tag(caramel).add(CFluids.caramel)
            tag(liquid_chocolate).add(CFluids.liquid_chocolate)
            tag(liquid_candy).add(CFluids.liquid_candy)
        }
    }
}

fun <T : Any> IntrinsicHolderTagsProvider.IntrinsicTagAppender<T>.add(set: IEntrySet<out T>) {
    set.forEach { add(it.get()) }
}