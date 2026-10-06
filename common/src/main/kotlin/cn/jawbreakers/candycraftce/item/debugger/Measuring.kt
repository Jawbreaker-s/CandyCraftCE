package cn.breadnicecat.candycraftce.core.item.items.debugger

import cn.jawbreakers.candycraftce.utils.CLevelUtils
import cn.jawbreakers.candycraftce.utils.CLevelUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.translate
import net.minecraft.ChatFormatting
import net.minecraft.ChatFormatting.GREEN
import net.minecraft.ChatFormatting.YELLOW
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Component.translatable
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
object Measuring : DebugFunc("measuring") {

    override val displayName: Component = translatable(description)
        .withStyle(ChatFormatting.AQUA)
        .translate("Measuring", "测距")
    val set_tip: Component = translatable("$description.set")
        .withStyle(YELLOW)
        .translate("Left Click to set zero", "左键 设置调零")
    val reset_tip: Component = translatable("$description.reset")
        .withStyle(YELLOW)
        .translate("Press Shift+Left Click to reset", "SHIFT+左键 清除调零")
    val get_tip: Component = translatable("$description.get")
        .withStyle(YELLOW)
        .translate("Right Click to measure coords", "右键 获取坐标")
    val zero_tip_pre: Component = translatable("$description.zero_pre")
        .withStyle(GREEN)
        .translate("Zero Pos: ", "当前零点: ")


    override fun appendTooltip(
        data: CompoundTag,
        item: ItemStack,
        tooltips: MutableList<Component>,
        isAdvanced: TooltipFlag,
    ) {
        tooltips.add(set_tip)
        tooltips.add(reset_tip)
        tooltips.add(get_tip)
        tooltips.add(zero_tip_pre.copy().append(data.zeroToString()))
    }

    override fun leftClickOn(
        data: CompoundTag,
        level: Level,
        pos: BlockPos,
        clickedFace: Direction,
        player: Player,
        item: ItemStack,
    ) {
        if (player.isShiftKeyDown) {
            data.reset()
        } else {
            data.setZero(pos)
        }
        sendMessage(player, zero_tip_pre.copy().append(data.zeroToString()), level.isClientSide)
    }

    override fun rightClickOn(
        data: CompoundTag,
        level: Level,
        pos: BlockPos,
        clickedFace: Direction,
        player: Player,
        item: ItemStack,
    ): Boolean {
        sendMessage(player, data.get(pos).toShortString(), level.isClientSide) {
            withStyle(GREEN)
        }
        level.ifClient {
            CLevelUtils.particleBlock(ParticleTypes.HAPPY_VILLAGER, it, pos, 0.25)
        }
        return true
    }


    override fun onInventoryTick(
        data: CompoundTag,
        level: Level,
        player: Player,
        item: ItemStack,
        isInHand: Boolean,
    ) {
        if (isInHand && level is ClientLevel && !data.isZeroZero() && player.tickCount % 3 == 0) {
            val zero = data.getZero()
            if (player.position().distanceTo(zero.center) <= 128) {
                CLevelUtils.particleBlock(ParticleTypes.FLAME, level, zero, 0.25)
            }
        }
    }

    private var CompoundTag.x
        get() = getInt("x")
        set(value) = putInt("x", value)
    private var CompoundTag.y
        get() = getInt("y")
        set(value) = putInt("y", value)
    private var CompoundTag.z
        get() = getInt("z")
        set(value) = putInt("z", value)

    private fun CompoundTag.zeroToString(): String {
        return "$x, $y, $z"
    }

    private fun CompoundTag.isZeroZero(): Boolean = x == 0 || y == 0 || z == 0
    private fun CompoundTag.setZero(x0: Int? = null, y0: Int? = null, z0: Int? = null) {
        if (x0 != null) x = x0
        if (y0 != null) y = y0
        if (z0 != null) z = z0
    }

    private fun CompoundTag.setZero(blockPos: BlockPos) = setZero(blockPos.x, blockPos.y, blockPos.z)
    private fun CompoundTag.reset() = setZero(0, 0, 0)
    private fun CompoundTag.getZero() = BlockPos(x, y, z)
    private fun CompoundTag.get(blockPos: BlockPos): BlockPos {
        if (isZeroZero()) return blockPos
        return getZero().multiply(-1).offset(blockPos.x, blockPos.y, blockPos.z)
    }

}