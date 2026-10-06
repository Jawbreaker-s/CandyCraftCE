package cn.jawbreakers.candycraftce.forge.data.providers

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.block.CandyFarmlandBlock
import cn.jawbreakers.candycraftce.block.CaramelPortalBlock
import cn.jawbreakers.candycraftce.forge.data.providers.CItemModelProvider.Companion.generated
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.asItemEntry
import cn.jawbreakers.candycraftce.registry.CItems.asItem
import cn.jawbreakers.candycraftce.utils.CUtils.key
import cn.jawbreakers.candycraftce.utils.CUtils.mcLoc
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.GlassBlock
import net.minecraft.world.level.block.IronBarsBlock
import net.minecraftforge.client.model.generators.BlockModelBuilder
import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.client.model.generators.ConfiguredModel
import net.minecraftforge.client.model.generators.ModelFile.ExistingModelFile
import net.minecraftforge.common.data.ExistingFileHelper

class CBlockStateProvider(output: PackOutput, val efHelper: ExistingFileHelper) :
    BlockStateProvider(output, CandyCraftCE.MOD_ID, efHelper) {
    override fun registerStatesAndModels() {
        CBlocks.apply {
            //cubeAll
            listOf(
                pudding_block,
                sugar_block,
                sugar_brick,
                sugar_sand,
                caramel_bricks,
                marshmallow_planks,
                light_marshmallow_planks,
                dark_marshmallow_planks,
                caramel_block,
                cotton_candy_block,
                raspberry_cotton_candy_block,
                cookie_block,
                nougat_block,
                chiseled_nougat_block,
                square_pattern_nougat_block,
                licorice_block,
                licorice_brick,
                strawberry_ice_cream,
                mint_ice_cream,
                blueberry_ice_cream,
                chocolate_ice_cream,
                banana_ice_cream,
                ice_cream,
                honey_lamp,
                purple_trampojelly,
                trampojelly,
                red_trampojelly,
                yellow_trampojelly,
                jelly_shock_absorber,
                grenadine_ice,
                banana_block,
                chewing_gum_block,
                mint_block,
                raspberry_block,
                honeycomb_block,
                pez_block,
                jawbreaker_block,
                chocolate_stone, white_chocolate_stone,
                chocolate_cobblestone, white_chocolate_cobblestone,
                chocolate_bricks, white_chocolate_bricks,
                licorice_ore, white_licorice_ore,
                jelly_ore, white_jelly_ore,
                pez_ore, white_pez_ore,
                nougat_ore, white_nougat_ore,
                honey_ore, white_honey_ore,
                chocolate_leaves,
                ice_cream_leaves,
                candied_cherry_leaves,
                caramel_leaves,
            ).forEach { simpleBlockWithItem(it.get(), cubeAll(it.get())) }
            //side end
            listOf(candy_cane_block).forEach {
                val model = models().cubeColumn(it.id.path, it.getBlockTexture("_side"), it.getBlockTexture("_end"))
                simpleBlockWithItem(it.get(), model)
            }
            //side end +axis
//            listOf(
//            ).forEach {
//                val model = models().cubeColumn(it.id.path, it.getBlockTexture("_side"), it.getBlockTexture("_end"))
//                axisBlock(it.get(), model, model)
//                simpleExistedItem(it.get())
//            }
            listOf(candied_cherry_sack).forEach {
                val model = models().cubeBottomTop(
                    it.id.toString(),
                    it.getBlockTexture("_side"),
                    it.getBlockTexture("_bottom"),
                    it.getBlockTexture("_top")
                )
                simpleBlockWithItem(it.get(), model)
            }
            simpleBlock(fragile_grenadine_ice.get(), existModelFile(grenadine_ice.get()))
            //nougat_head
            nougat_head.also {
                val texture = nougat_block.getBlockTexture()
                val model = models().orientable(
                    it.id.path,
                    texture,
                    it.getBlockTexture(),
                    texture
                )
                horizontalBlock(it.get(), model)
                simpleBlockItem(it.get(), model)
            }
            enchant_candy_leaves.also {
                val model = models().leaves(it.id.path, it.getBlockTexture())
                simpleBlockWithItem(it.get(), model)
            }
            //cross
            listOf(
                sweet_grass_pink, sweet_grass_pale, sweet_grass_yellow, sweet_grass_red,
                chocolate_sapling,
                caramel_sapling,
                ice_cream_sapling,
                candied_cherry_sapling,
                fraise_tagada_flower,
                acid_mint_flower,
                sugar_essence_flower,
                rope_licorice,
                mint,
                banana_seaweed,
                lollipop_fruit,
                sugar_spikes, cranberry_spikes
            ).forEach {
                val texture = it.getBlockTexture()
                simpleBlock(it.get(), models().cross(it.id.path, texture))
                itemModels().generated(it.asItemEntry(), texture)
            }
            //l4 crop
            listOf(dragibus_crops, lollipop_stem).forEach {
                val crop = it.get()
                val textures = Array(crop.maxStage + 1) { i -> it.getBlockTexture("_${i}") }
                val models = textures.mapIndexed { i, tex -> models().cross("${it.id.path}_$i", tex) }
                getVariantBuilder(crop).forAllStates { state ->
                    models[crop.getStage(state)].configuredArray()
                }
            }

            listOf(marshmallow_slice, marshmallow_slice_flower, chewing_gum_puddle).forEach {
                val tex = it.getBlockTexture()
                val model = models().withExistingParent(it.id.toString(), "block/lily_pad")
                    .texture("texture", tex)
                    .texture("particle", tex)
                simpleBlock(it.get(), model)
                itemModels().generated(it.asItemEntry(), tex)
            }
            //rotation variants
            jawbreaker_light.also {
                val model = models().cubeAll(it.id.path, it.getBlockTexture())
                val builder = getVariantBuilder(it.get())
                builder.partialState().addModels(*Array(4) { i -> model.configured(rotY = i * 90) })
                simpleBlockItem(it.get(), model)

            }

            //ladder
            marshmallow_ladder.also {
                val tex = it.getBlockTexture()
                val model = models().withExistingParent(it.id.path, "block/ladder")
                    .texture("texture", tex)
                    .texture("particle", tex)
                horizontalBlock(it.get(), model)
                itemModels().generated(it.asItemEntry(), tex)
            }
            //glass
            glassFamily(
                caramel_glass to caramel_pane,
                caramel_glass_round to caramel_pane_round,
                caramel_glass_diamond to caramel_pane_diamond,
            )
            glassFamily(
                dark_caramel_glass to dark_caramel_pane,
                dark_caramel_glass_round to dark_caramel_pane_round,
                dark_caramel_glass_diamond to dark_caramel_pane_diamond,
            )
            glassFamily(
                honey_glass to honey_pane,
                honey_glass_round to honey_pane_round,
                honey_glass_diamond to honey_pane_diamond,
            )
            glassFamily(
                sugar_glass to sugar_pane,
                sugar_glass_round to sugar_pane_round,
                sugar_glass_diamond to sugar_pane_diamond,
            )
            glassFamily(
                grenadine_glass to grenadine_pane,
                grenadine_glass_grid to grenadine_pane_grid
            )
            families.forEach { f ->
                when (f) {
                    candy_cane_family -> family(
                        candy_cane_family,
                        candy_cane_family.original.getBlockTexture("_side"),
                        candy_cane_family.original.getBlockTexture("_end")
                    )

                    else -> family(f)
                }

            }

            //log
            listOf(
                marshmallow_log, dark_marshmallow_log, light_marshmallow_log,
                stripped_marshmallow_log, stripped_dark_marshmallow_log, stripped_light_marshmallow_log
            ).forEach {
                logBlock(it.get())
                simpleBlockItem(it.get(), existModelFile(it.get()))
            }

            listOf(custard_pudding_block to pudding_block).forEach { (it, base) ->
                val base = base.getBlockTexture()
                val top = custard_pudding_block.getBlockTexture("_top_overlay")
                val model = models().withExistingParent(it.id.toString(), "block/grass_block")
                    .texture("particle", base)
                    .texture("bottom", base)
                    .texture("top", top)
                    .texture("side", base)
                    .texture("overlay", custard_pudding_block.getBlockTexture("_side_overlay"))
                simpleBlockWithItem(it.get(), model)
            }

            pudding_farmland.also {
                val model = models().withExistingParent(it.id.toString(), "block/farmland")
                    .texture("dirt", pudding_block.getBlockTexture())
                    .texture("particle", pudding_block.getBlockTexture())
                    .texture("top", pudding_farmland.getBlockTexture("_top"))
                val modelMoist = models().withExistingParent(it.id.toString() + "_moist", "block/farmland")
                    .texture("dirt", pudding_block.getBlockTexture())
                    .texture("particle", pudding_block.getBlockTexture())
                    .texture("top", pudding_farmland.getBlockTexture("_top_moist"))
                getVariantBuilder(it.get())
                    .forAllStates { state ->
                        (if (state.getValue(CandyFarmlandBlock.MOISTURE) == 7) modelMoist else model).configuredArray()
                    }
                simpleBlockItem(it.get(), model)
            }
            //portal
            listOf(caramel_portal).forEach {
                val name = it.id.path
                val block = it.get()
                val tex = it.getBlockTexture()
                val baseName = "block/$name"
                val x = models().withExistingParent(baseName + "_x", "block/nether_portal_ew".mcLoc())
                    .texture("portal", tex)
                    .texture("particle", tex)
                val y = models().withExistingParent(baseName + "_y", "block/caramel_portal_y_template".modLoc())
                    .texture("portal", tex)
                    .texture("particle", tex)

                getMultipartBuilder(block).part().modelFile(x).addModel()
                    .condition(CaramelPortalBlock.X, true).end()
                    .part().modelFile(x).rotationY(90).addModel().condition(CaramelPortalBlock.Z, true).end()
                    .part().modelFile(y).addModel().condition(CaramelPortalBlock.Y, true).end()
            }
            //fluids
            particle(grenadine, grenadine.getBlockTexture("_static"))
            particle(caramel, caramel.getBlockTexture("_static"))

        }
    }

    private fun glassFamily(
        original: Pair<Entry<out GlassBlock>, Entry<out IronBarsBlock>>,
        vararg variants: Pair<Entry<out GlassBlock>, Entry<out IronBarsBlock>>,
        edge: ResourceLocation = original.first.getBlockTexture("_edge"),
    ) {
        (listOf(original) + variants).forEach { (glass, pane) ->
            val full = glass.getBlockTexture()
            simpleBlock(glass.get())
            paneBlock(pane.get(), full, edge)

            simpleExistedItem(glass.get())
            itemModels().generated(pane.asItemEntry(), full)
        }
    }

    private fun family(
        family: CBlocks.BlockFamily,
        baseTexture: ResourceLocation = family.original.getBlockTexture(),
        top: ResourceLocation = baseTexture,
        bottom: ResourceLocation = top,
        fullBlockModel: ResourceLocation = family.original.getBlockTexture(),
    ) {
        family.stairs?.also {
            stairsBlock(it.get(), baseTexture)
            simpleExistedItem(it.get())
        }
        family.slab?.also {
            slabBlock(it.get(), fullBlockModel, baseTexture, bottom, top)
            simpleExistedItem(it.get())
        }
        family.wall?.also {
            wallBlock(it.get(), baseTexture)
            itemModels().wallInventory(it.id.path, baseTexture)
        }
        family.fence?.also {
            fenceBlock(it.get(), baseTexture)
            itemModels().fenceInventory(it.id.path, baseTexture)
        }
        family.fenceGate?.also {
            fenceGateBlock(it.get(), baseTexture)
            itemModels().fenceGate(it.id.path, baseTexture)
        }
        family.door?.also {
            doorBlock(it.get(), it.getBlockTexture("_bottom"), it.getBlockTexture("_top"))
            itemModels().basicItem(it.asItem())
        }
        family.trapdoor?.also {
            trapdoorBlock(it.get(), it.getBlockTexture(), true)
            itemModels().trapdoorBottom(it.id.path, it.getBlockTexture())
        }
        family.sign?.also {
            signBlock(it.get(), family.wallSign!!.get(), baseTexture)
            itemModels().basicItem(it.asItem())
        }
    }

    private fun BlockModelBuilder.configured(rotX: Int = 0, rotY: Int = 0, uvlock: Boolean = false) =
        ConfiguredModel(this, rotX, rotY, uvlock)

    private fun BlockModelBuilder.configuredArray() = arrayOf(configured())
    private fun Entry<out Block>.getBlockTexture(suffix: String = ""): ResourceLocation {
        return this.id.withPrefix("block/")
            .let { if (suffix.isEmpty()) it else it.withSuffix(suffix) }
    }

    fun particle(block: Entry<out Block>, texture: ResourceLocation = block.getBlockTexture()) {
        val model = models().getBuilder(block.id.toString())
            .texture("particle", texture)
        simpleBlock(block.get(), model)
    }

    fun existModelFile(block: Block): ExistingModelFile {
        return existModelFile(block.key.withPrefix("block/"))
    }

    fun existModelFile(location: ResourceLocation): ExistingModelFile {
        return ExistingModelFile(location, efHelper)
    }

    fun simpleExistedItem(block: Block) {
        simpleBlockItem(block, existModelFile(block))
    }
}