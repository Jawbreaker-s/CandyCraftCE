package cn.jawbreakers.candycraftce.misc.multiblocks.caramel_portal

import it.unimi.dsi.fastutil.ints.IntIntPair
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Predicate

/**
 * @param enableCompound   允许复合传送门，如果为true，statePlacer则为必填项
 * @param enableHorizontal 允许传送门水平放置
 * @param horizontalLength 水平放置时的长宽覆盖
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
    val horizontalLength: IntIntPair? = null,//null不覆盖
) {
    val searcher by lazy { CaramelPortalSearcher(this) }
    fun isEmpty(b: BlockState) = isEmpty.test(b)
    fun isFrame(b: BlockState) = isFrame.test(b)
}
