package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.mixin_stub.ICandyStuckProjectileCarrier
import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import cn.jawbreakers.candycraftce.registry.CMobEffects
import cn.jawbreakers.candycraftce.utils.CUtils.instance
import cn.jawbreakers.candycraftce.utils.TickUnit.second
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.AbstractArrow
import net.minecraft.world.level.Level
import kotlin.math.max

class HoneyArrow : AbstractArrow {
    companion object {
        private const val CROSSBOW_DAMAGE_MULTIPLIER = 1.75
        private val BOSS_SUGUARD_GROUND_DESPAWN_TICKS = 10.second
        private val HONEY_GLUE_DURATION_TICKS = 5.second

        const val TAG_HONEY_GLUE = "HoneyGlue"
        const val TAG_BOSS_SUGUARD_PROJECTILE = "BossSuguardProjectile"
        const val TAG_BOSS_SUGUARD_GROUND_TICKS = "BossSuguardGroundTicks"
    }

    private var honeyGlue = false
    private var bossSuguardProjectile = false
    private var bossSuguardGroundTicks = 0

    constructor(entityType: EntityType<out HoneyArrow>, level: Level) : super(entityType, level)

    constructor(level: Level, owner: LivingEntity) : super(CEntityTypes.honey_arrow.get(), owner, level)

    constructor(level: Level, x: Double, y: Double, z: Double) : super(CEntityTypes.honey_arrow.get(), x, y, z, level)

    override fun getPickupItem() = CItems.honey_arrow.defaultInstance

    fun setHoneyGlue(honeyGlue: Boolean) {
        this.honeyGlue = honeyGlue
    }

    fun markBossSuguardProjectile() {
        bossSuguardProjectile = true
    }

    override fun tick() {
        super.tick()
        if (!level().isClientSide) {
            if (bossSuguardProjectile) {
                if (inGround) {
                    if (++bossSuguardGroundTicks >= BOSS_SUGUARD_GROUND_DESPAWN_TICKS) {
                        discard()
                        return
                    }
                } else {
                    bossSuguardGroundTicks = 0
                }
            }
        }
        if (!inGround && !isInWater) {
            deltaMovement = deltaMovement.scale(0.6)
        }
    }

    override fun getWaterInertia() = 0.6f

    override fun doPostHurtEffects(living: LivingEntity) {
        super.doPostHurtEffects(living)
        if (!level().isClientSide && honeyGlue) {
            living.addEffect(
                CMobEffects.propolis.get().instance(HONEY_GLUE_DURATION_TICKS),
                effectSource
            )
        }

        if (!level().isClientSide && pierceLevel <= 0 && living is ICandyStuckProjectileCarrier) {
            living.arrowCount = max(0, living.arrowCount - 1)
            if (shotFromCrossbow()) {
                living.candycraftce_honeyBoltCount++
            } else {
                living.arrowCount++
            }
        }
    }

    override fun setShotFromCrossbow(shotFromCrossbow: Boolean) {
        if (shotFromCrossbow() != shotFromCrossbow) {
            if (shotFromCrossbow) {
                baseDamage *= CROSSBOW_DAMAGE_MULTIPLIER
            } else {
                baseDamage /= CROSSBOW_DAMAGE_MULTIPLIER
            }
            super.setShotFromCrossbow(shotFromCrossbow)
        }
    }

    override fun tryPickup(player: Player): Boolean {
        return !bossSuguardProjectile && super.tryPickup(player)
    }

    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        tag.putBoolean(TAG_HONEY_GLUE, honeyGlue)
        tag.putBoolean(TAG_BOSS_SUGUARD_PROJECTILE, bossSuguardProjectile)
        tag.putInt(TAG_BOSS_SUGUARD_GROUND_TICKS, bossSuguardGroundTicks)
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        honeyGlue = tag.getBoolean(TAG_HONEY_GLUE)
        bossSuguardProjectile = tag.getBoolean(TAG_BOSS_SUGUARD_PROJECTILE)
        bossSuguardGroundTicks = tag.getInt(TAG_BOSS_SUGUARD_GROUND_TICKS)
    }
}
