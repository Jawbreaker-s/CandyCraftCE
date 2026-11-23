package cn.jawbreakers.candycraftce.entity.jelly

import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

/**
 * Created in 2026/10/6 22:01 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class MintJelly(entityType: EntityType<out MintJelly>, level: Level) : RaspberryJelly(entityType, level) {
    companion object {
        private val particle by lazy { ItemParticleOption(ParticleTypes.ITEM, CItems.mint_jelly_ball.defaultInstance) }
    }

    override fun getSize(): Int = 1
    override fun getParticleType(): ItemParticleOption = particle

    override fun aiStep() {
        super.aiStep()
        if (level().isClientSide && !onGround() && tickCount % 4 == 0) {
            level().addParticle(ParticleTypes.CLOUD, getRandomX(0.8), getRandomY(), getRandomZ(0.8), 0.0, 0.02, 0.0)
        }
    }
}