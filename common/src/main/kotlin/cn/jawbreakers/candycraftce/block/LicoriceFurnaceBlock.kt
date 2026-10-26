package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.block.blockentity.LicoriceFurnaceBE
import cn.jawbreakers.candycraftce.registry.CBlockEntities
import cn.jawbreakers.candycraftce.registry.CBlockEntities.create
import cn.jawbreakers.candycraftce.utils.CLevelUtils.ifServer
import net.minecraft.core.BlockPos
import net.minecraft.world.Containers
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.AbstractFurnaceBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

/**
 * Created in 2024/1/30 23:03
 * Project: candycraftce
 * 
 * Author [Bread_NiceCat](https://github.com/Bread-Nicecat)
 * 
 * 
 */
open class LicoriceFurnaceBlock(properties: Properties) : AbstractFurnaceBlock(properties) {

    override fun openContainer(level: Level, pos: BlockPos, player: Player) {
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity is LicoriceFurnaceBE) {
            player.openMenu(blockEntity)
        }
    }

    override fun onRemove(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        newState: BlockState,
        movedByPiston: Boolean,
    ) {
        if (state.`is`(newState.block)) {
            return
        }
        val blockEntity = level.getBlockEntity(pos)
        if (!level.isClientSide() && blockEntity is LicoriceFurnaceBE) {
            Containers.dropContents(level, pos, blockEntity)
            blockEntity.dropExp(Vec3.atCenterOf(pos))
            level.updateNeighbourForOutputSignal(pos, this)
        }
        super.onRemove(state, level, pos, newState, movedByPiston)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState) =
        CBlockEntities.licorice_furnace_be.create(pos, state)

    override fun <T : BlockEntity?> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T?>,
    ): BlockEntityTicker<T>? {
        return level.ifServer {
            BlockEntityTicker { lvl, pos, stat1, be ->
                if (be is LicoriceFurnaceBE) {
                    be.serverTick()
                }
            }
        }
    }
}

