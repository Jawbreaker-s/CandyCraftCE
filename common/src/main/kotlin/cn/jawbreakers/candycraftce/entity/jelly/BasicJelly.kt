package cn.jawbreakers.candycraftce.entity.jelly

import cn.jawbreakers.candycraftce.utils.CUtils.always
import cn.jawbreakers.candycraftce.utils.Ticks
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.monster.Slime
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

/**
 * Created in 2026/10/6 21:58 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
abstract class BasicJelly(entityType: EntityType<out BasicJelly>, level: Level) : Slime(entityType, level) {
    companion object {
        fun createJellyAttribute(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
        }
    }

    var specialAttackCooldown: Ticks = 0
    override fun registerGoals() {
        super.registerGoals()
        targetSelector.removeAllGoals(::always)
        this.targetSelector.addGoal(1, NearestAttackableTargetGoal(this, Player::class.java, true))
    }


    final override fun playerTouch(entity: Player) {
        if (!isAlive || specialAttackCooldown > 0) return
        specialAttackCooldown = doSpecialAttack(entity)
    }

    override fun aiStep() {
        super.aiStep()
        if (specialAttackCooldown > 0) specialAttackCooldown--
    }


    abstract fun doSpecialAttack(player: Player): Int//cooldown
    abstract override fun getParticleType(): ParticleOptions

}