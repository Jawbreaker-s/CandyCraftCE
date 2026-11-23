package cn.jawbreakers.candycraftce.item.debugger

import cn.breadnicecat.candycraftce.core.item.items.debugger.DebugFunc
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.utils.CLevelUtils.component1
import cn.jawbreakers.candycraftce.utils.CLevelUtils.component2
import cn.jawbreakers.candycraftce.utils.CLevelUtils.component3
import cn.jawbreakers.candycraftce.utils.CLevelUtils.ifServer
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.translate
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Component.translatable
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.Heightmap
import kotlin.math.sqrt

/**
 * Created in 2026/10/21 19:06 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
object ListBlocks : DebugFunc("list_blocks") {
    override val displayName: Component = translatable(descriptionId)
        .withStyle(ChatFormatting.YELLOW)
        .translate("List Blocks", "枚举方块")

    override fun rightClickOn(
        data: CompoundTag,
        level: Level,
        pos: BlockPos,
        clickedFace: Direction,
        player: Player,
        item: ItemStack,
    ): Boolean {
        level.ifServer { level ->
            val base = Blocks.WHITE_CONCRETE.defaultBlockState()
            val states = CBlocks.blocks
                .asSequence()
                .map { it.value.get() }
                .flatMap { it.stateDefinition.possibleStates }
                .toList()
            val size = states.size
            val d = Mth.ceil(sqrt(size.toFloat()))
            val mu = pos.mutable()
            val (x, y, z) = pos
            mu.y = y + 1
            for (dx in -1..<(d - 1) * 2) {
                mu.x = x + dx
                for (dz in -1..<(d - 1) * 2) {
                    mu.z = z + dz
                    val top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, mu.x, mu.z)
                    for (y in top downTo y + 2) {
                        mu.y = y
                        level.setBlock(mu, Blocks.AIR.defaultBlockState(), 2 or 16 or 32)
                    }
                    mu.y = y + 1
                    level.setBlock(mu, base, 2 or 16 or 32)
                    level.setBlock(mu.above(), Blocks.BARRIER.defaultBlockState(), 2 or 16 or 32)
                }
            }
            states.forEachIndexed { index, state ->
                mu.y = y + 2
                val row = index / d
                val col = index % d
                mu.x = x + 2 * row
                mu.z = z + 2 * col
                level.setBlock(mu, state, 2 or 16 or 32)
            }

        }
        return true
    }
}