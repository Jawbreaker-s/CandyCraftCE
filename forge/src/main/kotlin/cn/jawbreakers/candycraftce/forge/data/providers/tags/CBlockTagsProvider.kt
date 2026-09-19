package cn.jawbreakers.candycraftce.forge.data.providers.tags

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.registry.CBlockTags
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.ItemBlockTagkey
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.tags.BlockTags
import net.minecraftforge.common.data.BlockTagsProvider
import net.minecraftforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class CBlockTagsProvider(
    output: PackOutput,
    lookup: CompletableFuture<HolderLookup.Provider>,
    efHelper: ExistingFileHelper,
) : BlockTagsProvider(output, lookup, MOD_ID, efHelper) {
    override fun addTags(provider: HolderLookup.Provider) {
        fun tag(biTag: ItemBlockTagkey) = tag(biTag.block)
        CBlockTags.apply {
            CBlocks.apply {
                tag(CBlockTags.marshmallow_planks)
                    .add(marshmallow_planks.get())
                    .add(light_marshmallow_planks.get())
                    .add(dark_marshmallow_log.get())

                tag(candy_soil)
                    .add(pudding_block.get())
                    .add(custard_pudding_block.get())
                    .add(strawberry_filled_pudding.get())
                    .add(pudding_farmland.get())
                    .add(chocolate_covered_white_brownie.get())
                    .add(milk_brownie_block.get())
                    .add(dark_brownie_block.get())
                    .add(white_brownie_block.get())

                tag(worm_blocks)
                    .add(orange_gummy_family.block.get())
                    .add(yellow_gummy_family.block.get())
                    .add(white_gummy_family.block.get())
                    .add(green_gummy_family.block.get())
                    .add(red_gummy_family.block.get())

                tag(CBlockTags.marshmallow_logs)
                    .add(marshmallow_log.get())
                    .add(light_marshmallow_log.get())
                    .add(dark_marshmallow_log.get())

                tag(BlockTags.LEAVES)
                    .add(chocolate_leaves.get())
                    .add(ice_cream_leaves.get())
                    .add(candied_cherry_leaves.get())
                    .add(caramel_leaves.get())
                    .add(enchant_candy_leaves.get())
                    .add(milk_chocolate_leaves.get())
                    .add(white_chocolate_leaves.get())
                    .add(dark_chocolate_leaves.get())
            }
        }
    }
}

