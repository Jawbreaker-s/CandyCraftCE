package cn.jawbreakers.candycraftce.block.blockentity

import cn.breadnicecat.candycraftce.utils.CDataAccessors
import cn.jawbreakers.candycraftce.gui.menu.SugarFactoryMenu
import cn.jawbreakers.candycraftce.recipe.recipes.SugarFactoryRecipe
import cn.jawbreakers.candycraftce.registry.CBlockEntities
import cn.jawbreakers.candycraftce.registry.CItemTags
import cn.jawbreakers.candycraftce.registry.CRecipeTypes
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.ItemStackList
import cn.jawbreakers.candycraftce.utils.TickUnit.SEC2TICK
import cn.jawbreakers.candycraftce.utils.registry.MutableAccessor.Companion.lambda
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.MenuProvider
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeManager.createCheck
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import kotlin.math.max

/**
 * Created in 2024/2/3
 * Project: candycraftce
 *
 * Author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 *
 * Kotlinize in 2026/10/23
 *
 */
//level不应该为null
open class SugarFactoryBE protected constructor(
    blockEntityType: BlockEntityType<*>,
    blockPos: BlockPos,
    blockState: BlockState,
) : BlockEntity(blockEntityType, blockPos, blockState), MenuProvider, WorldlyContainer {
    companion object {
        const val OUTPUT_SLOT: Int = 1
        const val INPUT_SLOT: Int = 0
        const val TICKED_DATA_SLOT: Int = 0
        const val TICKED_TOTAL_DATA_SLOT: Int = 1
        const val RECIPE_TYPE_DATA_SLOT: Int = 2
        const val NULL_TYPE: Int = 0

        val SLOT_FOR_OTHER: IntArray = intArrayOf(INPUT_SLOT)
        val SLOT_FOR_DOWN: IntArray = intArrayOf(OUTPUT_SLOT)

        const val COMMON_TYPE: Int = 1

        val sugaryRecipe: SugarFactoryRecipe = SugarFactoryRecipe(
            "__internal_sugary_recipe".modLoc(),
            Ingredient.of(CItemTags.sugary),
            Items.SUGAR,
            1,
            false
        )

        const val COMMON_TICKED_TOTAL: Int = 10 * SEC2TICK
    }

    private val items: ItemStackList = ItemStackList(2)
    private var ticked = 0
    private var tickedTotal = 0
    private var recipeType: Int = NULL_TYPE
    private var recipeUsed: SugarFactoryRecipe? = null

    private val quickCheck = createCheck(CRecipeTypes.sugar_factory_type.type)

    protected var data: CDataAccessors = CDataAccessors(
        lambda({ ticked }, { v -> ticked = v }),
        lambda({ tickedTotal }, { v -> tickedTotal = v }),
        lambda({ recipeType }, { v -> recipeType = v })
    )

    constructor(blockPos: BlockPos, blockState: BlockState) : this(
        CBlockEntities.sugar_factory_be.get(),
        blockPos,
        blockState
    )

    override fun getDisplayName(): Component = blockState.block.name
    override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu? {
        return SugarFactoryMenu(i, inventory, this, data)
    }

    fun serverTick() {
        val legal = tickBaseLegality()
        tickProgress(legal)
    }


    private fun tickBaseLegality(): Boolean {
        val level = level!!
        if (items.isEmpty(INPUT_SLOT)) return false
        val recipeUsedPrev: SugarFactoryRecipe? = recipeUsed
        //检查配方是否匹配
        if (recipeUsed == null || !recipeUsed!!.matches(this, level)) {
            val recipe = quickCheck.getRecipeFor(this, level).orElse(null)
            recipeUsed = recipe ?: if (sugaryRecipe.matches(this, level)) sugaryRecipe else return false
        }
        if (recipeUsed !== recipeUsedPrev) {
            //如果配方改变了
            tickedTotal = getTickTimeTotal(recipeUsed)
            recipeType = getRecipeType(recipeUsed)
            ticked = 0
        }
        return items.simulateInsert(OUTPUT_SLOT, recipeUsed!!.getResultItem(level.registryAccess())).isEmpty
    }

    private fun tickProgress(legal: Boolean) {
        if (recipeUsed != null && legal) {
            if (++ticked >= tickedTotal) {
                ticked = 0
                items.extract(INPUT_SLOT, 1)
                items.insert(OUTPUT_SLOT, recipeUsed!!.assemble(this, level!!.registryAccess()))
            }
        } else ticked = max(0, ticked - 2)
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        ContainerHelper.loadAllItems(tag.getCompound("items"), items)
        ticked = tag.getInt("ticked")
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        val items = CompoundTag()
        ContainerHelper.saveAllItems(items, this.items)
        tag.put("items", items)
        tag.putInt("ticked", ticked)
    }

    protected open fun getRecipeType(recipe: SugarFactoryRecipe?): Int {
        return if (recipe == null) NULL_TYPE else COMMON_TYPE
    }


    protected open fun getTickTimeTotal(recipe: SugarFactoryRecipe?): Int {
        return if (recipe == null) 0 else COMMON_TICKED_TOTAL
    }

    override fun getSlotsForFace(side: Direction): IntArray {
        return if (side == Direction.DOWN) SLOT_FOR_DOWN else SLOT_FOR_OTHER
    }

    override fun canPlaceItemThroughFace(index: Int, itemStack: ItemStack, direction: Direction?): Boolean {
        return direction != null && direction != Direction.DOWN
    }

    override fun canTakeItemThroughFace(index: Int, stack: ItemStack, direction: Direction): Boolean {
        return direction == Direction.DOWN && (!stack.`is`(Items.SUGAR) || stack.count > 1)
    }

    override fun getContainerSize(): Int = items.size

    override fun isEmpty(): Boolean = items.isEmpty()

    override fun getItem(slot: Int): ItemStack = items[slot]

    override fun removeItem(slot: Int, amount: Int): ItemStack = items.extract(slot, amount)

    override fun removeItemNoUpdate(slot: Int): ItemStack = items.removeAt(slot)

    override fun setItem(slot: Int, stack: ItemStack) {
        items[slot] = stack
    }

    override fun stillValid(player: Player): Boolean = Container.stillValidBlockEntity(this, player)

    override fun clearContent() = items.clear()
}
