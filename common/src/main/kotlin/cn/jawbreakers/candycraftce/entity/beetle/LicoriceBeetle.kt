package cn.jawbreakers.candycraftce.entity.beetle

import cn.jawbreakers.candycraftce.entity.CandyBossType
import cn.jawbreakers.candycraftce.entity.ICandyBoss
import cn.jawbreakers.candycraftce.entity.ICandyBoss.Companion.HALF_FAINT_TICKS
import cn.jawbreakers.candycraftce.utils.CUtils.defineId
import cn.jawbreakers.candycraftce.utils.CUtils.synched
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.animal.AbstractGolem
import net.minecraft.world.level.Level

/**
 * Created in 2026/11/2 00:48 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class LicoriceBeetle(entityType: EntityType<out LicoriceBeetle>, level: Level) : AbstractGolem(entityType, level),
    ICandyBoss {
    companion object {
        val AWAKE = defineId(EntityDataSerializers.BOOLEAN)
        val FAINT = defineId(EntityDataSerializers.INT)
    }

    override fun self(): Mob = this
    override var awake: Boolean by synched(AWAKE)
    override var faint: Int by synched(FAINT)
    override var lostTargetTicks: Int = 0
    override val bossType: CandyBossType = CandyBossType.MiniBoss
    override fun registerGoals() {
        super.registerGoals()
    }

    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(AWAKE, false)
        entityData.define(FAINT, HALF_FAINT_TICKS)
    }


}