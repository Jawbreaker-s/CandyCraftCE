package cn.jawbreakers.candycraftce.client.screen

import cn.jawbreakers.candycraftce.menu.ChocolateFurnaceMenu
import cn.jawbreakers.candycraftce.utils.CUtils.guiTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
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
class ChocolateFurnaceScreen(menu: ChocolateFurnaceMenu, inventory: Inventory, component: Component) :
    LicoriceFurnaceScreen<ChocolateFurnaceMenu>(menu, inventory, component) {
    companion object {
        val CHOCOLATE: ResourceLocation = "gui_chocolate_furnace".modLoc().guiTex()
    }

    override val textColor: Int = 0x704338
    override val guiStyle: ResourceLocation = CHOCOLATE
}
