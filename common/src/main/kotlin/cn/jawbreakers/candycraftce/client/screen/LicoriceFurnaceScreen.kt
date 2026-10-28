package cn.jawbreakers.candycraftce.client.screen

import cn.jawbreakers.candycraftce.blockentity.LicoriceFurnaceBE.Companion.LIT_TIME_DATA
import cn.jawbreakers.candycraftce.blockentity.LicoriceFurnaceBE.Companion.LIT_TIME_TOTAL_DATA
import cn.jawbreakers.candycraftce.blockentity.LicoriceFurnaceBE.Companion.TICKED_DATA
import cn.jawbreakers.candycraftce.blockentity.LicoriceFurnaceBE.Companion.TICKED_TOTAL_DATA
import cn.jawbreakers.candycraftce.menu.LicoriceFurnaceMenu
import cn.jawbreakers.candycraftce.utils.CUtils.guiTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.TickUnit.TICK2SEC
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

/**
 * Created in 2024/2/3
 * Project: candycraftce
 *
 * author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 *
 *
 */
open class LicoriceFurnaceScreen<L : LicoriceFurnaceMenu>(
    menu: L,
    inventory: Inventory,
    component: Component,
) : AbstractContainerScreen<L>(menu, inventory, component) {
    companion object {
        val LICORICE: ResourceLocation = "gui_licorice_furnace".modLoc().guiTex()
    }

    open val guiStyle: ResourceLocation = LICORICE
    open val textColor = 0xd7d7d7
    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        guiGraphics.blit(guiStyle, leftPos, topPos, 0, 0, 176, 166)
        //燃料条
        val litTime: Int = menu.containerData.get(LIT_TIME_DATA)
        val litTimeTotal: Int = menu.containerData.get(LIT_TIME_TOTAL_DATA)
        if (litTime > 0) {
            val p = if (litTime > litTimeTotal) 14 else (14f * litTime / litTimeTotal).toInt() //渲染高度
            //反向渲染
            val k = 14 - p //未渲染高度
            guiGraphics.blit(guiStyle, leftPos + 57, topPos + 36 + k, 176, k, 14, p)
        }
        //进度条
        val ticked: Int = menu.containerData.get(TICKED_DATA)
        val tickedTotal: Int = menu.containerData.get(TICKED_TOTAL_DATA)
        if (ticked > 0) {
            val w = (22f * ticked / tickedTotal).toInt()
            guiGraphics.blit(guiStyle, leftPos + 80, topPos + 35, 176, 14, w, 16)
        }
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(graphics)
        super.render(graphics, mouseX, mouseY, partialTick)
        renderTooltip(graphics, mouseX, mouseY)
    }

    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, textColor, false)
    }

    override fun renderTooltip(guiGraphics: GuiGraphics, x: Int, y: Int) {
        val litTime: Int = menu.containerData.get(LIT_TIME_DATA)
//        val ticked: Int = menu.containerData.get(TICKED_DATA)
//        val tickedTotal: Int = menu.containerData.get(TICKED_TOTAL_DATA)
        super.renderTooltip(guiGraphics, x, y)
        if (x >= leftPos + 57 && x <= leftPos + 71 && y >= topPos + 37 && y <= topPos + 51) {
            guiGraphics.renderTooltip(
                font,
                //保留一位小数
                Component.literal(((10f * litTime * TICK2SEC).toInt() / 10f).toString() + " s"),
                x,
                y
            )
        }
    }
}
