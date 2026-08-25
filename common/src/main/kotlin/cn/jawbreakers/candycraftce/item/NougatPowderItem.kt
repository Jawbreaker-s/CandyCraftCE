package cn.jawbreakers.candycraftce.item

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class NougatPowderItem(properties: Properties) : Item(properties) {
    override fun finishUsingItem(stack: ItemStack, level: Level, living: LivingEntity): ItemStack {
        val result = super.finishUsingItem(stack, level, living)
        if (!level.isClientSide && living is Player && living.getRandom().nextInt(4) == 0) {
            level.explode(null, living.x, living.y, living.z, 1.0f, Level.ExplosionInteraction.MOB)
        }
        return result
    }
}
