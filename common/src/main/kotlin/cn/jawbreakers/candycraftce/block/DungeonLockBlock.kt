package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.utils.CLevelUtils
import cn.jawbreakers.candycraftce.utils.CLevelUtils.ifServer
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.phys.BlockHitResult
import java.util.function.Supplier

/**
 * Created in 2026/10/17 12:57 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class DungeonLockBlock(val key: Supplier<out Item>, properties: Properties) : Block(properties) {
    companion object {
        val FADE: BooleanProperty = BooleanProperty.create("fade")
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(FADE, false))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(FADE)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun use(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult,
    ): InteractionResult {
        if (!state.getValue(FADE) && player.getItemInHand(hand).`is`(key.get())) {
            level.ifServer { level ->
                unlock(state, level, pos)
            }
            return InteractionResult.sidedSuccess(level.isClientSide)
        }
        return super.use(state, level, pos, player, hand, hit)
    }

    fun unlock(state: BlockState, level: ServerLevel, pos: BlockPos) {
        level.setBlock(pos, state.setValue(FADE, true), 1 or 2 or 8)
        level.scheduleTick(pos, this, 7)
    }

    @Deprecated("Deprecated in Java")
    override fun tick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (state.getValue(FADE)) {
            level.destroyBlock(pos, false)
            CLevelUtils.getNeighbourPos(pos).forEach { pos1 ->
                val state1 = level.getBlockState(pos1)
                if (state1.`is`(this)) {
                    unlock(state1, level, pos1)
                }
            }
        }
    }

}

