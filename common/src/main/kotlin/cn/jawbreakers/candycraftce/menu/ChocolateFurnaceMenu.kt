package cn.jawbreakers.candycraftce.menu

import cn.jawbreakers.candycraftce.registry.CMenus
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.SimpleContainerData

/**
 * Created in 2024/2/3
 * Project: candycraftce
 * 
 * author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * 
 * 
 */
class ChocolateFurnaceMenu : LicoriceFurnaceMenu {

    constructor(i: Int, inventory: Inventory) : this(
        i,
        inventory,
        SimpleContainer(3),
        SimpleContainerData(4)
    )

    constructor(containerId: Int, inventory: Inventory, container: Container, containerData: ContainerData) : super(
        CMenus.chocolate_furnace_menu.get(),
        containerId,
        inventory,
        container,
        containerData
    )
}
