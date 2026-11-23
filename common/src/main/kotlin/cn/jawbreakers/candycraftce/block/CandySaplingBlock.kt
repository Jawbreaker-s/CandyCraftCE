package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CBlockTags
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.SaplingBlock
import net.minecraft.world.level.block.grower.AbstractTreeGrower
import net.minecraft.world.level.block.state.BlockState

class CandySaplingBlock(treeGrower: AbstractTreeGrower?, properties: Properties) : SaplingBlock(treeGrower, properties),
    ISugarTarget {
    override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return state.`is`(CBlockTags.candy_soil)
    }

    override fun isValidSugarTarget(
        level: LevelReader,
        stack: ItemStack,
        pos: BlockPos,
        state: BlockState,
        isClient: Boolean,
    ) = true

    override fun isSugarSuccess(level: Level, random: RandomSource, pos: BlockPos, state: BlockState): Boolean {
        return level.random.nextFloat() < 0.3
    }

    override fun performSugar(level: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) {
        advanceTree(level, pos, state, random)
    }

    override fun isValidBonemealTarget(level: LevelReader, pos: BlockPos, state: BlockState, isClient: Boolean) = false

    override fun isBonemealSuccess(level: Level, random: RandomSource, pos: BlockPos, state: BlockState) = false

    override fun performBonemeal(level: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) {
    }
}