package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.utils.UsedByMixin
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.BlockState

interface ISugarTarget {
    companion object {
        /**
         * @see [cn.jawbreakers.candycraftce.mixin.item.MixinItem.useOn]
         * */
        @JvmStatic
        @UsedByMixin
        fun grow(item: ItemStack, stack: ItemStack, level: Level, pos: BlockPos): Boolean {
            val state = level.getBlockState(pos)
            val block = state.block
            if (block is ISugarTarget && block.isValidSugarTarget(level, stack, pos, state, level.isClientSide)
            ) {
                if (level !is ServerLevel) return true
                item.shrink(1)
                if (block.isSugarSuccess(level, level.random, pos, state)) {
                    block.performSugar(level, level.random, pos, state)
                    return true
                }
            }
            return false
        }
    }

    fun isValidSugarTarget(
        level: LevelReader,
        stack: ItemStack,
        pos: BlockPos,
        state: BlockState,
        isClient: Boolean,
    ): Boolean

    fun isSugarSuccess(level: Level, random: RandomSource, pos: BlockPos, state: BlockState): Boolean

    fun performSugar(level: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState)
}