package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CBlockTags
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.FluidTags
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SimpleWaterloggedBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids

class SeaweedBlock(
    private val canStack: Boolean,
    properties: Properties,
) : CandyPlantBlock(properties), SimpleWaterloggedBlock {
    override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return state.`is`(CBlockTags.seaweed_soil) || (canStack && state.`is`(this))
    }

    override fun canSurvive(state: BlockState, level: LevelReader, pos: BlockPos): Boolean {
        if (mayPlaceOn(level.getBlockState(pos.below()), level, pos.below())) {
            if (state.fluidState.`is`(FluidTags.WATER)) {
                val above = level.getBlockState(pos.above())
                if (above.fluidState.`is`(FluidTags.WATER) || canStack && above.`is`(this)) {
                    return true
                }
            }
        }
        return false
    }

    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        level: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos,
    ): BlockState {
        level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level))
        if (!state.canSurvive(level, pos)) {
            return Blocks.WATER.defaultBlockState()
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos)
    }

    @Deprecated("Deprecated in Java")
    override fun getFluidState(state: BlockState): FluidState {
        return Fluids.WATER.getSource(false)
    }
}

