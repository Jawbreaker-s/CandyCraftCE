package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.utils.CUtils.instance
import cn.jawbreakers.candycraftce.utils.MCTimeUnit.Companion.second
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.util.RandomSource
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

class AcidMintFlowerBlock(properties: Properties) : CandyPlantBlock(properties) {
    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (random.nextInt(4) == 0) {
            val x = pos.x + random.nextFloat() / 2.0f + 0.25
            val y = pos.y + random.nextFloat() + 0.5
            val z = pos.z + random.nextFloat() / 2.0f + 0.25
            level.addParticle(ParticleTypes.ENTITY_EFFECT, x, y, z, 0.1, 0.8, 0.1)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (!level.isClientSide && entity is LivingEntity) {
            entity.addEffect(MobEffects.POISON.instance(1.second, 1))
        }
    }
}
