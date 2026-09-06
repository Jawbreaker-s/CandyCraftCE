package cn.jawbreakers.candycraftce.block

import net.minecraft.core.Direction
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

open class SameBlockCullBlock(properties: Properties) : Block(properties) {
    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun skipRendering(state: BlockState, adjacentState: BlockState, side: Direction): Boolean {
        return adjacentState.`is`(this) || super.skipRendering(state, adjacentState, side)
    }
}
