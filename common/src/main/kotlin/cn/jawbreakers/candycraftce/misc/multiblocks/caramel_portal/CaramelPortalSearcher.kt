package cn.jawbreakers.candycraftce.misc.multiblocks.caramel_portal

import cn.jawbreakers.candycraftce.utils.CLevelUtils.getNeighbourPos
import it.unimi.dsi.fastutil.ints.IntIntPair
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Direction.Axis
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.state.BlockState
import java.util.*
import java.util.function.Predicate

/**
 * Created by NiceCat on 2025/12/10.
 * Project: candycraftce
 * @author <a href="https://github.com/BreadNiceCat">Bread_NiceCat</a>
 *
 * Kotlinize in 2026/11/12
 */
class CaramelPortalSearcher internal constructor(val config: PortalConfig) {
    companion object {
        private val XZY: Array<Axis> = arrayOf(Axis.X, Axis.Z, Axis.Y)
    }

    /**
     * 根据框架找
     * @param pos 任意一格有效框架
     */
    fun findPortalOnFrame(getter: BlockGetter, pos: BlockPos): List<CaramelPortalShape> {
        if (!config.isFrame(getter.getBlockState(pos))) return emptyList()
        val parts = LinkedList<CaramelPortalShape>()

        for (neighbourPos in getNeighbourPos(pos)) {
            val state = getter.getBlockState(neighbourPos)
            if (config.isEmpty(state)) {
                findPortal(getter, neighbourPos).ifPresent { parts.add(it) }
            }
        }
        return when {
            parts.isEmpty() -> emptyList()
            parts.size == 1 -> listOf(parts[0])
            else -> parts
        }
    }

    /**
     * @param pos 传送门内的任意一格
     */
    fun findPortal(getter: BlockGetter, pos: BlockPos): Optional<CaramelPortalShape> {
        if (!config.isEmpty.test(getter.getBlockState(pos))) return Optional.empty()
        val units: MutableList<UnitPortal> = ArrayList<UnitPortal>(3)

        for (axis in XZY) {
            if (axis !== Axis.Y || config.enableHorizontal) {
                val unit = findAxis(getter, pos, axis)
                if (unit != null) {
                    if (config.enableCompound) {
                        units.add(unit)
                    } else {
                        return Optional.of(unit)
                    }
                }
            }
        }
        return when {
            units.isEmpty() -> Optional.empty()
            units.size == 1 -> Optional.of(units[0])
            else -> Optional.of(CompoundPortal(units))
        }
    }

    private fun findAxis(level: BlockGetter, pos: BlockPos, axis: Axis): UnitPortal? {
        val pipe2: Array<Axis> = axis2pipe2(axis)
        val isHorizontal = axis === Axis.Y
        val exchangeable = config.horizontalLength == null && isHorizontal //只有传送门水平的时候才启用
        /*
         * exchangeable的思路是：先按对拿出最大值较大和较小的两组，
         * 因为findBound要的是limit，
         * 而为了把两个bound都拿出来，
         * 肯定要先用宽松的limit，
         * 所以先比较较大值，
         * M>m
         * 随后第一个bound拿出来了，
         * 如果此bound同时满足两个限制，
         * 则优先给这个这个bound赋予限制强的limit。
         * */
        var limitWM = config.horizontalLength.takeIf { isHorizontal }
            ?: IntIntPair.of(config.minWidth, config.maxWidth)
        var limitHm = config.horizontalLength.takeIf { isHorizontal }
            ?: IntIntPair.of(config.minHeight, config.maxHeight)

        if (exchangeable && limitWM.rightInt() < limitHm.rightInt()) {
            val i = limitHm
            limitHm = limitWM
            limitWM = i
        }
        //bound[正边界宽度,总宽度]
        //默认情况下(!exchangeable)
        //pipe2[1] = Y
        val boundW = findBound(level, pos, pipe2[0], config.isEmpty, config.isFrame, limitWM.rightInt())
        val boundH = findBound(
            level,
            pos,
            pipe2[1],
            config.isEmpty,
            config.isFrame,
            if (exchangeable) limitWM.rightInt() else limitHm.rightInt()
        )

        if (exchangeable) {
            var flag = false
            repeat(2) {
                //定双方都满足的limit
                if (boundW[1] >= limitWM.leftInt() && boundH[1] >= limitHm.leftInt()) flag = true
                //交换,继续
                val cg = limitHm
                limitHm = limitWM
                limitWM = cg
            }
            if (!flag) return null
        } else if (boundW[1] < limitWM.leftInt() || boundH[1] < limitHm.leftInt()) return null

        val delta1 = boundW[1] - boundW[0] - 1
        val delta2 = boundH[1] - boundH[0] - 1
        val bottomLeft = pos.relative(pipe2[0], -delta1)
            .relative(pipe2[1], -delta2)
        val sets = collectBlocks(level, bottomLeft, pipe2, boundW[1], boundH[1], config.isEmpty, config.isFrame)
        return if (sets == null) null else UnitPortal(
            bottomLeft,
            axis,
            boundW[1],
            boundH[1],
            sets[0],
            sets[1],
            sets[2]
        )
    }

    /**
     * <pre>
     * | = = = = = = = | : 总宽度 = 7
     * |       = = = = | : 正边界宽度 = 4
     * 8 7 6 0 1 2 3 4 5 : i(for),7号位是bottomLeft
     * | = = = = = = = |
     * ↑pos
    </pre> *
     *
     * @param limit 限制宽度
     * @return {正边界宽度，总宽度} 如果未找到则返回 {-1,-1}
     */
    private fun findBound(
        level: BlockGetter, pos: BlockPos, pipe: Axis, isEmpty: Predicate<BlockState>,
        isFrame: Predicate<BlockState>, limit: Int,
    ): IntArray {
        var pn = -1
        var direction: Direction = axis2direction(pipe, true)
        val mutable = pos.mutable()
        for (i in 0..limit) {
            val state = level.getBlockState(mutable)
            if (isEmpty.test(state)) {
                //skip
            } else if (isFrame.test(state)) {
                if (pn == -1) {
                    pn = i - 1
                    direction = direction.opposite
                    mutable.set(pos)
                } else {
                    return intArrayOf(pn, i - 1)
                }
            } else break
            mutable.move(direction)
        }
        return intArrayOf(-1, -1)
    }

    private fun collectBlocks(
        level: BlockGetter, bottomLeft: BlockPos, pipe2: Array<Axis>, length1: Int, length2: Int,
        isEmpty: Predicate<BlockState>, isFrame: Predicate<BlockState>,
    ): MutableList<MutableSet<BlockPos>>? {
        // | bw * * * * | length=5
        val sets: MutableList<MutableSet<BlockPos>> = MutableList(3, ::HashSet)

        val minW = bottomLeft.get(pipe2[0]) - 1
        val minH = bottomLeft.get(pipe2[1]) - 1
        val maxW = minW + length1 + 1
        val maxH = minH + length2 + 1
        val mutable = bottomLeft.mutable()
        for (w in minW..<maxW + 1) {
            mutable.at(pipe2[0], w)
            for (h in minH..<maxH + 1) {
                mutable.at(pipe2[1], h)
                var flag = 0
                if (w == minW || w == maxW) flag++
                if (h == minH || h == maxH) flag++
                //flag:   0      1      2
                //func: portal frame  extra
                val state = level.getBlockState(mutable)
                if ((if (flag == 0) isEmpty else isFrame).test(state)) {
                    sets[flag].add(mutable.immutable())
                } else if (flag == 2) {
                    //extra
                } else return null
            }
        }
        return sets
    }
}