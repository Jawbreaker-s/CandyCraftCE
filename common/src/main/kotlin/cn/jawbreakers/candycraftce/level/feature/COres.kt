package cn.jawbreakers.candycraftce.level.feature

import cn.jawbreakers.candycraftce.registry.CBlockTags
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import cn.jawbreakers.candycraftce.registry.CBlocks.honey_ore
import cn.jawbreakers.candycraftce.registry.CBlocks.licorice_ore
import cn.jawbreakers.candycraftce.registry.CBlocks.pez_ore
import cn.jawbreakers.candycraftce.registry.CBlocks.white_honey_ore
import cn.jawbreakers.candycraftce.registry.CBlocks.white_licorice_ore
import cn.jawbreakers.candycraftce.registry.CBlocks.white_pez_ore
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest

fun OreConfiguration.configure() = ConfiguredFeature(Feature.ORE, this)

private val all = TagMatchTest(CBlockTags.all_replaceable)
private val white_chocolate_rule: RuleTest = TagMatchTest(CBlockTags.white_ore_replaceable)
private val chocolate_rule: RuleTest = TagMatchTest(CBlockTags.ore_replaceable)

//dirt
fun createPudding() = OreConfiguration(all, CBlocks.pudding_block.defaultBlockState(), 33)

//gravel
fun createSugarSand() = OreConfiguration(all, CBlocks.sugar_sand.defaultBlockState(), 33)

//
//iron-like
val licorice_target = targeting(licorice_ore.defaultBlockState(), white_licorice_ore.defaultBlockState())
fun createLicoriceOre() = OreConfiguration(licorice_target, 9)
fun createLicoriceOreSmall() = OreConfiguration(licorice_target, 4)

//coal-like
private val honey_target = targeting(honey_ore.defaultBlockState(), white_honey_ore.defaultBlockState())
fun createHoneyOre() = OreConfiguration(honey_target, 17)
fun createHoneyOreBuried() = OreConfiguration(honey_target, 17, 0.5F)

//emerald-like
private val nougat_target = targeting(honey_ore.defaultBlockState(), white_honey_ore.defaultBlockState())
fun createNougatOre() = OreConfiguration(nougat_target, 3)

//gold-like
private val jelly_target = targeting(honey_ore.defaultBlockState(), white_honey_ore.defaultBlockState())
fun createJellyOre() = OreConfiguration(jelly_target, 9)
fun createJellyOreBuried() = OreConfiguration(jelly_target, 9, 0.5F)

//diamond-like
private val pez_target = targeting(pez_ore.defaultBlockState(), white_pez_ore.defaultBlockState())
fun createPezOreSmall() = OreConfiguration(pez_target, 4, 0.5F)
fun createPezOreLarge() = OreConfiguration(pez_target, 12, 0.7F)
fun createPezOreBuried() = OreConfiguration(pez_target, 8, 1.0F)

private fun targeting(chocolate: BlockState, whiteChocolate: BlockState) = listOf(
    OreConfiguration.target(white_chocolate_rule, whiteChocolate),
    OreConfiguration.target(chocolate_rule, chocolate)
)


