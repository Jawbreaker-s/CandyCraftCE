package cn.breadnicecat.candycraftce.core.item.items.debugger

import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.translate
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * Created by NiceCat on 2025/12/12.
 * Project: candycraftce
 * @author <a href="https://github.com/BreadNiceCat">Bread_NiceCat</a>
 *
 */
object Nop : DebugFunc("nop") {
    override val displayName: Component = Component.translatable(description)
        .withStyle(ChatFormatting.GRAY)
        .translate("Nop", "无操作")

    private fun withSide(level: Level): String = when (level.isClientSide) {
        true -> "Client"; false -> "Server"
    }

    override fun use(
        data: CompoundTag,
        level: Level,
        player: Player,
        item: ItemStack,
    ): Boolean {
        clog.info("use ${withSide(level)}")
        return super.use(data, level, player, item)
    }

    override fun rightClickOn(
        data: CompoundTag,
        level: Level,
        pos: BlockPos,
        clickedFace: Direction,
        player: Player,
        item: ItemStack,
    ): Boolean {
        clog.info("rightClickOn ${withSide(level)}")
        return super.rightClickOn(data, level, pos, clickedFace, player, item)
    }

    override fun leftClickOn(
        data: CompoundTag,
        level: Level,
        pos: BlockPos,
        clickedFace: Direction,
        player: Player,
        item: ItemStack,
    ) {
        clog.info("leftClickOn ${withSide(level)}")
    }

}