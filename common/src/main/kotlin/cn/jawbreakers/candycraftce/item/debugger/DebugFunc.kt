package cn.breadnicecat.candycraftce.core.item.items.debugger

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.translate
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

/**
 * Created by NiceCat on 2025/12/11.
 * Project: candycraftce
 * @author <a href="https://github.com/BreadNiceCat">Bread_NiceCat</a>
 *
 */
abstract class DebugFunc(val id: String) {
    companion object {
        const val DEBUG_TRANS_PREFIX = "idebugger.$MOD_ID"
        val timing_tip_pre: Component = Component.translatable("$DEBUG_TRANS_PREFIX.timing_pre")
            .withStyle(ChatFormatting.GOLD)
            .translate("Taking: ", "耗时: ")
    }

    protected val description by lazy { "$DEBUG_TRANS_PREFIX.fun.${id}" }
    abstract val displayName: Component

    /**
     * @return 是否处理,如果返回`false`,则会返回[net.minecraft.world.InteractionResult.PASS]
     **/
    open fun use(
        data: CompoundTag,
        level: Level,
        player: Player,
        item: ItemStack,
    ): Boolean = false

    /**
     * @return 是否处理,如果返回`false`,还会调用[use]
     **/
    open fun rightClickOn(
        data: CompoundTag,
        level: Level,
        pos: BlockPos,
        clickedFace: Direction,
        player: Player,
        item: ItemStack,
    ): Boolean = false

    open fun leftClickOn(
        data: CompoundTag,
        level: Level,
        pos: BlockPos,
        clickedFace: Direction,
        player: Player,
        item: ItemStack,
    ) {
    }

    open fun onInventoryTick(
        data: CompoundTag,
        level: Level,
        player: Player,
        item: ItemStack,
        isInHand: Boolean,
    ) {
    }

    open fun appendTooltip(
        data: CompoundTag,
        item: ItemStack,
        tooltips: MutableList<Component>,
        isAdvanced: TooltipFlag,
    ) {
    }

    inline fun DebugFunc.sendMessage(
        player: Player,
        message: String,
        condition: Boolean = true,
        mod: MutableComponent.() -> Unit = {},
    ) {
        if (condition) sendMessage(player, Component.literal(message).apply(mod))
    }

    fun DebugFunc.sendMessage(player: Player, message: Component, condition: Boolean = true) {
        if (condition) player.sendSystemMessage(displayName.copy().append(": ").append(message))
    }

}