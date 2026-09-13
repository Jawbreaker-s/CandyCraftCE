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
            tag(grenadine).addSet(CFluids.grenadine)
            tag(caramel).addSet(CFluids.caramel)
            tag(liquid_chocolate).addSet(CFluids.liquid_chocolate)
            tag(liquid_candy).addSet(CFluids.liquid_candy)
        }
    }
}

fun <T : Any> IntrinsicHolderTagsProvider.IntrinsicTagAppender<T>.addSet(set: IEntrySet<out T>) {
    set.forEach { add(it.get()) }
}