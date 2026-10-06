package cn.jawbreakers.candycraftce.item.debugger

import cn.breadnicecat.candycraftce.core.item.items.debugger.DebugFunc
import cn.jawbreakers.candycraftce.utils.CLevelUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.translate
import cn.jawbreakers.candycraftce.utils.CUtils.getBlockPos
import cn.jawbreakers.candycraftce.utils.CUtils.putBlockPos
import net.minecraft.ChatFormatting
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import kotlin.time.measureTime

/**
 * Created in 2026/10/6 15:28 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
object SliceChunk : DebugFunc("slice_chunk") {
    override val displayName = Component.translatable(description)
        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
        .translate("Slice Chunk", "区块切片")
    val confirm_tip: Component = Component.translatable("$description.confirm")
        .withStyle(ChatFormatting.GOLD)
        .translate("Right-click the block again to confirm", "再次右键该方块确认操作")
    val radius_tip: Component = Component.translatable("$description.radius")
        .withStyle(ChatFormatting.GREEN)
        .translate("Chunk Radius: ", "区块半径: ")
    const val MAX_RADIUS = 2

    override fun leftClickOn(
        data: CompoundTag,
        level: Level,
        pos: BlockPos,
        clickedFace: Direction,
        player: Player,
        item: ItemStack,
    ) {
        if (level.isClientSide) return

        var rad = data.getInt("radius").coerceIn(0, MAX_RADIUS)

        val delta = if (player.isShiftKeyDown) -1 else 1
        rad += delta
        if (rad > MAX_RADIUS) rad = 0
        else if (rad < 0) rad = MAX_RADIUS
        data.putInt("radius", rad)
        sendMessage(player, radius_tip.copy().append(rad.toString()))
    }

    override fun rightClickOn(
        data: CompoundTag,
        level: Level,
        pos: BlockPos,
        clickedFace: Direction,
        player: Player,
        item: ItemStack,
    ): Boolean {
        if (level.isClientSide) return true

        val selected = data.getBlockPos("selected")
        if (selected == pos) {
            val rad = data.getInt("radius").coerceIn(0, MAX_RADIUS)
            var cnt = 0
            measureTime {
                val chunk = level.getChunk(pos)
                val chunks = buildList {
                    val center = chunk.pos
                    if (rad == 0) {
                        add(center)
                    } else {
                        for (dx in -rad..rad) {
                            for (dz in -rad..rad) {
                                add(ChunkPos(center.x + dx, center.z + dz))
                            }
                        }
                    }
                }
                val mutable = BlockPos.MutableBlockPos()
                chunks.forEach { chunkPos ->
                    for (x in chunkPos.minBlockX..chunkPos.maxBlockX) {
                        mutable.x = x
                        for (z in chunkPos.minBlockZ..chunkPos.maxBlockZ) {
                            mutable.z = z
                            for (y in level.minBuildHeight..level.maxBuildHeight) {
                                mutable.y = y
                                if (level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 2 or 16 or 32)) {
                                    cnt++
                                }
                            }
                        }
                    }
                }
                data.remove("selected")
            }.also { sendMessage(player, timing_tip_pre.copy().append("$it ($cnt Blocks)")) }
        } else {
            data.putBlockPos("selected", pos)
            sendMessage(player, confirm_tip)
        }
        return true
    }

    override fun onInventoryTick(data: CompoundTag, level: Level, player: Player, item: ItemStack, isInHand: Boolean) {
        if (isInHand && level is ClientLevel && player.tickCount % 10 == 0) {
            val zero = data.getBlockPos("selected") ?: return
            if (player.position().distanceTo(zero.center) <= 128) {
                CLevelUtils.particleBlock(ParticleTypes.FLAME, level, zero, 0.25)
                val chunk = level.getChunk(zero)
                val base = chunk.pos
                val rad = data.getInt("radius").coerceIn(0, MAX_RADIUS)
                val minChunk = ChunkPos(base.x - rad, base.z - rad)
                val maxChunk = ChunkPos(base.x + rad, base.z + rad)
                CLevelUtils.particleBlock(
                    ParticleTypes.ANGRY_VILLAGER, level,
                    BlockPos(minChunk.minBlockX, chunk.minBuildHeight, minChunk.minBlockZ),
                    BlockPos(maxChunk.maxBlockX, chunk.maxBuildHeight, maxChunk.maxBlockZ),
                    1.0
                )
            }
        }
    }
}