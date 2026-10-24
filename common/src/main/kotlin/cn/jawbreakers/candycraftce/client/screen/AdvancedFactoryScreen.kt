package cn.jawbreakers.candycraftce.client.screen

import cn.jawbreakers.candycraftce.block.blockentity.AdvancedFactoryBE.Companion.ADVANCED_TYPE
import cn.jawbreakers.candycraftce.gui.menu.SugarFactoryMenu
import cn.jawbreakers.candycraftce.utils.CUtils.guiTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

/**
 * Created in 2024/2/5
 * Project: candycraftce
 * 
 * author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * 
 * 
 */
class AdvancedFactoryScreen(abstractContainerMenu: SugarFactoryMenu, inventory: Inventory, component: Component) :
    SugarFactoryScreen(abstractContainerMenu, inventory, component) {
    companion object {
        val ADVANCED_STYLE: ResourceLocation = "gui_advanced_sugar_factory".modLoc().guiTex()
    }

    override val style: ResourceLocation = ADVANCED_STYLE

    override val textColor: Int = 0xf84356

    override fun renderProcessBar(graphics: GuiGraphics, recipeType: Int, progress: Float) {
        if (recipeType == ADVANCED_TYPE) {
            renderProcessBar(graphics, ADVANCED_STYLE, 0, 114, progress)
        } else {
            super.renderProcessBar(graphics, recipeType, progress)
        }
    }
}
