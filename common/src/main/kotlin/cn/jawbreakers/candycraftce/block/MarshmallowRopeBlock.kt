package cn.jawbreakers.candycraftce.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.ChainBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.material.Fluids

/**
 * Marshmallow rope: climbable from all four sides and visually connects to
 * neighbouring marshmallow ropes facing a different axis, so horizontal and
 * vertical sections join without a gap at the corner. Parallel ropes of the
 * same axis never grow connector stubs.
 */
class MarshmallowRopeBlock(properties: Properties) : ChainBlock(properties) {
    companion object {
        val AXIS: EnumProperty<Direction.Axis> = ChainBlock.AXIS
        val UP: BooleanProperty = BlockStateProperties.UP
        val DOWN: BooleanProperty = BlockStateProperties.DOWN
        val NORTH: BooleanProperty = BlockStateProperties.NORTH
        val SOUTH: BooleanProperty = BlockStateProperties.SOUTH
        val EAST: BooleanProperty = BlockStateProperties.EAST
        val WEST: BooleanProperty = BlockStateProperties.WEST
    }

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
        )
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        super.createBlockStateDefinition(builder)
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        return withConnections(super.getStateForPlacement(context)!!, context.level, context.clickedPos)
    }

    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        level: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos,
    ): BlockState {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level))
        }
        // Directions along the rope's own axis are already covered by the
        // full-length model; only perpendicular faces need a connector stub,
        // and only when the neighbouring rope faces a different axis.
        if (direction.axis === state.getValue(AXIS)) {
            return state
        }
        return state.setValue(
            connectionProperty(direction),
            connectsTo(state, neighborState)
        )
    }

    private fun withConnections(state: BlockState, level: BlockGetter, pos: BlockPos): BlockState {
        var state = state
        for (direction in Direction.entries) {
            if (direction.axis !== state.getValue(AXIS)) {
                state = state.setValue(
                    connectionProperty(direction),
                    connectsTo(state, level.getBlockState(pos.relative(direction)))
                )
            }
        }
        return state
    }

    private fun connectsTo(state: BlockState, neighborState: BlockState): Boolean {
        return neighborState.`is`(state.block) &&
                neighborState.getValue(AXIS) != state.getValue(AXIS)
    }

    private fun connectionProperty(direction: Direction): BooleanProperty =
        when (direction) {
            Direction.UP -> UP
            Direction.DOWN -> DOWN
            Direction.NORTH -> NORTH
            Direction.SOUTH -> SOUTH
            Direction.WEST -> WEST
            Direction.EAST -> EAST
        }
}
