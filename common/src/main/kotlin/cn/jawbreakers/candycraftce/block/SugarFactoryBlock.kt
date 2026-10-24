package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.block.blockentity.SugarFactoryBE
import cn.jawbreakers.candycraftce.registry.CBlockEntities
import cn.jawbreakers.candycraftce.utils.CLevelUtils.ifServer
import net.minecraft.core.BlockPos
import net.minecraft.world.Containers
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

/**
 * Created in 2024/2/3
 * Project: candycraftce
 * 
 * author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 *
 * Kotlinize in 2026/10/23
 * 
 * 
 */
open class SugarFactoryBlock(properties: Properties) : Block(properties), EntityBlock {
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return CBlockEntities.sugar_factory_be.get().create(pos, state)
    }

    @Deprecated("Deprecated in Java")
    override fun use(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult,
    ): InteractionResult {
        level.ifServer {
            val be = level.getBlockEntity(pos)
            if (be is SugarFactoryBE) {
                player.openMenu(be)
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide())
    }

    override fun <T : BlockEntity?> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T?>,
    ): BlockEntityTicker<T>? {
        return level.ifServer {
            BlockEntityTicker { _, _, _, be ->
                if (be is SugarFactoryBE) {
                    be.serverTick()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
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
        level.ifServer {
            val be = level.getBlockEntity(pos)
            if (be is SugarFactoryBE) {
                Containers.dropContents(level, pos, be)
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston)
    }

    @Deprecated("Deprecated in Java")
    override fun hasAnalogOutputSignal(state: BlockState): Boolean = true

    @Deprecated("Deprecated in Java")
    override fun getAnalogOutputSignal(state: BlockState, level: Level, pos: BlockPos): Int {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos))
    }
}
