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
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

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

        val YP_AXIS_AABB: VoxelShape = box(6.5, 8.0, 6.5, 9.5, 16.0, 9.5)
        val YN_AXIS_AABB: VoxelShape = box(6.5, 0.0, 6.5, 9.5, 8.0, 9.5)
        val ZP_AXIS_AABB: VoxelShape = box(6.5, 6.5, 8.0, 9.5, 9.5, 16.0)
        val ZN_AXIS_AABB: VoxelShape = box(6.5, 6.5, 0.0, 9.5, 9.5, 8.0)
        val XN_AXIS_AABB: VoxelShape = box(0.0, 6.5, 6.5, 8.0, 9.5, 9.5)
        val XP_AXIS_AABB: VoxelShape = box(8.0, 6.5, 6.5, 16.0, 9.5, 9.5)

        // bit0=YP(UP) bit1=YN(DOWN) bit2=ZN(NORTH) bit3=ZP(SOUTH) bit4=XP(EAST) bit5=XN(WEST)
        val AABBS = Array<VoxelShape>(64) { flag ->
            Shapes.or(
                Shapes.empty(),
                *buildList {
                    if (flag and 1 != 0) add(YP_AXIS_AABB)
                    if (flag and 2 != 0) add(YN_AXIS_AABB)
                    if (flag and 4 != 0) add(ZN_AXIS_AABB)
                    if (flag and 8 != 0) add(ZP_AXIS_AABB)
                    if (flag and 16 != 0) add(XP_AXIS_AABB)
                    if (flag and 32 != 0) add(XN_AXIS_AABB)
                }.toTypedArray()
            )
        }


        fun key(state: BlockState): Int {
            val axis = state.getValue(AXIS)
            val y = axis == Direction.Axis.Y
            val x = axis == Direction.Axis.X
            val z = axis == Direction.Axis.Z
            return key(
                y || state.getValue(UP),
                y || state.getValue(DOWN),
                z || state.getValue(NORTH),
                z || state.getValue(SOUTH),
                x || state.getValue(EAST),
                x || state.getValue(WEST),
            )
        }

        fun key(
            up: Boolean,
            down: Boolean,
            north: Boolean,
            south: Boolean,
            east: Boolean,
            west: Boolean,
        ): Int {
            var idx = 0
            idx = idx or if (up) 1 else 0
            idx = idx or if (down) 2 else 0
            idx = idx or if (north) 4 else 0
            idx = idx or if (south) 8 else 0
            idx = idx or if (east) 16 else 0
            idx = idx or if (west) 32 else 0
            return idx
        }

        fun getShape(
            up: Boolean,
            down: Boolean,
            north: Boolean,
            south: Boolean,
            east: Boolean,
            west: Boolean,
        ): VoxelShape {
            return AABBS[key(up, down, north, south, east, west)]
        }
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

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        return withConnections(super.getStateForPlacement(context)!!, context.level, context.clickedPos)
    }

    @Deprecated("Deprecated in Java")
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return AABBS[key(state)]
    }

    @Deprecated("Deprecated in Java")
    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        level: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos,
    ): BlockState {
        super.updateShape(state, direction, neighborState, level, pos, neighborPos)
        return state.setValue(
            connectionProperty(direction),
            connectsTo(state, neighborState)
        )
    }

    private fun withConnections(state: BlockState, level: BlockGetter, pos: BlockPos): BlockState {
        var state = state
        val axis = state.getValue(AXIS)
        for (direction in Direction.entries) {
            if (axis == direction.axis) continue
            state = state.setValue(
                connectionProperty(direction),
                connectsTo(state, level.getBlockState(pos.relative(direction)))
            )
        }
        return state
    }

    private fun connectsTo(state: BlockState, neighborState: BlockState): Boolean {
        return neighborState.`is`(state.block)
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
