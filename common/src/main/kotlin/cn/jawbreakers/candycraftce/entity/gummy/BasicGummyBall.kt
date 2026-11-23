package cn.jawbreakers.candycraftce.entity.gummy

import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.utils.CUtils.SEC2TICK
import cn.jawbreakers.candycraftce.utils.CUtils.instance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult


class BasicGummyBall : AbstractGummyBall {

    override var baseDamage: Float = 0.1f

    constructor(type: EntityType<out BasicGummyBall>, level: Level) : super(type, level)
    constructor(owner: LivingEntity, level: Level) : super(CEntityTypes.gummy_ball.get(), owner, level)
    constructor(x: Double, y: Double, z: Double, level: Level) : super(CEntityTypes.gummy_ball.get(), x, y, z, level)

    override fun getDefaultItem(): Item = CItems.gummy_ball.get()
    override fun applyHitEntityEffect(result: EntityHitResult, entity: Entity) {
        if (entity is LivingEntity) {
            entity.addEffect(MobEffects.MOVEMENT_SLOWDOWN.instance(5 * SEC2TICK, 2))
        }
    }

}
