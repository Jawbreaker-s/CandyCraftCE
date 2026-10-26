package cn.jawbreakers.candycraftce.gui.menu

import cn.jawbreakers.candycraftce.block.blockentity.SugarFactoryBE
import cn.jawbreakers.candycraftce.menu.slot.SimpleResultSlot
import cn.jawbreakers.candycraftce.registry.CMenus.sugar_factory_menu
import cn.jawbreakers.candycraftce.utils.CUtils.getItemById
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.*
import net.minecraft.world.item.ItemStack


/**
 * Created in 2024/2/4
 * Project: candycraftce
 * 
 * author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * 
 * 
 * Kotlinize in 2026/10/23
 */
open class SugarFactoryMenu protected constructor(
    type: MenuType<*>?,
    id: Int,
    val inventory: Inventory,
    val container: Container,
    val data: ContainerData,
) : AbstractContainerMenu(type, id) {
    companion object {
        private const val INV_START = 0
        private const val INV_END = 35
        private const val INPUT_SLOT = 36
        private const val OUTPUT_SLOT = 37
    }

    init {
        //player inventory
        for (i in 0..2) {
            for (k in 0..8) {
                this.addSlot(Slot(inventory, k + i * 9 + 9, 7 + k * 18, 33 + i * 18))
            }
        }
        for (i in 0..8) {
            this.addSlot(Slot(inventory, i, 7 + i * 18, 33 + 58))
        }
        addSlot(Slot(container, SugarFactoryBE.INPUT_SLOT, 7, 7))
        addSlot(SimpleResultSlot(container, SugarFactoryBE.OUTPUT_SLOT, 151, 7).apply {
            fakeItem = { data[SugarFactoryBE.OUTPUT_ITEM_DATA_SLOT].getItemById() }
        })
        addDataSlots(data)
    }

    constructor(
        id: Int,
        inventory: Inventory,
        container: Container,
        data: ContainerData,
    ) : this(sugar_factory_menu.get(), id, inventory, container, data)

    constructor(id: Int, inv: Inventory) : this(id, inv, SimpleContainer(2), SimpleContainerData(4))

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        if (index in INV_START..INV_END) {
            moveItemStackTo(slots[index].item, INPUT_SLOT, INPUT_SLOT + 1, false)
        } else {
            moveItemStackTo(slots[index].item, INV_START, INV_END + 1, false)
        }
        return ItemStack.EMPTY
    }

    override fun stillValid(player: Player): Boolean = container.stillValid(player)
}
