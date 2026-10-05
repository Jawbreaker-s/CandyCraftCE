package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
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
    }

    var fuse: Int = 65

    constructor(entityType: EntityType<out DynamiteEntity>, level: Level) : super(entityType, level)

    constructor(level: Level, owner: LivingEntity) : super(CEntityTypes.dynamite.get(), owner, level)

    constructor(level: Level, x: Double, y: Double, z: Double) : super(CEntityTypes.dynamite.get(), x, y, z, level)

    override fun getDefaultItem(): Item = CItems.dynamite.get()

    override fun tick() {
        super.tick()
        if (level().isClientSide) {
            spawnTrailingParticles()
        } else {
            if (--fuse <= 0) {
                explode()
            }
        }
    }

    override fun onHitEntity(result: EntityHitResult) {
        val entity = result.entity
        entity.hurt(damageSources().thrown(this, owner), 0.5f)
        if (entity is LivingEntity) {
            chockAt(entityCenter(entity), Direction.UP, entity = entity)
            deltaMovement = Vec3.ZERO
        }
    }

    override fun onHitBlock(result: BlockHitResult) {
        val pos = result.blockPos
        if (level().getBlockState(pos).getCollisionShape(level(), pos).isEmpty) {
            return
        }
        chockAt(surfaceCenter(result.location, result.direction), result.direction, blockPos = pos)
    }

    protected open fun chockAt(
        center: Vec3,
        face: Direction?,
        blockPos: BlockPos? = null,
        entity: LivingEntity? = null,
    ) {
        xo = center.x
        yo = center.y
        zo = center.z
        when (face) {
            Direction.NORTH, Direction.SOUTH -> setDeltaMovement(deltaMovement.x * 0.1, 0.0, 0.0)
            Direction.EAST, Direction.WEST -> setDeltaMovement(0.0, 0.0, deltaMovement.z * 0.1)
            Direction.UP -> setDeltaMovement(deltaMovement.x * 0.1, 0.0, deltaMovement.z * 0.1)
            Direction.DOWN -> setDeltaMovement(deltaMovement.x * 0.2, 0.0, deltaMovement.z * 0.2)
            null -> {}
        }
    }

    protected fun entityCenter(entity: Entity): Vec3 {
        return entity.position().add(0.0, entity.bbHeight * 0.6, 0.0)
    }

    protected fun surfaceCenter(location: Vec3, face: Direction): Vec3 {
        val inset = bbWidth * 0.5 + 0.002
        return location.add(
            direction.stepX * inset,
            direction.stepY * inset,
            direction.stepZ * inset
        )
    }

    override fun makeBoundingBox(): AABB {
//        //以pos为中心生成碰撞箱
//        val halfWidth = bbWidth / 2f
//        val halfHeight = bbHeight / 2f
//        return AABB(
//            position().x - halfWidth,
//            position().y - halfHeight,
//            position().z - halfWidth,
//            position().x + halfWidth,
//            position().y + halfHeight,
//            position().z + halfWidth
//        )
        return super.makeBoundingBox()
    }

    protected fun explode() {
        spawnExplosionParticles()
        level().explode(this, x, y, z, EXPLOSION_RADIUS, Level.ExplosionInteraction.TNT)
        discard()
    }

    open fun spawnTrailingParticles() {
        level().addParticle(
            ParticleTypes.LARGE_SMOKE,
            x + (random.nextDouble() - 0.5) * bbWidth,
            y + random.nextDouble() * bbHeight,
            z + (random.nextDouble() - 0.5) * bbWidth,
            0.0, 0.0, 0.0
        )
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
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        fuse = tag.getInt(TAG_FUSE)
    }
}
