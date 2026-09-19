package cn.jawbreakers.candycraftce.level.feature

import cn.jawbreakers.candycraftce.registry.CBlockTags
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

class GummyWormFeature : Feature<NoneFeatureConfiguration>(NoneFeatureConfiguration.CODEC) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val random = context.random()
        val origin = context.origin()
        val surfacePos = BlockPos(
            origin.x,
            level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.x, origin.z),
            origin.z
        )

        if (isWormBlock(level.getBlockState(surfacePos.below()))) {
            return false
        }

        val state: BlockState = randomWormState(random)
        return when (random.nextInt(3)) {
            0 -> generateWormFlat(level, surfacePos, random.nextInt(10) + 7, state, random)
            1 -> generateWormStraight(level, surfacePos, random.nextInt(12) + 6, random.nextInt(4) + 3, state)
            else -> generateWormArc(level, surfacePos, state, random)
        }
    }

    private fun generateWormStraight(
        level: WorldGenLevel,
        position: BlockPos,
        below: Int,
        above: Int,
        state: BlockState,
    ): Boolean {
        val yState = state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
        var placed = false
        for (i in -below..<above) {
            placed = placed or place(level, position.above(i), yState)
        }
        return placed
    }

    private fun generateWormArc(
        level: WorldGenLevel,
        position: BlockPos,
        state: BlockState,
        random: RandomSource,
    ): Boolean {
        val height = random.nextInt(2) + 2
        val startDepth = random.nextInt(4) + 4
        val direction = Direction.Plane.HORIZONTAL.getRandomDirection(random)
        val vertical = state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
        var pos = position.below(startDepth)
        var placed = false

        for (i in 0..height + startDepth) {
            pos = pos.above()
            placed = placed or place(level, pos, vertical)
        }

        val horizontal =
            state.setValue(RotatedPillarBlock.AXIS, direction.axis)
        for (i in 0..2 + random.nextInt(2)) {
            pos = pos.relative(direction)
            placed = placed or place(level, pos, horizontal)
        }

        while (isAirOrLiquid(level, pos.below())) {
            pos = pos.below()
            placed = placed or place(level, pos, vertical)
        }
        for (i in 0..4 + random.nextInt(4)) {
            pos = pos.below()
            placed = placed or place(level, pos, vertical)
        }
        return placed
    }

    private fun generateWormFlat(
        level: WorldGenLevel,
        position: BlockPos,
        length: Int,
        state: BlockState,
        random: RandomSource,
    ): Boolean {
        var pos = position.above()
        var direction = Direction.Plane.HORIZONTAL.getRandomDirection(random)
        var lastTurnDir = 0
        var hasTurned = false
        var placed = false

        var i = 0
        while (i <= length) {
            placed = placed or place(
                level,
                pos,
                state.setValue(RotatedPillarBlock.AXIS, direction.axis)
            )

            if (hasTurned) {
                hasTurned = false
            } else if (random.nextInt(3) == 0) {
                direction = direction.getClockWise()
                if (lastTurnDir == 1 || lastTurnDir == 0 && random.nextBoolean()) {
                    direction = direction.opposite
                    lastTurnDir = -1
                } else {
                    lastTurnDir = 1
                }
                hasTurned = true
            }

            while (isAirOrLiquid(level, pos.below())) {
                pos = pos.below()
                placed = placed or place(
                    level,
                    pos,
                    state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                )
                i++
            }

            while (!isAirOrLiquid(level, pos.relative(direction))) {
                pos = pos.above()
                if (!isAirOrLiquid(level, pos)) {
                    return placed
                }
                placed = placed or place(
                    level,
                    pos,
                    state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                )
                i++
            }

            pos = pos.relative(direction)
            i++
        }
        return placed
    }

    private fun place(level: WorldGenLevel, pos: BlockPos, state: BlockState): Boolean {
        if (level.isOutsideBuildHeight(pos)) {
            return false
        }
        level.setBlock(pos, state, 2 or 16)
        return true
    }

    private fun isAirOrLiquid(level: WorldGenLevel, pos: BlockPos): Boolean {
        return !level.isOutsideBuildHeight(pos) && (level.isEmptyBlock(pos) || !level.getFluidState(pos).isEmpty)
    }

    private fun isWormBlock(state: BlockState): Boolean {
        return state.`is`(CBlockTags.worm_blocks)
    }

    private fun randomWormState(random: RandomSource): BlockState {
        return when (random.nextInt(5)) {
            1 -> CBlocks.orange_gummy_family
            2 -> CBlocks.yellow_gummy_family
            3 -> CBlocks.white_gummy_family
            4 -> CBlocks.green_gummy_family
            else -> CBlocks.red_gummy_family
        }.worm.defaultBlockState()
    }
}

