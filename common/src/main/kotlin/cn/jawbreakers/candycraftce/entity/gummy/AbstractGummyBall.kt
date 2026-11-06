package cn.jawbreakers.candycraftce.entity.gummy

import cn.jawbreakers.candycraftce.utils.CLevelUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CLevelUtils.ifServer
import cn.jawbreakers.candycraftce.utils.CUtils.SEC2TICK
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.ThrowableItemProjectile
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

abstract class AbstractGummyBall : ThrowableItemProjectile {

    constructor(type: EntityType<out AbstractGummyBall>, level: Level) : super(type, level)
    constructor(type: EntityType<out AbstractGummyBall>, owner: LivingEntity, level: Level)
            : super(type, owner, level)

    constructor(type: EntityType<out AbstractGummyBall>, x: Double, y: Double, z: Double, level: Level)
            : super(type, x, y, z, level)

    abstract val baseDamage: Float
    open var normalVelocity: Float = 1.5f
    open val maxAge = 5 * SEC2TICK

    override fun tick() {
        super.tick()
        level().ifClient {
            tickTrailingParticles(it)
        }
        level().ifServer {
            if (tickCount > maxAge) {
                //防止一直飞
                broken()
            }
        }
    }

    open fun tickTrailingParticles(level: ClientLevel) {}
    override fun hurt(source: DamageSource, amount: Float): Boolean = false

    override fun onHit(result: HitResult) {
        super.onHit(result)
        broken()
    }

    override fun onHitEntity(result: EntityHitResult) {
        super.onHitEntity(result)
        val owner = owner
        val entity = result.entity
        entity.hurt(damageSources().thrown(this, owner), baseDamage)
        if (owner is LivingEntity) owner.setLastHurtMob(entity)
        applyHitEntityEffect(result, entity)
    }

    fun broken() {
        level().broadcastEntityEvent(this, 3.toByte())
        discard()
    }

    fun shootBy(entity: LivingEntity, inaccuracy: Float = normalVelocity) {
        shootFromRotation(entity, entity.xRot, entity.yRot, 0.0f, inaccuracy, 1.0f)
    }

    fun shootAt(targetPos: Vec3, velocity: Float = normalVelocity) {
        val diff = targetPos.subtract(position())
        shoot(diff.x, diff.y, diff.z, velocity, 1.0f)
    }

    /**
     * 附加效果。
     */
    protected open fun applyHitEntityEffect(result: EntityHitResult, entity: Entity) {
    }

    fun spawnBrokenParticles() {
        level().addParticle(
            ItemParticleOption(ParticleTypes.ITEM, getItem()),
            x, y, z,
            (random.nextDouble() - 0.5) * 0.1,
            (random.nextDouble() - 0.5) * 0.1,
            (random.nextDouble() - 0.5) * 0.1
        )
    }

    override fun handleEntityEvent(id: Byte) {
        if (id == 3.toByte()) {
            if (level().isClientSide) {
                repeat(8) { spawnBrokenParticles() }
            }
        } else super.handleEntityEvent(id)
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.putInt("Age", tickCount)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        tickCount = compound.getInt("Age")
    }
}
