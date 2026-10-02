package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.animal.AbstractSchoolingFish
import net.minecraft.world.entity.animal.Cod
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class Cranfish(type: EntityType<out Cranfish>, level: Level) : Cod(type, level) {
    override fun getBucketItemStack(): ItemStack = ItemStack.EMPTY

    override fun getSoundVolume(): Float = 0.4f

    override fun dropCustomDeathLoot(source: DamageSource, looting: Int, recentlyHit: Boolean) {
        if (random.nextInt(50) == 0) {
            spawnAtLocation(CItems.cranberry_emblem.defaultInstance)
        }
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return AbstractSchoolingFish.createAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 2.0)
        }

    }
}
