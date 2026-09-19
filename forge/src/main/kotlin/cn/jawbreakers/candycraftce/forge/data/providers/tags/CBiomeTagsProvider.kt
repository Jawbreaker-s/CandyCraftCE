package cn.jawbreakers.candycraftce.forge.data.providers.tags

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.registry.CBiomeTags
import cn.jawbreakers.candycraftce.registry.CBiomes
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.BiomeTagsProvider
import net.minecraftforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class CBiomeTagsProvider(
    output: PackOutput,
    lookup: CompletableFuture<HolderLookup.Provider>,
    efHelper: ExistingFileHelper,
) : BiomeTagsProvider(output, lookup, MOD_ID, efHelper) {
    override fun addTags(provider: HolderLookup.Provider) {
        CBiomeTags.apply {
            CBiomes.apply {
                tag(is_cold)
                    .add(ice_cream_plains)
                    .add(ice_cream_sky_mountains)
                    .add(white_chocolate_forest)

                tag(has_essence_flower)
                    .add(enchanted_forest)
                    .addTag(is_cold)


                tag(has_mint_flower)
                    .add(caramel_forest)

                tag(candy_biomes)
                    .add(caramel_forest)
                    .add(chocolate_forest)
                    .add(cotton_candy_plains)
                    .add(gummy_swamp)
                    .add(ice_cream_plains)
                    .add(ice_cream_sky_mountains)
                    .add(white_chocolate_forest)
                    .add(enchanted_forest)
                    .add(sugar_forest)
                    .add(pudding_hill)
                    .add(sugar_oceans)
                    .add(pudding_plains)
                    .add(sugar_river)
            }
        }
    }
}