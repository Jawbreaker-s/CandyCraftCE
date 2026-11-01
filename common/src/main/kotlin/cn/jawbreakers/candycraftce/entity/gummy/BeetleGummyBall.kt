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


class BeetleGummyBall : AbstractGummyBall {
    constructor(type: EntityType<out BeetleGummyBall>, level: Level) : super(type, level)
    constructor(owner: LivingEntity, level: Level) : super(gummy_ball_gum.get(), owner, level)
    constructor(x: Double, y: Double, z: Double, level: Level) : super(gummy_ball_gum.get(), x, y, z, level)

    override val baseDamage: Float = 4.0f
    override val normalVelocity: Float = 0.8f
    override val maxAge: Int = 10 * SEC2TICK
    override fun getGravity(): Float = 5.0E-4F

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        val entity = source.entity ?: return false
        val look = entity.lookAngle
        owner = entity
        setDeltaMovement(look.x, look.y, look.z)
        hasImpulse = true
        markHurt()
        return true
    }

    override fun applyHitEntityEffect(result: EntityHitResult, entity: Entity) {
        val motion = deltaMovement
        val horizontal = motion.horizontalDistance()
        if (horizontal > 1.0E-4) {
            entity.push(motion.x * 0.6 / horizontal, 0.1, motion.z * 0.6 / horizontal)
        }
    }

    override fun getDefaultItem() = CItems.gummy_ball_gum.get()
}
