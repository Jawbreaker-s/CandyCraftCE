package cn.jawbreakers.candycraftce.block.blockentity

import cn.jawbreakers.candycraftce.menu.ChocolateFurnaceMenu
import cn.jawbreakers.candycraftce.registry.CBlockEntities
import cn.jawbreakers.candycraftce.utils.TickUnit.SEC2TICK
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

/**
 * Created in 2024/2/3
 * Project: candycraftce
 * 
 * Author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * 
 * 
 */
class ChocolateFurnaceBE private constructor(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState,
    tickedTotal: Int,
) : LicoriceFurnaceBE(type, pos, state, tickedTotal) {
    constructor(pos: BlockPos, state: BlockState) : this(
        CBlockEntities.chocolate_furnace_be.get(),
        pos,
        state,
        15 * SEC2TICK
    )

    override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
        return ChocolateFurnaceMenu(i, inventory, this, data)
    }
}
