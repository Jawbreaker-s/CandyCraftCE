package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CMobEffects
import cn.jawbreakers.candycraftce.utils.TickUnit.second
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class SpikesBlock(properties: Properties, private val damage: Int, private val withCloying: Boolean = false) :
    Block(properties) {
    companion object {
        private val shape: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 9.6, 16.0)
    }

    @Deprecated("Deprecated in Java")
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext) = shape

    @Deprecated("Deprecated in Java")
    override fun canSurvive(state: BlockState, level: LevelReader, pos: BlockPos): Boolean {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
    }

    @Deprecated("Deprecated in Java")
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        hurtEntity(level, entity)
    }

    override fun fallOn(level: Level, state: BlockState, pos: BlockPos, entity: Entity, fallDistance: Float) {
        hurtEntity(level, entity)
        super.fallOn(level, state, pos, entity, fallDistance * 2f)
    }

    private fun hurtEntity(level: Level, entity: Entity?) {
        if (!level.isClientSide && entity is LivingEntity) {
            entity.hurt(level.damageSources().generic(), damage / 2.0f)
            if (withCloying) {
                entity.addEffect(MobEffectInstance(CMobEffects.cloying.get(), 5.second))
            }
        }
    }
}
