package cn.jawbreakers.candycraftce.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import java.util.*

class WaferStickBlock(properties: Properties) : RotatedPillarBlock(properties) {
    @Deprecated("Deprecated in Java")
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
        shapes[state.getValue(AXIS)]!!

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun skipRendering(state: BlockState, adjacentState: BlockState, side: Direction): Boolean {
        return adjacentState.`is`(this) || super.skipRendering(state, adjacentState, side)
    }

    companion object {
        private val shapes = EnumMap<Direction.Axis, VoxelShape>(Direction.Axis::class.java).apply {
            put(
                Direction.Axis.Y,
                Shapes.or(
                    box(3.0, 0.0, 13.0, 13.0, 16.0, 16.0),
                    box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0),
                    box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0),
                    box(3.0, 0.0, 0.0, 13.0, 16.0, 3.0),
                )
            )
            put(
                Direction.Axis.X, Shapes.or(
                    box(0.0, 3.0, 13.0, 16.0, 13.0, 16.0),
                    box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0),
                    box(0.0, 13.0, 0.0, 16.0, 16.0, 16.0),
                    box(0.0, 3.0, 0.0, 16.0, 13.0, 3.0)
                )
            )
            put(
                Direction.Axis.Z, Shapes.or(
                    box(3.0, 13.0, 0.0, 13.0, 16.0, 16.0),
                    box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0),
                    box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0),
                    box(3.0, 0.0, 0.0, 13.0, 3.0, 16.0)
                )
            )
        }
    }
}
