package cn.jawbreakers.candycraftce.menu.slot

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.Container
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

/**
 * 原版[net.minecraft.world.inventory.ResultSlot]过于繁琐，这里写一个简单化的版本
 *
 * @author Bread_NiceCat(https://gitee.com/Bread_NiceCat)
 * @date 2023/1/23 17:05
 *
 */
open class SimpleResultSlot(pContainer: Container, pSlot: Int, pX: Int, pY: Int) : Slot(pContainer, pSlot, pX, pY) {
    var fakeItem: (() -> Item)? = null
    override fun mayPlace(pStack: ItemStack): Boolean {
        return false
    }

    fun renderExtra(guiGraphics: GuiGraphics) {
        if (isActive && item.isEmpty) {
            fakeItem?.also {
                guiGraphics.setColor(1f, 1f, 1f, 0.5f)
                guiGraphics.renderFakeItem(it().defaultInstance, x, y)
                guiGraphics.setColor(1f, 1f, 1f, 1f)
            }
        }
    }
}
