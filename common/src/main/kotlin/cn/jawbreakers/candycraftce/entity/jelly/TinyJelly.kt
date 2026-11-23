package cn.jawbreakers.candycraftce.entity.jelly

import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

/**
 * Created in 2026/10/6 23:54 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
abstract class TinyJelly(entityType: EntityType<out TinyJelly>, level: Level) : BasicJelly(entityType, level) {
    override fun getSize(): Int = 1
    override fun setSize(size: Int, resetHealth: Boolean) {
        if (super.size != size) super.setSize(this.size, resetHealth)
        if (resetHealth) this.health = this.maxHealth
    }

    /**[setSize]被此类重写，请子类重写[getSize]*/
    final override fun onFinalizeSpawn() {}

    override fun isRemoveDirectly(reason: RemovalReason): Boolean = true
}