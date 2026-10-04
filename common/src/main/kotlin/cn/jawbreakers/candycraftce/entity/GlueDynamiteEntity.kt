package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.mixin.level.LevelAccessor
import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import net.minecraft.core.Direction
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.*

class GlueDynamiteEntity : DynamiteEntity {
    companion object {
        const val TAG_GLUED = "Glued"
        const val TAG_STUCK_ENTITY_UUID = "StuckEntityUUID"
        private val glue_particle = DustParticleOptions(Vector3f(0.64f, 0.32f, 0.86f), 1.0f)
        val KEY_GLUED: EntityDataAccessor<Boolean> =
            SynchedEntityData.defineId(GlueDynamiteEntity::class.java, EntityDataSerializers.BOOLEAN)
    }

    private var stuckEntity: LivingEntity? = null
    private var stuckEntityUUID: UUID? = null
    private var glued: Boolean
        get() = entityData.get(KEY_GLUED)
        set(value) = entityData.set(KEY_GLUED, value)

    constructor(entityType: EntityType<out GlueDynamiteEntity>, level: Level) : super(entityType, level)

    constructor(level: Level, owner: LivingEntity) : super(CEntityTypes.glue_dynamite.get(), level) {
        setOwner(owner)
        setPos(owner.x, owner.eyeY - 0.1, owner.z)
    }

    constructor(level: Level, x: Double, y: Double, z: Double) : super(CEntityTypes.glue_dynamite.get(), level) {
        setPos(x, y, z)
    }

    override fun getDefaultItem() = CItems.glue_dynamite.get()

    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(KEY_GLUED, false)
    }

    fun getStuckEntity(level: Level?): LivingEntity? {
        return when {
            stuckEntity != null -> stuckEntity
            level != null && stuckEntityUUID != null -> {
                ((level as LevelAccessor).callGetEntities().get(stuckEntityUUID!!) as? LivingEntity)
                    ?.also { setStuckEntity(it) }
            }

            else -> null
        }
    }

    fun setStuckEntity(entity: LivingEntity) {
        stuckEntity = entity
        stuckEntityUUID = entity.uuid
    }

    override fun getGravity() = if (this.glued) 0.0f else super.getGravity()

    override fun addTrailingParticles() {
        super.addTrailingParticles()
        repeat(2) {
            level().addParticle(
                glue_particle,
                x + (random.nextDouble() - 0.5) * bbWidth,
                y + random.nextDouble() * bbHeight,
                z + (random.nextDouble() - 0.5) * bbWidth,
                0.0, 0.0, 0.0
            )
        }
    }

    private var cachedStuckPos: Vec3? = null
    override fun tick() {
        super.tick()
        if (glued) {
            noPhysics = true
            val stuckEntity = getStuckEntity(level())
            if (stuckEntity != null && stuckEntity.isAlive) {
                //绑定到粘住的生物上
                val position = stuckEntity.position()
                if (position != cachedStuckPos) {
                    cachedStuckPos = position
                    val center = position.add(0.0, stuckEntity.bbHeight * 0.6, 0.0)
                    setPos(center.x, center.y, center.z)
                }
            }
            deltaMovement = Vec3.ZERO
        }
    }

    override fun onHitEntity(result: EntityHitResult) {
        if (level().isClientSide || chocked) {
            return
        }
        super.onHitEntity(result)
        val entity = result.entity
        if (entity is LivingEntity) {
            setStuckEntity(entity)
            stickAt(entity.position().add(0.0, entity.bbHeight * 0.6, 0.0), Direction.UP)
        }
    }

    override fun onHitBlock(result: BlockHitResult) {
        if (chocked || level().getBlockState(result.blockPos).getCollisionShape(level(), result.blockPos).isEmpty) {
            return
        }
        stickAt(surfaceCenter(result), result.direction)
    }

    private fun stickAt(center: Vec3, face: Direction) {
        chocked = true
        glued = true
        stuckFace = face
        setPos(center.x, center.y, center.z)
        xo = center.x
        yo = center.y
        zo = center.z
    }

    override fun spawnExplosionParticles() {
        val level = level()
        if (level is ServerLevel) {
            level.sendParticles(
                ItemParticleOption(ParticleTypes.ITEM, CItems.chewing_gum.defaultInstance),
                x, y, z, 70, 0.9, 0.7, 0.9, 0.18
            )
            level.sendParticles(glue_particle, x, y, z, 50, 0.9, 0.7, 0.9, 0.08)
            level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }

    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        tag.putBoolean(TAG_GLUED, this.glued)
        (stuckEntity?.uuid ?: stuckEntityUUID)?.also {
            tag.putUUID(TAG_STUCK_ENTITY_UUID, it)
        }
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        glued = tag.getBoolean(TAG_GLUED)
        if (tag.hasUUID(TAG_STUCK_ENTITY_UUID)) {
            stuckEntityUUID = tag.getUUID(TAG_STUCK_ENTITY_UUID)
        }

    }
}
