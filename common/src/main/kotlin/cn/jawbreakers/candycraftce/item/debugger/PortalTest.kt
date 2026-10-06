package cn.breadnicecat.candycraftce.core.item.items.debugger

import cn.jawbreakers.candycraftce.block.CaramelPortalBlock
import cn.jawbreakers.candycraftce.misc.multiblocks.caramel_portal.VectorPortalShape
import cn.jawbreakers.candycraftce.utils.CLevelUtils
import cn.jawbreakers.candycraftce.utils.CLevelUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.translate
import com.mojang.serialization.JsonOps
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtUtils
import net.minecraft.network.chat.Component
import net.minecraft.util.GsonHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import kotlin.jvm.optionals.getOrNull
import kotlin.time.measureTime

/**
 * Created by NiceCat on 2025/12/13.
 * Project: candycraftce
 * @author <a href="https://github.com/BreadNiceCat">Bread_NiceCat</a>
 *
 */
object PortalTest : DebugFunc("portal_test") {
    override val displayName: Component = Component.translatable(description)
        .withStyle(ChatFormatting.LIGHT_PURPLE)
        .translate("Portal Test", "传送门测试")
    val test_tip: Component = Component.translatable("$description.test")
        .withStyle(ChatFormatting.YELLOW)
        .translate("Right Click to Find Portal", "右键点击寻找传送门")
    val notfound_tip: Component = Component.translatable("$description.notfound")
        .withStyle(ChatFormatting.RED)
        .translate("Portal Frame not found", "未找到传送门框架")
    val found_tip: Component = Component.translatable("$description.found")
        .withStyle(ChatFormatting.GREEN)
        .translate("Portal Frame found", "传送门框架已找到")


    override fun rightClickOn(
        data: CompoundTag,
        level: Level,
        pos: BlockPos,
        clickedFace: Direction,
        player: Player,
        item: ItemStack,
    ): Boolean {
        level.ifClient {
            val pos0 = pos.relative(clickedFace.opposite)
            measureTime {
                val portal = VectorPortalShape.findPortal(level, pos0, CaramelPortalBlock.config).getOrNull()
                if (portal == null) {
                    CLevelUtils.particleBlock(ParticleTypes.FLAME, it, pos0, 0.25)
                    sendMessage(player, notfound_tip)
                } else {
                    portal.units.forEach { unit ->
                        CLevelUtils.particleBlock(ParticleTypes.FLAME, it, unit.bottomLeft, unit.topRight, 0.25)
                        CLevelUtils.particleBlock(ParticleTypes.HAPPY_VILLAGER, it, unit.bottomLeft, 0.25)
                    }
                    sendMessage(player, found_tip)
                    sendMessage(
                        player,
                        NbtUtils.toPrettyComponent(
                            JsonOps.INSTANCE.convertTo(
                                NbtOps.INSTANCE,
                                GsonHelper.parse(portal.toString())
                            )
                        )
                    )
                }
            }.also { time ->
                sendMessage(player, timing_tip_pre.copy().append(Component.literal(time.toString())))
            }
        }
        return true
    }

    override fun appendTooltip(
        data: CompoundTag,
        item: ItemStack,
        tooltips: MutableList<Component>,
        isAdvanced: TooltipFlag,
    ) {
        tooltips.add(test_tip)
    }
}