package cn.jawbreakers.candycraftce.gui.menu

import cn.jawbreakers.candycraftce.registry.CMenus
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.SimpleContainerData

/**
 * Created in 2024/2/5
 * Project: candycraftce
 * 
 * author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * 
 * 
 */
class AdvancedSugarFactoryMenu private constructor(
    type: MenuType<*>?,
    id: Int,
    inventory: Inventory,
    container: Container,
    data: ContainerData,
) : SugarFactoryMenu(type, id, inventory, container, data) {
    constructor(
        id: Int,
        inventory: Inventory,
        container: Container,
        data: ContainerData,
    ) : this(CMenus.advanced_sugar_factory_menu.get(), id, inventory, container, data)

    constructor(id: Int, inv: Inventory) : this(
        CMenus.advanced_sugar_factory_menu.get(),
        id,
        inv,
        SimpleContainer(2),
        SimpleContainerData(4)
    )
}
