package cn.jawbreakers.candycraftce.item

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.AbstractArrow
import net.minecraft.world.entity.projectile.Arrow
import net.minecraft.world.item.ArrowItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class HoneyArrowItem(properties: Properties) : ArrowItem(properties) {
    //TODO HoneyArrowEntity
    override fun createArrow(level: Level, stack: ItemStack, shooter: LivingEntity): AbstractArrow {
//        return HoneyArrowEntity(level, shooter)
        return Arrow(level, shooter)
    }
}
