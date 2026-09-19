package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CBlockTags
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.state.BlockState

/**
 * Created in 2026/9/19 19:19
 * Project: CandyCraftCE
 * Author: [Bread_NiceCat](https://github.com/Bread-NiceCat)
 */
class IceCandyPlantBlock(properties: Properties) : CandyPlantBlock(properties) {
    override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return super.mayPlaceOn(state, level, pos) || state.`is`(CBlockTags.ice_soil)
    }
}