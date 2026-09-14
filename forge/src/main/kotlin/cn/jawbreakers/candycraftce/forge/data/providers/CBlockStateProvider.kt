package cn.jawbreakers.candycraftce.forge.data.providers

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.block.CandyFarmlandBlock
import cn.jawbreakers.candycraftce.forge.data.providers.CItemModelProvider.Companion.generated
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.asItemEntry
import cn.jawbreakers.candycraftce.registry.CItems.asItem
import cn.jawbreakers.candycraftce.utils.CUtils.key
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
                caramel_brick,
                marshmallow_planks,
                light_marshmallow_planks,
                dark_marshmallow_planks,
                crystallized_sugar,
                pink_crystallized_sugar,
                smooth_pink_sugar,
                pink_sugar_brick,
                caramel_block,
                cotton_candy_block,
                raspberry_cotton_candy_block,
                cookie_block,
                nougat_block,
                chiseled_nougat_block,
                square_pattern_nougat_block,
                licorice_block,
                licorice_brick,
                milk_chocolate_block,
                milk_chocolate_brick,
                white_chocolate_block,
                white_chocolate_brick,
                dark_chocolate_block,
                dark_chocolate_brick,
                strawberry_ice_cream,
                mint_ice_cream,
                blueberry_ice_cream,
                chocolate_ice_cream,
                banana_ice_cream,
                ice_cream,
                waffle_block,
                wafer_cone_block,
                solid_wafer_block,
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
                chocolate_stone,
                chocolate_cobblestone,
                white_brownie_block,
                milk_brownie_block,
                cake_block,
                licorice_ore,
                jelly_ore,
                pez_ore,
                nougat_ore,
                crystallized_cookie_ore,
                cookie_ore,
                magic_candy_ore,
                honey_ore,
                chocolate_leaves,
                ice_cream_leaves,
                candied_cherry_leaves,
                caramel_leaves,
                milk_chocolate_leaves,
                white_chocolate_leaves,
                dark_chocolate_leaves,
            ).forEach { simpleBlockWithItem(it.get(), cubeAll(it.get())) }
            //side end
            listOf(candy_cane_block, dark_brownie_block).forEach {
                val model = models().cubeColumn(it.id.path, it.getBlockTexture("_side"), it.getBlockTexture("_end"))
                simpleBlockWithItem(it.get(), model)
            }
            //side end +axis
            listOf(
                white_hard_candy_block,
                red_hard_candy_block,
                green_hard_candy_block,
                yellow_hard_candy_block,
                orange_hard_candy_block,
                light_blue_hard_candy_block,
                pink_hard_candy_block,
                purple_hard_candy_block,
                white_red_hard_candy_block,
                white_green_hard_candy_block,
                white_yellow_hard_candy_block,
                white_orange_hard_candy_block,
                white_light_blue_hard_candy_block,
                white_pink_hard_candy_block,
                white_purple_hard_candy_block,
                red_green_hard_candy_block,
                dark_brownie_cake_roll_block,
                milk_brownie_cake_roll_block,
                white_brownie_cake_roll_block,
            ).forEach {
                val model = models().cubeColumn(it.id.path, it.getBlockTexture("_side"), it.getBlockTexture("_end"))
                axisBlock(it.get(), model, model)
                simpleExistedItem(it.get())
            }
            simpleBlock(fragile_grenadine_ice.get(), existModelFile(grenadine_ice.get()))
            //chiffon_cake
            listOf(
                dark_chiffon_cake_block to dark_brownie_block,
                milk_chiffon_cake_block to milk_brownie_block,
                white_chiffon_cake_block to white_brownie_block,
            ).forEach { (it, soil) ->
                val model = models().cubeBottomTop(
                    it.id.path,
                    it.getBlockTexture("_side"),
                    soil.getBlockTexture(if (soil == dark_brownie_block) "_end" else ""),
                    it.getBlockTexture("_top")
                )
                axisBlock(it.get(), model, model)
                simpleExistedItem(it.get())
            }
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
                wafer_chocolate_sapling,
                cotton_candy_sapling,
                fraise_tagada_flower,
                acid_mint_flower,
                sugar_essence_flower,
                rope_licorice,
                mint,
                banana_seaweed,
            ).forEach {
                val texture = it.getBlockTexture()
                simpleBlock(it.get(), models().cross(it.id.path, texture))
                itemModels().generated(it.asItemEntry(), texture)
            }
            listOf(marshmallow_slice, marshmallow_slice_flower).forEach {
                models().withExistingParent(it.id.toString(), "block/lily_pad")
                    .texture("texture", it.getBlockTexture())
                    .texture("particle", it.getBlockTexture())
            }
            //rotation variants
            jawbreaker_light.also {
                val model = models().cubeAll(it.id.path, it.getBlockTexture())
                val builder = getVariantBuilder(it.get())
                builder.partialState().addModels(*Array(4) { i -> model.configured(rotY = i * 90) })
                simpleBlockItem(it.get(), model)

            }
            listOf(dark_chocolate_mushroom, white_chocolate_mushroom, milk_chocolate_mushroom).forEach {
                val models = (0..1)
                    .map { i -> models().cross("${it.id.path}_$i", it.getBlockTexture("_$i")).configured() }
                    .toTypedArray()
                getVariantBuilder(it.get())
                    .partialState().addModels(*models)
                itemModels().generated(it.asItemEntry(), it.getBlockTexture("_0"))
            }
            chocolate_covered_white_brownie.also {
                val model = models().cubeBottomTop(
                    it.id.path,
                    it.getBlockTexture("_side"),
                    white_brownie_block.getBlockTexture(),
                    chocolate_stone.getBlockTexture(),
                )
                simpleBlockWithItem(it.get(), model)
            }
            candy_cotton_grass_block.also {
                val model = models().cubeBottomTop(
                    it.id.path,
                    it.getBlockTexture("_side"),
                    milk_brownie_block.getBlockTexture(),
                    cotton_candy_block.getBlockTexture("")
                )
                simpleBlockWithItem(it.get(), model)
            }
            //ladder
            marshmallow_ladder.also {
                val tex = it.getBlockTexture()
                val model = models().withExistingParent(it.id.path, "block/ladder")
                    .texture("texture", tex)
                    .texture("particle", tex);
                horizontalBlock(it.get(), model)
                itemModels().generated(it.asItemEntry(), tex);
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

            //family
            family(
                candy_cane_family,
                candy_cane_family.original.getBlockTexture("_side"),
                candy_cane_family.original.getBlockTexture("_end")
            )
            //simple_family
            listOf(
                marshmallow_family,
                dark_marshmallow_family,
                light_marshmallow_family,
                sugar_family,
                sugar_brick_family,
                cotton_candy_family,
                raspberry_cotton_candy_family,
                ice_cream_family,
                pink_sugar_brick_family,
                smooth_pink_sugar_family,
                caramel_family,
                caramel_brick_family,
                cookie_family,
                nougat_family,
                licorice_family,
                licorice_brick_family,
                milk_chocolate_brick_family,
                milk_chocolate_family,
                white_chocolate_brick_family,
                white_chocolate_family,
                dark_chocolate_brick_family,
                dark_chocolate_family,
                strawberry_ice_cream_family,
                mint_ice_cream_family,
                blueberry_ice_cream_family,
                chocolate_ice_cream_family,
                banana_ice_cream_family,
                chocolate_cobblestone_family,
                chocolate_stone_family
            ).forEach(::family)

            gummyFamily(red_gummy_family)
            gummyFamily(orange_gummy_family)
            gummyFamily(yellow_gummy_family)
            gummyFamily(white_gummy_family)
            gummyFamily(green_gummy_family)
            //log
            listOf(
                marshmallow_log, dark_marshmallow_log, light_marshmallow_log,
                stripped_marshmallow_log, stripped_dark_marshmallow_log, stripped_light_marshmallow_log
            ).forEach {
                logBlock(it.get())
                simpleBlockItem(it.get(), existModelFile(it.get()))
            }

            listOf(milk_chocolate_bar_block, white_chocolate_bar_block, dark_chocolate_bar_block).forEach {
                val model = models().withExistingParent(it.id.toString(), "block/chocolate_bar_block".modLoc())
                    .texture("front", it.getBlockTexture("_front"))
                    .texture("back", it.getBlockTexture("_back"))
                horizontalBlock(it.get(), model)
                simpleBlockItem(it.get(), model)
            }
            wafer_stick_block.also {
                val model = existModelFile(it.get())
                axisBlock(it.get(), model, model)
                simpleBlockItem(it.get(), model)
            }

            listOf(strawberry_filled_pudding, custard_pudding_block).forEach {
                val model = models().withExistingParent(it.id.toString(), "block/grass_block")
                    .texture("particle", pudding_block.getBlockTexture())
                    .texture("bottom", pudding_block.getBlockTexture())
                    .texture(
                        "top", custard_pudding_block.getBlockTexture(
//                            if (it == custard_pudding_block) "_top_overlay" else "_top"
                            "_top_overlay"
                        )
                    )
                    .texture("side", it.getBlockTexture("_side"))
                    .texture("overlay", custard_pudding_block.getBlockTexture("_side_overlay"))
                simpleBlockWithItem(it.get(), model)
            }

            pudding_farmland.also {
                val model = models().withExistingParent(it.id.toString(), "block/farmland")
                    .texture("dirt", pudding_block.getBlockTexture())
                    .texture("particle", pudding_block.getBlockTexture())
                    .texture("top", pudding_farmland.getBlockTexture("_top"))
                val modelMoist = models().withExistingParent(it.id.toString(), "block/farmland")
                    .texture("dirt", pudding_block.getBlockTexture())
                    .texture("particle", pudding_block.getBlockTexture())
                    .texture("top", pudding_farmland.getBlockTexture("_top_moist"))
                getVariantBuilder(it.get())
                    .forAllStates { state ->
                        (if (state.getValue(CandyFarmlandBlock.MOISTURE) == 7) modelMoist else model).configuredArray()
                    }
                simpleBlockItem(it.get(), model)
            }

            //fluids
            particle(grenadine, grenadine.getBlockTexture("_static"))
            particle(caramel, caramel.getBlockTexture("_static"))
            particle(liquid_chocolate, liquid_chocolate.getBlockTexture("_still"))
            particle(liquid_candy, liquid_candy.getBlockTexture("_still"))

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

    private fun gummyFamily(family: CBlocks.GummyFamily) {
        val gummySolid = "block/gummy_block_solid".modLoc()
//        val wormSide = "block/gummy_worm_block_side".modLoc()
        val gummyTransluscent = "block/gummy_block_transluscent".modLoc()

        val modelTransluscent = models().leaves(family.block.id.toString(), gummyTransluscent)
        simpleBlockWithItem(family.block.get(), modelTransluscent)

        val modelSolid = models().leaves(family.hardened.id.toString(), gummySolid)
        simpleBlockWithItem(family.hardened.get(), modelSolid)

        val wormModel = existModelFile("block/gummy_worm_block".modLoc())
        axisBlock(family.worm.get(), wormModel, wormModel)
        simpleBlockItem(family.worm.get(), wormModel)
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