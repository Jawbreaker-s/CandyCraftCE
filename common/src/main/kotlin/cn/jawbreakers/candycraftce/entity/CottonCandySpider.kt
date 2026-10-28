package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.registry.CMobEffects
import cn.jawbreakers.candycraftce.utils.CUtils.instance
import cn.jawbreakers.candycraftce.utils.CandyTargeting
import cn.jawbreakers.candycraftce.utils.TickUnit.second
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.Difficulty
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.entity.*
import net.minecraft.world.entity.monster.Spider
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor

class CottonCandySpider(type: EntityType<out CottonCandySpider>, level: Level) : Spider(type, level) {
    override fun finalizeSpawn(
        level: ServerLevelAccessor, difficulty: DifficultyInstance, reason: MobSpawnType,
        spawnData: SpawnGroupData?, tag: CompoundTag?,
    ): SpawnGroupData? {
        //不要蜘蛛骑士和随机效果。
        return spawnData
    }

    override fun doHurtTarget(target: Entity): Boolean {
        if (!CandyTargeting.canAttackEntity(target, this)) {
            setTarget(null)
            return false
        }
        if (super.doHurtTarget(target)) {
            if (target is LivingEntity) {
                val seconds = if (level().difficulty == Difficulty.HARD) 15 else 7
                target.addEffect(CMobEffects.cloying.get().instance(seconds.second), this)
            }
            return true
        }
        return false
    }
}
