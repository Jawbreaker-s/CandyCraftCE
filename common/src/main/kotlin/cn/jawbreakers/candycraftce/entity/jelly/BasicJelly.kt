package cn.jawbreakers.candycraftce.entity.jelly

import cn.jawbreakers.candycraftce.utils.CUtils.always
import cn.jawbreakers.candycraftce.utils.CandyTargeting
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.SpawnGroupData
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.monster.Slime
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor

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

    override fun registerGoals() {
        super.registerGoals()
        targetSelector.removeAllGoals(::always)
        this.targetSelector.addGoal(1, NearestAttackableTargetGoal(this, Player::class.java, true, ::mayAttack))
    }

    fun mayAttack(entity: Entity) = CandyTargeting.canAttackEntity(entity, this)
    final override fun remove(reason: RemovalReason) {
        if (isRemoveDirectly(reason)) {
            removeDirectly(reason)
        } else {
            super.remove(reason)
        }
    }

    /**
     * 是否直接移除而不裂变
     * */
    open fun isRemoveDirectly(reason: RemovalReason): Boolean = true

    open fun removeDirectly(reason: RemovalReason) {
        brain.clearMemories();
        setRemoved(reason)
    }

    abstract override fun getParticleType(): ParticleOptions

    override fun finalizeSpawn(
        level: ServerLevelAccessor,
        difficulty: DifficultyInstance,
        reason: MobSpawnType,
        spawnData: SpawnGroupData?,
        dataTag: CompoundTag?,
    ): SpawnGroupData? {
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag).also { onFinalizeSpawn() }
    }

    /**
     * 生成后的一些设置如设置大小等
     * */
    open fun onFinalizeSpawn() {}

}