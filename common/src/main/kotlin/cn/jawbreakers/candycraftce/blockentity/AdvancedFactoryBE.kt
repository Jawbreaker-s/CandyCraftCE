package cn.jawbreakers.candycraftce.blockentity

import cn.jawbreakers.candycraftce.gui.menu.AdvancedSugarFactoryMenu
import cn.jawbreakers.candycraftce.recipe.recipes.SugarFactoryRecipe
import cn.jawbreakers.candycraftce.registry.CBlockEntities
import cn.jawbreakers.candycraftce.utils.TickUnit.SEC2TICK
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState


/**
 * Created in 2024/2/4
 * Project: candycraftce
 * 
 * author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * 
 * 
 */
class AdvancedFactoryBE(blockEntityType: BlockEntityType<*>, blockPos: BlockPos, blockState: BlockState) :
    SugarFactoryBE(blockEntityType, blockPos, blockState) {

    companion object {
        const val ADVANCED_TYPE: Int = 3
        const val ADVANCED_TICKED_TOTAL: Int = 6 * SEC2TICK
    }

    constructor(blockPos: BlockPos, blockState: BlockState) : this(
        CBlockEntities.advanced_sugar_factory_be.get(),
        blockPos,
        blockState
    )

    override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
        return AdvancedSugarFactoryMenu(i, inventory, this, data)
    }

    override fun getRecipeType(recipe: SugarFactoryRecipe?): Int {
        return if (recipe != null && recipe.advanced) ADVANCED_TYPE else super.getRecipeType(recipe)
    }

    override fun getTickTimeTotal(recipe: SugarFactoryRecipe?): Int {
        return if (recipe != null && !recipe.advanced) ADVANCED_TICKED_TOTAL else super.getTickTimeTotal(recipe)
    }
}
