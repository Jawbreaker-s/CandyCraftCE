package cn.jawbreakers.candycraftce.level.feature

import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import cn.jawbreakers.candycraftce.registry.CBlocks.grenadine
import cn.jawbreakers.candycraftce.registry.CBlocks.liquid_chocolate
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

/**
 * The 1.12.2 underground lake generator, adapted to the modern feature API.
 * Lower cells are filled with source fluid and the upper cells are open,
 * preserving the old cave/lake shape and solid-boundary checks.
 */
class CandyLiquidLakeFeature(val mode: FluidMode) : Feature<NoneFeatureConfiguration>(NoneFeatureConfiguration.CODEC) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val random = context.random()
        val origin = context.origin()
        val baseX = origin.x - 8
        var baseY = origin.y
        val baseZ = origin.z - 8

        while (baseY > level.minBuildHeight + 5 && level.isEmptyBlock(BlockPos(baseX, baseY, baseZ))) {
            baseY--
        }
        baseY -= 4
        if (baseY <= level.minBuildHeight + 1) {
            return false
        }

        val lake = BooleanArray(16 * 16 * 8)
        val ellipsoids = random.nextInt(4) + 4
        repeat(ellipsoids) {
            val width = random.nextDouble() * 6.0 + 3.0
            val height = random.nextDouble() * 4.0 + 2.0
            val depth = random.nextDouble() * 6.0 + 3.0
            val centerX = random.nextDouble() * (16.0 - width - 2.0) + 1.0 + width / 2.0
            val centerY = random.nextDouble() * (8.0 - height - 4.0) + 2.0 + height / 2.0
            val centerZ = random.nextDouble() * (16.0 - depth - 2.0) + 1.0 + depth / 2.0

            for (x in 1..14) {
                for (z in 1..14) {
                    for (y in 1..6) {
                        val dx = (x - centerX) / (width / 2.0)
                        val dy = (y - centerY) / (height / 2.0)
                        val dz = (z - centerZ) / (depth / 2.0)
                        if (dx * dx + dy * dy + dz * dz < 1.2) {
                            lake[index(x, z, y)] = true
                        }
                    }
                }
            }
        }

        val fluid = mode.chooseFluid(random)
        for (x in 0..15) {
            for (z in 0..15) {
                for (y in 0..7) {
                    if (lake[index(x, z, y)] || !touchesLake(lake, x, z, y)) {
                        continue
                    }
                    val pos = BlockPos(baseX + x, baseY + y, baseZ + z)
                    val state = level.getBlockState(pos)
                    if (y >= 4 && !state.fluidState.isEmpty) {
                        return false
                    }
                    @Suppress("DEPRECATION")
                    if (y < 4 && !state.isSolid && !state.`is`(fluid.block)) {
                        return false
                    }
                }
            }
        }

        var placed = false
        for (x in 0..15) {
            for (z in 0..15) {
                for (y in 0..7) {
                    if (lake[index(x, z, y)]) {
                        val pos = BlockPos(baseX + x, baseY + y, baseZ + z)
                        level.setBlock(pos, if (y >= 4) Blocks.AIR.defaultBlockState() else fluid, 2 or 16)
                        placed = true
                    }
                }
            }
        }
        return placed
    }


    private fun touchesLake(lake: BooleanArray, x: Int, z: Int, y: Int): Boolean {
        return (x > 0 && lake[index(x - 1, z, y)]) || (x < 15 && lake[index(x + 1, z, y)])
                || (z > 0 && lake[index(x, z - 1, y)]) || (z < 15 && lake[index(x, z + 1, y)])
                || (y > 0 && lake[index(x, z, y - 1)]) || (y < 7 && lake[index(x, z, y + 1)])
    }

    private fun index(x: Int, z: Int, y: Int): Int {
        return (x * 16 + z) * 8 + y
    }

    enum class FluidMode {
        WATER_OR_GRENADINE {
            override fun chooseFluid(random: RandomSource): BlockState {
                return if (random.nextInt(4) == 0) grenadine.defaultBlockState() else Blocks.WATER.defaultBlockState()
            }
        },
        CHOCOLATE {
            override fun chooseFluid(random: RandomSource): BlockState {
                return liquid_chocolate.defaultBlockState()
            }
        };

        abstract fun chooseFluid(random: RandomSource): BlockState
    }
}
