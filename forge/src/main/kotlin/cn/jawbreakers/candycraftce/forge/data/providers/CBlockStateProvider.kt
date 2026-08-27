package cn.jawbreakers.candycraftce.forge.data.providers

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.block.CandyFarmlandBlock
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CItems.asItem
import cn.jawbreakers.candycraftce.utils.CUtils.key
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraftforge.client.model.generators.BlockModelBuilder
import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.client.model.generators.ConfiguredModel
import net.minecraftforge.client.model.generators.ModelFile.ExistingModelFile
import net.minecraftforge.common.data.ExistingFileHelper

class CBlockStateProvider(output: PackOutput, val efHelper: ExistingFileHelper) :
    BlockStateProvider(output, CandyCraftCE.MOD_ID, efHelper) {
    override fun registerStatesAndModels() {
        CBlocks.apply {
            listOf(
                pudding_block,
                sugar_block,
                sugar_sand,
            ).forEach { simpleBlockWithItem(it.get(), cubeAll(it.get())) }


            listOf(marshmallow, dark_marshmallow, light_marshmallow).forEach { family ->
                val (
                    original,
                    stairs,
                    slab,
                    fence,
                    fenceGate,
                    door,
                    trapdoor,
                    sign,
                    wallSign,
                ) = family
                simpleBlockWithItem(original.get(), cubeAll(original.get()))

                val texture = original.getBlockTexture()
                stairsBlock(stairs.get(), texture)
                slabBlock(slab.get(), texture, texture)
                fenceBlock(fence.get(), texture)
                fenceGateBlock(fenceGate.get(), texture)
                doorBlock(door.get(), door.getBlockTexture("_bottom"), door.getBlockTexture("_top"))
                trapdoorBlock(trapdoor.get(), trapdoor.getBlockTexture(), true)
                signBlock(sign.get(), wallSign.get(), texture)

                simpleExistedItem(stairs.get())
                simpleExistedItem(slab.get())
                itemModels().fenceInventory(fence.id.path, texture)
                itemModels().fenceGate(fenceGate.id.path, texture)
                itemModels().basicItem(door.asItem())
                itemModels().trapdoorBottom(trapdoor.id.path, trapdoor.getBlockTexture())
                itemModels().basicItem(sign.asItem())
            }
            listOf(
                marshmallow_log, dark_marshmallow_log, light_marshmallow_log,
                stripped_marshmallow_log, stripped_dark_marshmallow_log, stripped_light_marshmallow_log
            ).forEach {
                logBlock(it.get())
                simpleBlockItem(it.get(), existModelFile(it.get()))
            }

            listOf(strawberry_filled_pudding, custard_pudding_block).forEach {
                val model = models().withExistingParent(it.id.toString(), "block/grass_block")
                    .texture("particle", pudding_block.getBlockTexture())
                    .texture("bottom", pudding_block.getBlockTexture())
                    .texture("top", custard_pudding_block.getBlockTexture("_top"))
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

        }
    }

    private fun BlockModelBuilder.configured() = ConfiguredModel(this)
    private fun BlockModelBuilder.configuredArray() = arrayOf(configured())
    private fun Entry<out Block>.getBlockTexture(suffix: String = ""): ResourceLocation {
        return this.id.withPrefix("block/")
            .let { if (suffix.isEmpty()) it else it.withSuffix(suffix) }
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