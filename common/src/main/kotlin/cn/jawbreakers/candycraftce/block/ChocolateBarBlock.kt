package cn.jawbreakers.candycraftce.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class ChocolateBarBlock(properties: Properties) : FacingModelBlock(properties) {
    companion object {
        private val SHAPE_NS: VoxelShape = box(2.5, 0.0, 5.5, 13.5, 14.5, 10.5)
        private val SHAPE_EW: VoxelShape = box(5.5, 0.0, 2.5, 10.5, 14.5, 13.5)
    }

    @Deprecated("Deprecated in Java")
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        val facing = state.getValue(FACING)
        return if (facing == Direction.NORTH || facing == Direction.SOUTH) SHAPE_NS else SHAPE_EW
    }

    @Deprecated("Deprecated in Java")
    override fun canSurvive(state: BlockState, level: LevelReader, pos: BlockPos): Boolean {
        val below = pos.below()
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun neighborChanged(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        block: Block,
        fromPos: BlockPos,
        isMoving: Boolean,
    ) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true)
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving)
    }
}
