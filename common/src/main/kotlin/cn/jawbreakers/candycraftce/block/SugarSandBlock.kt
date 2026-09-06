package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.utils.CLevelUtils
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.FluidTags
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.FallingBlock
import net.minecraft.world.level.block.state.BlockState

class SugarSandBlock(properties: Properties) : FallingBlock(properties) {

    override fun updateShape(
        state: BlockState,
        facing: Direction,
        facingState: BlockState,
        level: LevelAccessor,
        pos: BlockPos,
        facingPos: BlockPos,
    ): BlockState {
        return if (isReachWater(level, pos)) {
            CBlocks.sugar_block.get().defaultBlockState()
        } else {
            super.updateShape(state, facing, facingState, level, pos, facingPos)
        }
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        return if (isReachWater(context.level, context.clickedPos)) {
            CBlocks.sugar_block.get().defaultBlockState()
        } else {
            super.getStateForPlacement(context)
        }
    }

    private fun isReachWater(level: LevelAccessor, pos: BlockPos) =
        CLevelUtils.getNeighbourPos(pos).anyMatch { level.getFluidState(it).`is`(FluidTags.WATER) }

}