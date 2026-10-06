package cn.breadnicecat.candycraftce.core.item.items.debugger

import cn.breadnicecat.candycraftce.core.item.items.debugger.DebugFunc.Companion.DEBUG_TRANS_PREFIX
import cn.jawbreakers.candycraftce.item.debugger.SliceChunk
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.translate
import cn.jawbreakers.candycraftce.utils.CUtils.useCompound
import net.minecraft.ChatFormatting.GREEN
import net.minecraft.ChatFormatting.YELLOW
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

/**
 * Created by NiceCat on 2025/12/11.
 * Project: candycraftce
 * @author <a href="https://github.com/BreadNiceCat">Bread_NiceCat</a>
 *
 */
class MarshmallowDebuggerItem(properties: Properties) : Item(properties) {
    companion object {
        private val current_func_tip_pre = Component.translatable("$DEBUG_TRANS_PREFIX.current_func")
            .withStyle(GREEN)
            .translate("Current Mode: ", "当前模式: ")
        private val next_func_tip = Component.translatable("$DEBUG_TRANS_PREFIX.next_func_tip")
            .withStyle(YELLOW)
            .translate("Press Shift+Right Click on the Air to switch mode", "对空气 SHIFT+右键 切换模式")

        val funcs = buildList {
            //不用map：后面需要索引循环
            add(Nop)
            add(Measuring)
            add(PortalTest)
            add(SliceChunk)
        }

    }

    private fun getCurrentFunc(stack: ItemStack): DebugFunc {
        val string = stack.orCreateTag.getString("current")
        val f = funcs.firstOrNull { it.id == string } ?: funcs.first()
        return f
    }

    private fun setCurrentFunc(stack: ItemStack, func: DebugFunc) {
        val prevFunc = getCurrentFunc(stack)
        if (prevFunc != func) {
            stack.orCreateTag.putString("current", func.id)
        }
    }

    private fun getOrCreateData(stack: ItemStack): CompoundTag {
        val nbt = stack.orCreateTag
        val key = nbt.getString("current")
        return nbt.useCompound("data") { dataSet ->
            dataSet.useCompound(key) { funcData -> funcData }
        }
    }


    override fun appendHoverText(
        stack: ItemStack,
        level: Level?,
        tooltips: MutableList<Component>,
        isAdvanced: TooltipFlag,
    ) {
        val func = getCurrentFunc(stack)
        val data = getOrCreateData(stack)
        tooltips.add(current_func_tip_pre.copy().append(func.displayName))
        tooltips.add(next_func_tip)
        func.appendTooltip(data, stack, tooltips, isAdvanced)
    }


    override fun getName(stack: ItemStack): Component {
        return super.getName(stack).copy()
            .append("(")
            .append(getCurrentFunc(stack).displayName)
            .append(")")
    }


    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack?> {
        val stack = player.getItemInHand(usedHand)

        if (player.isShiftKeyDown) {
            if (!level.isClientSide) {
                val idx = (funcs.indexOf(getCurrentFunc(stack)) + 1) % funcs.size

                @Suppress("UNCHECKED_CAST")
                val func = funcs[idx]
                setCurrentFunc(stack, func)
                player.sendSystemMessage(current_func_tip_pre.copy().append(func.displayName))
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
        }
        val f = getCurrentFunc(stack)
        val data = getOrCreateData(stack)

        val result = f.use(data, level, player, stack)

        return if (result) {
            InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
        } else {
            InteractionResultHolder.pass(stack)
        }
    }

    override fun canAttackBlock(state: BlockState, level: Level, pos: BlockPos, player: Player): Boolean {
        val stack = player.mainHandItem

        val view = player.getViewVector(1f)
        val face = Direction.getNearest(view.x, view.y, view.z)

        val f = getCurrentFunc(stack)
        val data = getOrCreateData(stack)

        f.leftClickOn(data, level, pos, face, player, stack)
        return false
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val player = context.player ?: return InteractionResult.PASS
        val level = context.level
        val stack = context.itemInHand

        val view = player.getViewVector(1f)
        val face = Direction.getNearest(view.x, view.y, view.z)

        val f = getCurrentFunc(stack)
        val data = getOrCreateData(stack)

        val result = f.rightClickOn(data, level, context.clickedPos, face, player, stack)

        return if (result) {
            InteractionResult.sidedSuccess(level.isClientSide)
        } else {
            InteractionResult.PASS
        }
    }

    override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slotId: Int, isSelected: Boolean) {
        if (entity is Player) {
            val f = getCurrentFunc(stack)
            val data = getOrCreateData(stack)

            f.onInventoryTick(data, level, entity, stack, entity.handSlots.any { it == stack })
        }
    }

    override fun isFoil(stack: ItemStack): Boolean = true
}