package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CBlocks
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.BushBlock
import net.minecraft.world.level.block.state.BlockState

class LollipopBlock(properties: Properties) : BushBlock(properties) {
    override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return state.`is`(CBlocks.lollipop_stem.get())
    }
}
