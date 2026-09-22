@file:Suppress("DEPRECATION")

package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.misc.EmblemHelper
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class ChewingGumPuddleBlock(properties: Properties) : Block(properties) {
    companion object {
        private val SHAPE: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 0.25, 16.0)
    }

    @Deprecated("Deprecated in Java")
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return SHAPE
    }

    @Deprecated("Deprecated in Java")
    override fun canSurvive(state: BlockState, level: LevelReader, pos: BlockPos): Boolean {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
    }

    @Deprecated("Deprecated in Java")
    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        level: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos,
    ): BlockState {
        return if (!state.canSurvive(level, pos)) {
            if (!level.isClientSide && level is Level) popResource(level, pos, CItems.chewing_gum.defaultInstance)
            Blocks.AIR.defaultBlockState()
        } else {
            super.updateShape(state, direction, neighborState, level, pos, neighborPos)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        //TODO
        if (ChewingGumBlock.isChewingGumNative(entity)) {
            return
        }
        if (entity is Player && EmblemHelper.isChewingGumImmune(entity)) {
            return
        }
        val movement = entity.deltaMovement
        entity.setDeltaMovement(movement.x * 0.2, 0.0, movement.z * 0.2)
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (random.nextInt(10) == 0) {
            val x = pos.x + 0.15 + random.nextDouble() * 0.7
            val z = pos.z + 0.15 + random.nextDouble() * 0.7
            level.addParticle(BlockParticleOption(ParticleTypes.BLOCK, state), x, pos.y + 0.04, z, 0.0, 0.015, 0.0)
        }
    }
}
