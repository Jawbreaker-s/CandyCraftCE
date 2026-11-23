package cn.jawbreakers.candycraftce.entity.gummy

import cn.jawbreakers.candycraftce.registry.CEntityTypes.gummy_ball_gum
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.utils.CUtils.SEC2TICK
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult


class BeetleGummyBall : AbstractGummyBall {
    constructor(type: EntityType<out BeetleGummyBall>, level: Level) : super(type, level)
    constructor(owner: LivingEntity, level: Level) : super(gummy_ball_gum.get(), owner, level)
    constructor(x: Double, y: Double, z: Double, level: Level) : super(gummy_ball_gum.get(), x, y, z, level)

    override val baseDamage: Float = 4.0f
    override var normalVelocity: Float = 0.8f
    override val maxAge: Int = 10 * SEC2TICK
    override fun hurt(source: DamageSource, amount: Float): Boolean {
        val entity = source.entity ?: return false
        val look = entity.lookAngle
        setDeltaMovement(look.x, look.y, look.z)
        owner = entity
        hasImpulse = true
        markHurt()
        return true
    }

    override fun isPickable(): Boolean = true
    override fun getPickRadius(): Float = 1.0f

    override fun onHit(result: HitResult) {
        if (result is EntityHitResult) {
            if (result.entity != owner && result.entity !is BeetleGummyBall) {
                super.onHit(result)
            }
        } else {
            super.onHit(result)
        }
    }

    override fun applyHitEntityEffect(result: EntityHitResult, entity: Entity) {
        val motion = deltaMovement
        entity.push(motion.x * 1.25, motion.y * 1.25, motion.z * 1.25)
    }

    override fun getDefaultItem() = CItems.gummy_ball_gum.get()
}
