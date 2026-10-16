package cn.jawbreakers.candycraftce.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class StaticWaterBlock(properties: Properties) : Block(properties) {
    @Deprecated("Deprecated in Java")
    override fun getFluidState(state: BlockState): FluidState = Fluids.WATER.getSource(false)

    @Deprecated("Deprecated in Java")
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
        Shapes.empty()

    @Deprecated("Deprecated in Java")
    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.INVISIBLE
}
