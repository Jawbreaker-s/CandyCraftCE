package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import cn.jawbreakers.candycraftce.utils.CLevelUtils.component1
import cn.jawbreakers.candycraftce.utils.CLevelUtils.component2
import cn.jawbreakers.candycraftce.utils.CLevelUtils.component3
import cn.jawbreakers.candycraftce.utils.CUtils.getBlockPos
import cn.jawbreakers.candycraftce.utils.CUtils.getVec3
import cn.jawbreakers.candycraftce.utils.CUtils.putBlockPos
import cn.jawbreakers.candycraftce.utils.CUtils.putVec3
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.*

class GlueDynamiteEntity : DynamiteEntity {
    companion object {
        const val TAG_STUCK_ENTITY_UUID = "StuckEntityUUID"
        const val TAG_STUCK_BLOCK = "StuckBlock"
        const val TAG_STUCK_BLOCK_AT = "StuckBlockAt"
        private val glue_particle = DustParticleOptions(Vector3f(0.64f, 0.32f, 0.86f), 1.0f)
    }

    constructor(entityType: EntityType<out GlueDynamiteEntity>, level: Level) : super(entityType, level)

    constructor(level: Level, owner: LivingEntity) : super(CEntityTypes.glue_dynamite.get(), level) {
        setOwner(owner)
        setPos(owner.x, owner.eyeY - 0.1, owner.z)
    }

    constructor(level: Level, x: Double, y: Double, z: Double) : super(CEntityTypes.glue_dynamite.get(), level) {
        setPos(x, y, z)
    }

    private var stuckEntityUUID: UUID? = null

    //粘住的是Block
    private var stuckBlock: BlockPos? = null
    private var stuckBlockAt: Vec3? = null //粘住的位置
    fun isStuck() = stuckEntityUUID != null || stuckBlock != null

    override fun getDefaultItem() = CItems.glue_dynamite.get()

    override fun isNoGravity(): Boolean {
        return isStuck() || super.isNoGravity()
    }

    private var lastStuckEntityPos: Vec3? = null
    private var cachedStuckEntity: LivingEntity? = null
    override fun tick() {
        super.tick()
        val level = level()
        //实体判断部分
        if (level is ServerLevel && cachedStuckEntity == null && stuckEntityUUID != null) {
            cachedStuckEntity = level.getEntity(stuckEntityUUID!!) as? LivingEntity
        }
        cachedStuckEntity?.also {
            if (it.isAlive) {
                val position = it.position()
                if (position != lastStuckEntityPos) {
                    lastStuckEntityPos = position
                    val center = entityCenter(it)
                    setPos(center.x, center.y, center.z)
                }
            } else {
                stuckEntityUUID = null
                cachedStuckEntity = null
            }
        }
        //方块判断部分
        stuckBlock?.also {
            if (level().getBlockState(it).getCollisionShape(level(), it).isEmpty) {
                //方块不符合要求了，可能被挖了等情况
                stuckBlock = null
                stuckBlockAt = null
            } else {
                val (x, y, z) = stuckBlockAt ?: it.center
                setPos(x, y, z)
                deltaMovement = Vec3.ZERO
            }
        }
    }

    override fun onHit(result: HitResult) {
        //如果粘住实体或者方块了则不再进行碰撞判断
        if (isStuck()) return
        super.onHit(result)
    }

    override fun chockAt(center: Vec3, face: Direction?, blockPos: BlockPos?, entity: LivingEntity?) {
        super.chockAt(center, null, blockPos, entity)
        if (entity != null) {
            cachedStuckEntity = entity
            stuckEntityUUID = entity.uuid
        } else if (blockPos != null) {
            stuckBlock = blockPos
            stuckBlockAt = center
        }
        deltaMovement = Vec3.ZERO
    }


    override fun spawnTrailingParticles() {
        super.spawnTrailingParticles()
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
        stuckEntityUUID?.also {
            tag.putUUID(TAG_STUCK_ENTITY_UUID, it)
        }
        stuckBlock?.also {
            tag.putBlockPos(TAG_STUCK_BLOCK, it)
        }
        stuckBlockAt?.also {
            tag.putVec3(TAG_STUCK_BLOCK_AT, it)
        }
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        if (tag.hasUUID(TAG_STUCK_ENTITY_UUID)) {
            stuckEntityUUID = tag.getUUID(TAG_STUCK_ENTITY_UUID)
        }
        tag.getBlockPos(TAG_STUCK_BLOCK)?.also { stuckBlock = it }
        tag.getVec3(TAG_STUCK_BLOCK_AT)?.also { stuckBlockAt = it }

    }
}
