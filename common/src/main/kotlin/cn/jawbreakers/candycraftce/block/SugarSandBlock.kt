package cn.jawbreakers.candycraftce.block

import net.minecraft.world.level.block.FallingBlock

class SugarSandBlock(properties: Properties) : FallingBlock(properties) {

//    override fun updateShape(
//        state: BlockState,
//        facing: Direction,
//        facingState: BlockState,
//        level: LevelAccessor,
//        pos: BlockPos,
//        facingPos: BlockPos,
//    ): BlockState {
//        return if (isReachWater(level, pos)) {
//            CBlocks.sugar_block.get().defaultBlockState()
//        } else {
//            super.updateShape(state, facing, facingState, level, pos, facingPos)
//        }
//    }
//
//    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
//        return if (isReachWater(context.level, context.clickedPos)) {
//            CBlocks.sugar_block.get().defaultBlockState()
//        } else {
//            super.getStateForPlacement(context)
//        }
//    }
//
//    private fun isReachWater(level: LevelAccessor, pos: BlockPos) =
//        CLevelUtils.getNeighbourPos(pos).anyMatch { level.getFluidState(it).`is`(FluidTags.WATER) }

}