package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CBlockTags
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.BushBlock
import net.minecraft.world.level.block.state.BlockState

open class CandyPlantBlock(properties: Properties) : BushBlock(properties) {
    override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return state.`is`(CBlockTags.candy_soil)
    }
}