package cn.jawbreakers.candycraftce.level.feature

import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import cn.jawbreakers.candycraftce.registry.CBlocks.marshmallow_slice
import cn.jawbreakers.candycraftce.registry.CBlocks.marshmallow_slice_flower
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.sugar_forest
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.sugar_river
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.FluidTags
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import kotlin.math.abs


class MarshmallowSliceFeature : Feature<NoneFeatureConfiguration>(NoneFeatureConfiguration.CODEC) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val random = context.random()
        val origin = context.origin()
        if (!isAllowedSliceBiome(level, origin)) {
            return false
        }

        var placed = false
        for (i in 0..9) {
            val target = origin.offset(
                random.nextInt(8) - random.nextInt(8),
                random.nextInt(4) - random.nextInt(4),
                random.nextInt(8) - random.nextInt(8)
            )
            val surface: BlockPos? = findWaterSurface(level, target)
            if (surface != null && isAllowedSliceBiome(level, surface) && canPlaceLily(level, surface)) {
                placed = placed or placeLily(level, surface, random)
            }
        }
        return placed
    }

    private fun findWaterSurface(level: WorldGenLevel, origin: BlockPos): BlockPos? {
        val surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.x, origin.z)
        val cursor = BlockPos.MutableBlockPos(origin.x, surfaceY + 1, origin.z)
        for (y in surfaceY + 1 downTo level.minBuildHeight) {
            cursor.setY(y)
            if (isWaterSurface(level, cursor)) {
                return cursor.immutable()
            }
            if (!level.getBlockState(cursor).isAir && !level.getFluidState(cursor).`is`(FluidTags.WATER)) {
                return null
            }
        }
        return null
    }

    private fun canPlaceLily(level: WorldGenLevel, pos: BlockPos): Boolean {
        if (!isWaterSurface(level, pos)) {
            return false
        }
        return hasNearbyDryShore(level, pos) && marshmallow_slice.get().defaultBlockState().canSurvive(level, pos)
    }

    private fun isWaterSurface(level: WorldGenLevel, pos: BlockPos): Boolean {
        return level.getBlockState(pos).isAir && level.getFluidState(pos.below()).`is`(FluidTags.WATER)
    }


    private fun isAllowedSliceBiome(level: WorldGenLevel, pos: BlockPos): Boolean {
        return isMarshmallowSliceBiome(level, pos)
                || isRiverNearMarshmallowBiome(level, pos)
                || hasNearbyCustardPudding(level, pos)
    }

    private fun isMarshmallowSliceBiome(level: WorldGenLevel, pos: BlockPos): Boolean {
        return level.getBiome(pos).`is`(sugar_forest)
    }

    private fun isRiverNearMarshmallowBiome(level: WorldGenLevel, pos: BlockPos): Boolean {
        return level.getBiome(pos).`is`(sugar_river) && hasNearbyMarshmallowBiome(level, pos)
    }

    private fun hasNearbyMarshmallowBiome(level: WorldGenLevel, pos: BlockPos): Boolean {
        val cursor = BlockPos.MutableBlockPos()
        var dz = -24
        while (dz <= 24) {
            var dx = -24
            while (dx <= 24) {
                cursor.set(pos.x + dx, pos.y, pos.z + dz)
                if (isMarshmallowSliceBiome(level, cursor)) {
                    return true
                }
                dx += 8
            }
            dz += 8
        }
        return false
    }

    private fun hasNearbyCustardPudding(level: WorldGenLevel, pos: BlockPos): Boolean {
        val cursor = BlockPos.MutableBlockPos()
        for (dz in -5..5) {
            for (dx in -5..5) {
                if (abs(dx) + abs(dz) > 6) {
                    continue
                }
                for (dy in -2..1) {
                    cursor.set(pos.x + dx, pos.y + dy, pos.z + dz)
                    if (level.getBlockState(cursor).`is`(CBlocks.custard_pudding_block.get())) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun hasNearbyDryShore(level: WorldGenLevel, pos: BlockPos): Boolean {
        val cursor = BlockPos.MutableBlockPos()
        for (dz in -8..8) {
            for (dx in -8..8) {
                if (dx * dx + dz * dz > 64) {
                    continue
                }
                for (dy in -2..2) {
                    cursor.set(pos.x + dx, pos.y + dy, pos.z + dz)
                    val state = level.getBlockState(cursor)
                    if (state.fluidState.isEmpty
                        && !state.isAir && state.isFaceSturdy(level, cursor, Direction.UP)
                        && level.getFluidState(cursor.above()).isEmpty
                    ) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun placeLily(level: WorldGenLevel, pos: BlockPos, random: RandomSource): Boolean {
        val state: BlockState =
            if (random.nextInt(15) == 0) marshmallow_slice_flower.defaultBlockState() else marshmallow_slice.defaultBlockState()
        level.setBlock(pos, state, 2 or 16)
        return true
    }
}


