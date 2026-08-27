package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.utils.CLevelUtils
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.FluidTags
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.FallingBlock
import net.minecraft.world.level.block.state.BlockState

class SugarSandBlock(properties: Properties) : FallingBlock(properties) {
    override fun tick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        //遇水变糖块
        if (CLevelUtils.getNeighbourPos(pos).anyMatch { level.getFluidState(it).`is`(FluidTags.WATER) }) {
            level.setBlockAndUpdate(pos, CBlocks.sugar_block.get().defaultBlockState())
        } else super.tick(state, level, pos, random)
    }

}