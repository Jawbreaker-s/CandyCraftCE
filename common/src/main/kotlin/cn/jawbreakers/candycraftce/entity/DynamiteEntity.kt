package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import net.minecraft.core.Direction
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.ThrowableItemProjectile
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3

open class DynamiteEntity : ThrowableItemProjectile {
    companion object {
        protected const val EXPLOSION_RADIUS: Float = 3.0f
        const val TAG_FUSE = "Fuse"
        const val TAG_CHOCKED = "Chocked"
        const val TAG_STUCK_FACE = "StuckFace"
        val KEY_CHOCKED: EntityDataAccessor<Boolean> =
            SynchedEntityData.defineId(DynamiteEntity::class.java, EntityDataSerializers.BOOLEAN)
    }

    var fuse: Int = 65
    var stuckFace: Direction = Direction.UP
    var chocked: Boolean
        get() = entityData.get(KEY_CHOCKED)
        set(value) = entityData.set(KEY_CHOCKED, value)

    constructor(entityType: EntityType<out DynamiteEntity>, level: Level) : super(entityType, level)

    constructor(level: Level, owner: LivingEntity) : super(CEntityTypes.dynamite.get(), owner, level)

    constructor(level: Level, x: Double, y: Double, z: Double) : super(CEntityTypes.dynamite.get(), x, y, z, level)

    override fun getDefaultItem(): Item = CItems.dynamite.get()
    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(KEY_CHOCKED, false)
    }

    open fun addTrailingParticles() {
        level().addParticle(
            ParticleTypes.LARGE_SMOKE,
            x + (random.nextDouble() - 0.5) * bbWidth,
            y + random.nextDouble() * bbHeight,
            z + (random.nextDouble() - 0.5) * bbWidth,
            0.0, 0.0, 0.0
        )
    }

    override fun tick() {
        super.tick()
        if (level().isClientSide) {
            addTrailingParticles()
        } else {
            if (--fuse <= 0) {
                explode()
            }
        }
    }

    override fun onHitEntity(result: EntityHitResult) {
        if (level().isClientSide || chocked) {
            return
        }
        val entity = result.entity
        entity.hurt(damageSources().thrown(this, owner), 0.5f)
        if (entity is LivingEntity) {
            deltaMovement = Vec3.ZERO
            chocked = true
        }
    }

    override fun onHitBlock(result: BlockHitResult) {
        if (!level().isClientSide) {
            val pos = result.blockPos
            val state = level().getBlockState(pos)
            if (state.getCollisionShape(level(), pos).isEmpty) {
                return
            }
            chockAt(result)
        }
    }

    protected fun chockAt(result: BlockHitResult) {
        val face = result.direction
        val center = surfaceCenter(result)
        chocked = true
        stuckFace = face
        setPos(center.x, center.y, center.z)
        xo = center.x
        yo = center.y
        zo = center.z
        when (face) {
            Direction.NORTH, Direction.SOUTH -> setDeltaMovement(deltaMovement.x * 0.1, 0.0, 0.0)
            Direction.EAST, Direction.WEST -> setDeltaMovement(0.0, 0.0, deltaMovement.z * 0.1)
            Direction.UP -> setDeltaMovement(deltaMovement.x * 0.1, 0.0, deltaMovement.z * 0.1)
            Direction.DOWN -> setDeltaMovement(deltaMovement.x * 0.2, 0.0, deltaMovement.z * 0.2)
        }
    }

    protected fun surfaceCenter(result: BlockHitResult): Vec3 {
        val hit = result.getLocation()
        val inset = bbWidth * 0.5 + 0.002
        val direction = result.direction
        return hit.add(
            direction.stepX * inset,
            direction.stepY * inset,
            direction.stepZ * inset
        )
    }

    override fun makeBoundingBox(): AABB {
        //我们以pos为中心生成碰撞箱
        val halfWidth = bbWidth / 2f
        val halfHeight = bbHeight / 2f
        return AABB(
            position().x - halfWidth,
            position().y - halfHeight,
            position().z - halfWidth,
            position().x + halfWidth,
            position().y + halfHeight,
            position().z + halfWidth
        )
    }

    protected fun explode() {
        spawnExplosionParticles()
        level().explode(this, x, y, z, EXPLOSION_RADIUS, Level.ExplosionInteraction.TNT)
        discard()
    }

    protected open fun spawnExplosionParticles() {
        val level = level()
        if (level is ServerLevel) {
            level.sendParticles(
                ItemParticleOption(ParticleTypes.ITEM, CItems.nougat_powder.defaultInstance),
                x, y, z, 60, 0.9, 0.7, 0.9, 0.18
            )
            level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }

    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        tag.putInt(TAG_FUSE, fuse)
        tag.putBoolean(TAG_CHOCKED, chocked)
        tag.putInt(TAG_STUCK_FACE, stuckFace.get3DDataValue())
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        fuse = tag.getInt(TAG_FUSE)
        chocked = tag.getBoolean(TAG_CHOCKED)
        stuckFace = Direction.from3DDataValue(tag.getInt(TAG_STUCK_FACE))
    }
}
