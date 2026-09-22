package cn.jawbreakers.candycraftce.misc.multiblocks.caramel_portal

import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Predicate

/**
 * @param enableCompound   允许复合传送门，如果为true，statePlacer则为必填项
 * @param enableHorizontal 允许传送门水平放置
 */
data class PortalConfig(
    val minWidth: Int,
    val maxWidth: Int,
    val minHeight: Int,
    val maxHeight: Int,
    val isEmpty: Predicate<BlockState>,
    val isFrame: Predicate<BlockState>,
    val enableCompound: Boolean = true,
    val enableHorizontal: Boolean = false,
) {
    fun isEmpty(b: BlockState): Boolean {
        return isEmpty.test(b)
    }

    fun isFrame(b: BlockState): Boolean {
        return isFrame.test(b)
    }
}

fun interface PortalLighter {
    fun light(level: Level, portal: VectorPortalShape): Boolean
}