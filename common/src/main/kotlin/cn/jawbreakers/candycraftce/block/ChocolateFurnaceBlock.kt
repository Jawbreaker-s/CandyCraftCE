package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CBlockEntities
import cn.jawbreakers.candycraftce.registry.CBlockEntities.create
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

/**
 * Created in 2026/10/24 21:57 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class ChocolateFurnaceBlock(val white: Boolean, properties: Properties) : LicoriceFurnaceBlock(properties) {
    override fun newBlockEntity(pos: BlockPos, state: BlockState) =
        CBlockEntities.chocolate_furnace_be.create(pos, state)
}