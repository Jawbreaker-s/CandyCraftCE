package cn.jawbreakers.candycraftce.entity.jelly

import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

/**
 * Created in 2026/10/6 22:07 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class LemonJelly(entityType: EntityType<out LemonJelly>, level: Level) : TinyJelly(entityType, level) {
    companion object {
        private val particle by lazy { ItemParticleOption(ParticleTypes.ITEM, CItems.lemon_jelly_ball.defaultInstance) }
    }

    override fun getJumpDelay(): Int = 4
    override fun isDealsDamage(): Boolean = this.isEffectiveAi
    override fun playerTouch(entity: Player) {
        dealDamage(entity)
    }

    override fun dealDamage(livingEntity: LivingEntity) {
        if (this.isAlive) {
            if (this.distanceToSqr(livingEntity) < 0.8 * 2 * 0.8 * 2
                && this.hasLineOfSight(livingEntity)
                && livingEntity.hurt(this.damageSources().mobAttack(this), this.attackDamage)
            ) {
                this.playSound(SoundEvents.SLIME_ATTACK, 1.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f)
                this.doEnchantDamageEffects(this, livingEntity)
            }
        }
    }

    override fun getParticleType(): ParticleOptions = particle

}