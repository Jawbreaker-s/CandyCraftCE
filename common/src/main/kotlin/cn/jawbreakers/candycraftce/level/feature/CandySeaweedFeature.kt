package cn.jawbreakers.candycraftce.level.feature

import cn.jawbreakers.candycraftce.registry.CBlocks.banana_seaweed
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import cn.jawbreakers.candycraftce.registry.CBlocks.mint
import cn.jawbreakers.candycraftce.registry.CBlocks.rope_licorice
import net.minecraft.core.BlockPos
import net.minecraft.tags.FluidTags
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

class CandySeaweedFeature : Feature<NoneFeatureConfiguration>(NoneFeatureConfiguration.CODEC) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val random = context.random()
        val origin = context.origin()
        var placed = false

        repeat(64) {
            val target = origin.offset(
                random.nextInt(8) - random.nextInt(8),
                random.nextInt(4) - random.nextInt(4),
                random.nextInt(8) - random.nextInt(8)
            )

            if (random.nextBoolean()) {
                val state = if (random.nextBoolean()) mint.defaultBlockState() else banana_seaweed.defaultBlockState()
                placed = placed or tryPlace(level, target, state)
            } else {
                val height = random.nextInt(4) + 1
                for (y in 0..<height) {
                    placed =
                        placed or tryPlace(level, target.above(y), rope_licorice.defaultBlockState())
                }
            }
        }

        return placed
    }

    private fun tryPlace(level: WorldGenLevel, pos: BlockPos, state: BlockState): Boolean {
        if (level.isOutsideBuildHeight(pos) || !level.getFluidState(pos).`is`(FluidTags.WATER)) {
            return false
        }
        if (!state.canSurvive(level, pos)) {
            return false
        }
        level.setBlock(pos, state, 2 or 16)
        return true
    }
}
