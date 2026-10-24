package cn.jawbreakers.candycraftce.block.blockentity


import cn.breadnicecat.candycraftce.utils.CDataAccessors
import cn.jawbreakers.candycraftce.menu.LicoriceFurnaceMenu
import cn.jawbreakers.candycraftce.recipe.recipes.SugarFurnaceRecipe
import cn.jawbreakers.candycraftce.registry.CBlockEntities
import cn.jawbreakers.candycraftce.registry.CRecipeTypes
import cn.jawbreakers.candycraftce.registry.CSugarFuels
import cn.jawbreakers.candycraftce.utils.ItemStackList
import cn.jawbreakers.candycraftce.utils.TickUnit.SEC2TICK
import cn.jawbreakers.candycraftce.utils.registry.MutableAccessor.Companion.lambda
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.MenuProvider
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.level.block.AbstractFurnaceBlock.LIT
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import kotlin.math.max

open class LicoriceFurnaceBE protected constructor(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState,
    private var tickedTotal: Int,
) : BlockEntity(type, pos, state), MenuProvider, WorldlyContainer {
    companion object {
        const val INPUT_SLOT: Int = 0
        const val FUEL_SLOT: Int = 1
        const val OUTPUT_SLOT: Int = 2

        const val TICKED_DATA: Int = 0
        const val TICKED_TOTAL_DATA: Int = 1
        const val LIT_TIME_DATA: Int = 2
        const val LIT_TIME_TOTAL_DATA: Int = 3
        val SLOT_FOR_UP: IntArray = intArrayOf(INPUT_SLOT)
        val SLOT_FOR_DOWN: IntArray = intArrayOf(OUTPUT_SLOT)
        val SLOT_FOR_SIDE: IntArray = intArrayOf(FUEL_SLOT)
    }

    private val items: ItemStackList = ItemStackList(3)
    private var exp = 0f
    private var litTime = 0
    private var litTimeTotal = 0


    private var ticked = 0
    private var recipeUsed: SugarFurnaceRecipe? = null

    private val quickCheck = RecipeManager.createCheck(CRecipeTypes.sugar_furnace_type.type)

    val data: CDataAccessors = CDataAccessors(
        lambda({ this.ticked }, { v -> this.ticked = v }),
        lambda({ this.tickedTotal }, { v -> this.tickedTotal = v }),
        lambda({ this.litTime }, { v -> this.litTime = v }),
        lambda({ this.litTimeTotal }, { v -> this.litTimeTotal = v })
    )

    constructor(blockPos: BlockPos, blockState: BlockState) : this(
        CBlockEntities.licorice_furnace_be.get(),
        blockPos,
        blockState,
        10 * SEC2TICK
    )

    fun serverTick() {
        var legal = tickBaseLegality()
        legal = legal and tickFuel(legal)
        tickProgress(legal)
    }

    private fun tickBaseLegality(): Boolean {
        val level = level!!
        if (items.isEmpty(INPUT_SLOT)) return false
        if (recipeUsed == null || !recipeUsed!!.matches(this, level)) {
            recipeUsed = quickCheck.getRecipeFor(this, level).orElse(null) ?: return false
        }
        return items.simulateInsert(OUTPUT_SLOT, recipeUsed!!.getResultItem(level.registryAccess())).isEmpty
    }

    private fun tickFuel(ignite: Boolean): Boolean {
        litTime = max(0, litTime - 1)
        if (litTime == 0) {
            if (ignite && !items.isEmpty(FUEL_SLOT)) {
                val nf = CSugarFuels.getBurnDuration(items.extract(FUEL_SLOT, 1))
                if (nf != 0) {
                    litTime = nf
                    litTimeTotal = litTime
                }
            }
        }
        val lit = litTime > 0
        val state = blockState
        if (state.getValue(LIT) != lit) {
            state.setValue(LIT, lit)
            setChanged()
        }
        return lit
    }

    private fun tickProgress(promote: Boolean) {
        val recipeUsed = recipeUsed
        if (recipeUsed != null && promote) {
            if (++ticked >= tickedTotal) {
                ticked = 0
                items.extract(INPUT_SLOT, 1)
                items.insert(OUTPUT_SLOT, recipeUsed.assemble(this, level!!.registryAccess()))
            }
        } else ticked = max(0, ticked - 2)
    }

    override fun getDisplayName(): Component = blockState.block.name

    override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu? {
        return LicoriceFurnaceMenu(i, inventory, this, data)
    }


    fun dropExp(pos: Vec3) {
        if (exp > 0f) {
            val orb = ExperienceOrb(level!!, pos.x, pos.y, pos.z, Mth.floor(exp))
            this.exp = 0f
            setChanged()
            level!!.addFreshEntity(orb)
        }
    }

    override fun saveAdditional(tag: CompoundTag) {
        ContainerHelper.saveAllItems(tag, this.items)
        tag.putFloat("exp", exp)
        tag.putInt("litTime", litTime)
        tag.putInt("litTimeTotal", litTimeTotal)
        tag.putInt("ticked", ticked)
        super.saveAdditional(tag)
    }

    override fun load(tag: CompoundTag) {
        ContainerHelper.loadAllItems(tag, this.items)
        exp = tag.getFloat("exp")
        litTime = tag.getInt("litTime")
        litTimeTotal = tag.getInt("litTimeTotal")
        ticked = tag.getInt("ticked")
        super.load(tag)
    }

    override fun getSlotsForFace(side: Direction): IntArray {
        return when (side) {
            Direction.UP -> SLOT_FOR_UP
            Direction.DOWN -> SLOT_FOR_DOWN
            else -> SLOT_FOR_SIDE
        }
    }

    override fun canPlaceItemThroughFace(index: Int, itemStack: ItemStack, direction: Direction?): Boolean {
        return direction != null && direction != Direction.DOWN
    }

    override fun canTakeItemThroughFace(index: Int, stack: ItemStack, direction: Direction): Boolean {
        return direction == Direction.DOWN
    }

    override fun getContainerSize(): Int = items.size

    override fun isEmpty(): Boolean = items.isEmpty()

    override fun getItem(slot: Int): ItemStack = items[slot]

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        return ContainerHelper.removeItem(items, slot, amount)
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        return ContainerHelper.takeItem(items, slot)
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        items[slot] = stack
    }

    override fun stillValid(player: Player): Boolean {
        return Container.stillValidBlockEntity(this, player)
    }

    override fun clearContent() = items.clear()
}
