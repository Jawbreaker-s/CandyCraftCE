package cn.jawbreakers.candycraftce.forge.data.providers.tags

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.registry.CBiomeTags
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes
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

                val water = listOf(sugar_oceans, sugar_river)
                val forest =
                    listOf(caramel_forest, chocolate_forest, sugar_forest, white_chocolate_forest, enchanted_forest)


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
                    .add(*allCandyBiomes.toTypedArray())

                tag(has_floating_island)
                    .add(*allCandyBiomes.filter {
                        it !in water && it !in forest
                    }.toTypedArray())
            }
        }
    }
}