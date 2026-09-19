package cn.jawbreakers.candycraftce.level.feature

import cn.jawbreakers.candycraftce.level.foliage_placer.CandiedCherryFoliagePlacer
import cn.jawbreakers.candycraftce.level.foliage_placer.EnchantFoliagePlacer
import cn.jawbreakers.candycraftce.level.foliage_placer.FancyCaramelFoliagePlacer
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.candied_cherry_leaves
import cn.jawbreakers.candycraftce.registry.CBlocks.caramel_leaves
import cn.jawbreakers.candycraftce.registry.CBlocks.chocolate_leaves
import cn.jawbreakers.candycraftce.registry.CBlocks.dark_marshmallow_log
import cn.jawbreakers.candycraftce.registry.CBlocks.light_marshmallow_log
import cn.jawbreakers.candycraftce.registry.CBlocks.marshmallow_log
import cn.jawbreakers.candycraftce.registry.CBlocks.pudding_block
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer
import net.minecraft.world.level.levelgen.feature.foliageplacers.SpruceFoliagePlacer
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider.simple
import net.minecraft.world.level.levelgen.feature.trunkplacers.DarkOakTrunkPlacer
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer
import java.util.*

//@see https://zh.minecraft.wiki/w/%E8%87%AA%E5%AE%9A%E4%B9%89%E4%B8%96%E7%95%8C%E7%94%9F%E6%88%90/configured_feature

private val pudding = simple(pudding_block.get())
fun TreeConfiguration.configure() = ConfiguredFeature(Feature.TREE, this)
fun createChocolateTree(): TreeConfiguration {
    return createStraightBlobTree(
        marshmallow_log.get(),
        chocolate_leaves.get(),
    )
        .candy()
        .build()
}

fun createChocolateFancy(): TreeConfiguration {
    return TreeConfiguration.TreeConfigurationBuilder(
        simple(marshmallow_log.get()),
        FancyTrunkPlacer(3, 11, 0),
        simple(chocolate_leaves.get()),
        FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(4), 4),
        TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))
    )
        .candy()
        .build()
}

fun createCaramelWild(): TreeConfiguration {
    return createStraightBlobTree(
        dark_marshmallow_log.get(),
        caramel_leaves.get(),
        baseHeight = 4,
        heightRandB = 1
    )
        .candy()
        .build()
}

fun createCaramelForest(): TreeConfiguration {
    return TreeConfiguration.TreeConfigurationBuilder(
        simple(dark_marshmallow_log.get()),
        StraightTrunkPlacer(16, 0, 0),
        simple(caramel_leaves.get()),
        FancyCaramelFoliagePlacer(),
        TwoLayersFeatureSize(1, 0, 4, OptionalInt.empty())
    )
        .candy()
        .build()
}

fun createEnchantTree(): TreeConfiguration {
    return TreeConfiguration.TreeConfigurationBuilder(
        simple(marshmallow_log.get()),
        DarkOakTrunkPlacer(10, 2, 1),
        simple(CBlocks.enchant_candy_leaves.get()),
        EnchantFoliagePlacer(),
        TwoLayersFeatureSize(5, 2, 7, OptionalInt.empty())
    )
        .candy()
        .build()
}

fun createCandiedCherryTree(): TreeConfiguration {
    return TreeConfiguration.TreeConfigurationBuilder(
        simple(marshmallow_log.get()),
        StraightTrunkPlacer(7, 0, 0),
        simple(candied_cherry_leaves.get()),
        CandiedCherryFoliagePlacer(),
        TwoLayersFeatureSize(1, 0, 2, OptionalInt.empty())
    )
        .candy()
        .build()
}

fun createWhiteChocolateTree(): TreeConfiguration {
    //云杉状
    return TreeConfiguration.TreeConfigurationBuilder(
        simple(light_marshmallow_log.get()),
        StraightTrunkPlacer(5, 2, 1),
        simple(CBlocks.white_chocolate_leaves.get()),
        SpruceFoliagePlacer(UniformInt.of(2, 3), UniformInt.of(0, 2), UniformInt.of(1, 2)),
        TwoLayersFeatureSize(2, 0, 2)
    )
        .candy()
        .build()
}

//简单树，默认配置为oak配置
private fun createStraightBlobTree(
    logBlock: Block,
    leavesBlock: Block,
    baseHeight: Int = 4,
    heightRandA: Int = 2,
    heightRandB: Int = 0,
    radius: Int = 2,
    foliageHeight: Int = 3,
): TreeConfiguration.TreeConfigurationBuilder {
    return TreeConfiguration.TreeConfigurationBuilder(
        simple(logBlock),
        StraightTrunkPlacer(baseHeight, heightRandA, heightRandB),
        simple(leavesBlock),
        BlobFoliagePlacer(ConstantInt.of(radius), ConstantInt.of(0), foliageHeight),
        TwoLayersFeatureSize(baseHeight - foliageHeight, 0, radius - 1)
    )

}

private fun TreeConfiguration.TreeConfigurationBuilder.candy(): TreeConfiguration.TreeConfigurationBuilder {
    return this.ignoreVines().dirt(pudding)
}

