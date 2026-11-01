package cn.jawbreakers.candycraftce.entity.gummy

import cn.jawbreakers.candycraftce.registry.CEntityTypes.gummy_ball_hot_gum
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.utils.CUtils.SEC2TICK
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult

/**
 * power 2：4.0F + 点燃 7 秒；飞行途中生成火焰粒子；渲染 CustomModelData 1。
 */
class FireGummyBall : AbstractGummyBall {

    constructor(type: EntityType<out FireGummyBall>, level: Level) : super(type, level)
    constructor(owner: LivingEntity, level: Level) : super(gummy_ball_hot_gum.get(), owner, level)
    constructor(x: Double, y: Double, z: Double, level: Level) : super(gummy_ball_hot_gum.get(), x, y, z, level)

    override val baseDamage: Float = 2.0f

    override fun getGravity(): Float = 5.0E-4F

    override fun applyHitEntityEffect(result: EntityHitResult, entity: Entity) {
        entity.remainingFireTicks = 6 * SEC2TICK
    }

    override fun tickTrailingParticles(level: ClientLevel) {
        repeat(2) { i ->
            val step = i * 0.45 + random.nextDouble() * 0.2
            level().addParticle(
                ParticleTypes.FLAME,
                x - deltaMovement.x * step + (random.nextDouble() - 0.5) * 0.12,
                y - deltaMovement.y * step + (random.nextDouble() - 0.5) * 0.12,
                z - deltaMovement.z * step + (random.nextDouble() - 0.5) * 0.12,
                0.0, 0.0, 0.0
            )
        }
    }

    override fun getDefaultItem() = CItems.gummy_ball_hot_gum.get()
}
