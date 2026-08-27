package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CBlocks.pudding_block
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.tags.FluidTags
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.FarmBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty

class CandyFarmlandBlock(properties: Properties) : FarmBlock(properties) {
    companion object {
        val MOISTURE: IntegerProperty = BlockStateProperties.MOISTURE
    }

    override fun fallOn(level: Level, state: BlockState, pos: BlockPos, entity: Entity, distance: Float) {
        if (!level.isClientSide && level.random.nextFloat() < distance - 0.5f && entity is LivingEntity
            && (entity is Player || level.gameRules.getBoolean(GameRules.RULE_MOBGRIEFING))
            && entity.bbWidth * entity.bbWidth * entity.bbHeight > 0.512f
        ) {
            turnToFlour(level, pos)
        }
        entity.causeFallDamage(distance, 1.0f, level.damageSources().fall())
    }

    override fun tick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (!state.canSurvive(level, pos)) {
            turnToFlour(level, pos)
        }
    }

    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        val moisture = state.getValue(MOISTURE)
        if (!isNearWater(level, pos) && !level.isRainingAt(pos.above())) {
            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2)
            } else if (!shouldMaintainFarmland(level, pos)) {
                turnToFlour(level, pos)
            }
        } else if (moisture < 7) {
            level.setBlock(pos, state.setValue(MOISTURE, 7), 2)
        }
    }

    private fun shouldMaintainFarmland(level: BlockGetter, pos: BlockPos): Boolean {
        return level.getBlockState(pos.above()).`is`(BlockTags.MAINTAINS_FARMLAND)
    }

    private fun isNearWater(level: LevelReader, pos: BlockPos): Boolean {
        return BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))
            .any { level.getFluidState(it).`is`(FluidTags.WATER) }
    }

    private fun turnToFlour(level: Level, pos: BlockPos) {
        level.setBlockAndUpdate(pos, pudding_block.get().defaultBlockState())
    }

}
