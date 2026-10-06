package cn.jawbreakers.candycraftce.entity.jelly

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.Level

/**
 * Created in 2026/10/6 23:54 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
abstract class TinyJelly(entityType: EntityType<out TinyJelly>, level: Level) : BasicJelly(entityType, level) {
    companion object {
        fun createTinyAttribute(): AttributeSupplier.Builder {
            return createJellyAttribute()
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MAX_HEALTH, 1.0)
        }
    }

    override fun getSize(): Int = 1
    override fun setSize(size: Int, resetHealth: Boolean) {
        if (super.size != size) super.setSize(1, resetHealth)
//        if (resetHealth) this.health = this.maxHealth
    }

}