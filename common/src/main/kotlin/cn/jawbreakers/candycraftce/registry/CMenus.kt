package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.client.screen.AdvancedFactoryScreen
import cn.jawbreakers.candycraftce.client.screen.ChocolateFurnaceScreen
import cn.jawbreakers.candycraftce.client.screen.LicoriceFurnaceScreen
import cn.jawbreakers.candycraftce.client.screen.SugarFactoryScreen
import cn.jawbreakers.candycraftce.gui.menu.AdvancedSugarFactoryMenu
import cn.jawbreakers.candycraftce.gui.menu.SugarFactoryMenu
import cn.jawbreakers.candycraftce.menu.ChocolateFurnaceMenu
import cn.jawbreakers.candycraftce.menu.LicoriceFurnaceMenu
import cn.jawbreakers.candycraftce.mixin.render.MenuScreensAccessor
import cn.jawbreakers.candycraftce.registry.CBlockEntities.advanced_sugar_factory_be
import cn.jawbreakers.candycraftce.registry.CBlockEntities.chocolate_furnace_be
import cn.jawbreakers.candycraftce.registry.CBlockEntities.licorice_furnace_be
import cn.jawbreakers.candycraftce.registry.CBlockEntities.sugar_factory_be
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.whenInitialized
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType

/**
 * Created in 2026/10/23 20:17 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
object CMenus {
    init {
        CLogUtils.sign()
    }

    val sugar_factory_menu = register(sugar_factory_be.name, ::SugarFactoryMenu)
    val advanced_sugar_factory_menu = register(advanced_sugar_factory_be.name, ::AdvancedSugarFactoryMenu)
    val licorice_furnace_menu = register(licorice_furnace_be.name, ::LicoriceFurnaceMenu)
    val chocolate_furnace_menu = register(chocolate_furnace_be.name, ::ChocolateFurnaceMenu)

    init {
        ifClient {
            whenInitialized {
                MenuScreensAccessor.register(sugar_factory_menu.get(), ::SugarFactoryScreen)
                MenuScreensAccessor.register(advanced_sugar_factory_menu.get(), ::AdvancedFactoryScreen)
                MenuScreensAccessor.register(licorice_furnace_menu.get(), ::LicoriceFurnaceScreen)
                MenuScreensAccessor.register(chocolate_furnace_menu.get(), ::ChocolateFurnaceScreen)
            }
        }
    }

    fun <M : AbstractContainerMenu> register(name: String, constructor: MenuType.MenuSupplier<M>) =
        CPlatformUtils.registerMenu(name) {
            MenuType(constructor, FeatureFlagSet.of())
        }
}