package cn.jawbreakers.candycraftce.client.screen

import cn.jawbreakers.candycraftce.blockentity.SugarFactoryBE.Companion.COMMON_TYPE
import cn.jawbreakers.candycraftce.blockentity.SugarFactoryBE.Companion.RECIPE_TYPE_DATA_SLOT
import cn.jawbreakers.candycraftce.blockentity.SugarFactoryBE.Companion.TICKED_DATA_SLOT
import cn.jawbreakers.candycraftce.blockentity.SugarFactoryBE.Companion.TICKED_TOTAL_DATA_SLOT
import cn.jawbreakers.candycraftce.gui.menu.SugarFactoryMenu
import cn.jawbreakers.candycraftce.utils.CUtils.guiTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

/**
 * Created in 2024/2/4
 * Project: candycraftce
 *
 * author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 *
 * Kotlinize 2026/10/23
 */
open class SugarFactoryScreen(
    val menu: SugarFactoryMenu,
    inventory: Inventory,
    component: Component,
) : AbstractContainerScreen<SugarFactoryMenu>(menu, inventory, component) {

    companion object {
        val COMMON_STYLE: ResourceLocation = "gui_sugar_factory".modLoc().guiTex()
    }

    open val style: ResourceLocation = COMMON_STYLE
    open val textColor: Int = 0xb5ff71

    override fun init() {
        imageWidth = 174
        imageHeight = 114
        super.init()
        titleLabelY = -9
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderTooltip(guiGraphics, mouseX, mouseY)
    }

    override fun renderBg(graphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        graphics.blit(style, leftPos, topPos, 0, 0, 174, 114)
        val ticked = menu.data.get(TICKED_DATA_SLOT)
        val tickedTotal = menu.data.get(TICKED_TOTAL_DATA_SLOT)
        if (ticked != 0) renderProcessBar(
            graphics,
            menu.data.get(RECIPE_TYPE_DATA_SLOT),
            ticked.toFloat() / tickedTotal
        )
    }

    protected open fun renderProcessBar(graphics: GuiGraphics, recipeType: Int, progress: Float) {
        if (recipeType == COMMON_TYPE) renderProcessBar(graphics, COMMON_STYLE, 0, 114, progress)
    }

    protected fun renderProcessBar(graphics: GuiGraphics, style: ResourceLocation, u: Int, v: Int, progress: Float) {
        graphics.blit(style, leftPos + 27, topPos + 9, u, v, (120 * progress).toInt(), 12)
    }

    override fun renderLabels(graphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, textColor)
    }


}
