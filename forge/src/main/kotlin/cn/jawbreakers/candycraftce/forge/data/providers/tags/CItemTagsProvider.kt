package cn.jawbreakers.candycraftce.forge.data.providers.tags

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.registry.CBlockTags
import cn.jawbreakers.candycraftce.registry.CItemTags
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.ItemBlockTagkey
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.ItemTagsProvider
import net.minecraft.world.level.block.Block
import net.minecraftforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class CItemTagsProvider(
    output: PackOutput,
    lookup: CompletableFuture<HolderLookup.Provider>,
    blockTags: CompletableFuture<TagLookup<Block>>,
    efHelper: ExistingFileHelper,
) : ItemTagsProvider(output, lookup, blockTags, MOD_ID, efHelper) {
    override fun addTags(provider: HolderLookup.Provider) {

        CItemTags.apply {
            CItems.apply {
                tag(chocolate_bar)
                    .add(dark_chocolate_bar.get())
                    .add(milk_chocolate_bar.get())
                    .add(ruby_chocolate_bar.get())
                    .add(white_chocolate_bar.get())
            }
        }
        CBlockTags.apply {
            copy(marshmallow_planks)
        }
    }

    fun copy(biKey: ItemBlockTagkey) {
        copy(biKey.block, biKey.item)
    }
}