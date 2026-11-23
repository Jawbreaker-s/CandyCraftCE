package cn.jawbreakers.candycraftce.item

import cn.jawbreakers.candycraftce.entity.HoneyArrow
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.AbstractArrow
import net.minecraft.world.item.ArrowItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class HoneyArrowItem(properties: Properties) : ArrowItem(properties) {
    override fun createArrow(level: Level, stack: ItemStack, shooter: LivingEntity): AbstractArrow {
        return HoneyArrow(level, shooter)
    }
}
