package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CFluidTags
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty

class FragileGrenadineIce(properties: Properties) : Block(properties) {
    companion object {
        val AGE: IntegerProperty = BlockStateProperties.AGE_3
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(AGE, 0))
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        tick(state, level, pos, random)
    }

    @Deprecated("Deprecated in Java")
    override fun tick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (hasGrenadineNearby(level, pos)) {
            if (state.getValue(AGE) != 0) {
                level.setBlock(pos, state.setValue(AGE, 0), UPDATE_CLIENTS)
            }
            level.destroyBlockProgress(crackId(pos), pos, -1)
            level.scheduleTick(pos, this, 40 + random.nextInt(40))
            return
        }

        val age = state.getValue(AGE)
        if (age >= 3) {
            level.destroyBlockProgress(crackId(pos), pos, -1)
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), UPDATE_ALL)
        } else {
            level.setBlock(pos, state.setValue(AGE, age + 1), UPDATE_CLIENTS)
            level.destroyBlockProgress(crackId(pos), pos, (age + 1) * 3)
            level.scheduleTick(pos, this, 40 + random.nextInt(40))
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, moving: Boolean) {
        super.onPlace(state, level, pos, oldState, moving)
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 40 + level.random.nextInt(40))
        }
    }

    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player) {
        super.playerWillDestroy(level, pos, state, player)
        if (!level.isClientSide) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), UPDATE_ALL)
        }
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        builder.add(AGE)
    }

    private fun hasGrenadineNearby(level: Level, pos: BlockPos): Boolean {
        for (direction in Direction.entries) {
            if (level.getFluidState(pos.relative(direction)).`is`(CFluidTags.grenadine)) {
                return true
            }
        }
        return false
    }

    private fun crackId(pos: BlockPos): Int = pos.hashCode()
}
