package cn.jawbreakers.candycraftce.misc.multiblocks.caramel_portal

import cn.jawbreakers.candycraftce.utils.AxisSet
import cn.jawbreakers.candycraftce.utils.CLevelUtils.getNeighbourPos
import com.google.common.collect.Sets
import it.unimi.dsi.fastutil.ints.IntIntPair
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Direction.Axis
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import java.util.*
import java.util.function.Predicate

/**
 * Created in 2024/3/10 19:48
 * Project: candycraftce
 * 
 * Author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 *
 * Kotlinize in 2026/9/20 19:55 by Bread_NiceCat
 * 
 */
sealed class VectorPortalShape {
    /**
     * 框架内所有符合isEmpty的方块的集合
     */
    abstract val portals: Iterable<Pair<BlockPos, AxisSet>>

    /**
     * 所有有效的框架的集合，必须框架：构建一个传送门所必须的框架。如地狱门的四边
     */
    abstract val requiredFrames: Iterable<BlockPos>

    /**
     *  所有有效的可选框架的集合，可选框架：属于传送门结构，但是不必须。如地狱门的四角
     */
    abstract val optionalFrames: Iterable<BlockPos>

    /**
     *  所有框架的集合，包括必须框架和额外框架，
     */
    abstract val allFrames: Iterable<BlockPos>

    fun validate(level: Level, config: PortalConfig): Boolean {
        for (frame in this.allFrames) {
            if (!config.isFrame(level.getBlockState(frame))) return false
        }
        for ((portal, _) in this.portals) {
            if (!config.isEmpty(level.getBlockState(portal))) return false
        }
        return true
    }

    /**
     * 返回所有的单位传送门切片
     * */
    internal abstract val units: List<PortalUnit>
    abstract val config: PortalConfig

    /**
     * 构建传送门，这里传进来的传送门方块必须有Axis属性
     * 注意，构建前应当运行[validate]
     * @return true构建成功
     */
    fun build(level: Level, lighter: PortalLighter): Boolean {
        return lighter.light(level, this)
    }


    companion object {
        private val XZY: Array<Axis> = arrayOf(Axis.X, Axis.Z, Axis.Y)

        /**
         * 根据框架找
         * @param pos 任意一格有效框架
         */
        fun findPortalOnFrame(getter: BlockGetter, pos: BlockPos, config: PortalConfig): List<VectorPortalShape> {
            if (!config.isFrame(getter.getBlockState(pos))) return emptyList()
            val parts = LinkedList<VectorPortalShape>()

            for (neighbourPos in getNeighbourPos(pos)) {
                val state = getter.getBlockState(neighbourPos)
                if (config.isEmpty(state)) {
                    findPortal(getter, neighbourPos, config).ifPresent { parts.add(it) }
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
        fun findPortal(getter: BlockGetter, pos: BlockPos, config: PortalConfig): Optional<VectorPortalShape> {
            if (!config.isEmpty.test(getter.getBlockState(pos))) return Optional.empty()
            val units: MutableList<PortalUnit> = ArrayList<PortalUnit>(3)

            for (axis in XZY) {
                if (axis !== Axis.Y || config.enableHorizontal) {
                    val unit = findAxis(getter, pos, axis, config)
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
                else -> Optional.of(Compound(config, units))
            }
        }

        private fun findAxis(level: BlockGetter, pos: BlockPos, axis: Axis, config: PortalConfig): PortalUnit? {
            val pipe2: Array<Axis> = axis2pipe2(axis)
            val exchangeable = axis === Axis.Y //只有传送门水平的时候才启用
            /*
		 * exchangeable的思路是：
		 * 先按对拿出最大值较大和较小的两组，
		 * 因为findBound要的是limit，
		 * 而为了把两个bound都拿出来，
		 * 肯定要先用宽松的limit，
		 * 所以先比较较大值，
		 * M>m
		 * 随后第一个bound拿出来了，
		 * 如果此bound同时满足两个限制，
		 * 则优先给这个这个bound赋予限制强的limit。
		 *
		 * */
            var limitWM = IntIntPair.of(config.minWidth, config.maxWidth)
            var limitHm = IntIntPair.of(config.minHeight, config.maxHeight)

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
            return if (sets == null) null else PortalUnit(
                bottomLeft,
                axis,
                boundW[1],
                boundH[1],
                config,
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
}

fun axis2pipe2(axis: Axis): Array<Axis> {
    return when (axis) {
        Axis.X -> arrayOf(Axis.Z, Axis.Y)
        Axis.Z -> arrayOf(Axis.X, Axis.Y)
        Axis.Y -> arrayOf(Axis.X, Axis.Z)
    }
}

fun axis2direction(axis: Axis, positive: Boolean): Direction {
    return Direction.get(
        if (positive) Direction.AxisDirection.POSITIVE else Direction.AxisDirection.NEGATIVE,
        axis
    )
}


fun BlockPos.MutableBlockPos.at(axis: Axis, value: Int): BlockPos.MutableBlockPos {
    return when (axis) {
        Axis.X -> setX(value)
        Axis.Y -> setY(value)
        Axis.Z -> setZ(value)
    }
}

/**
 * 这里的width和height都是泛义的
 * bottomLeft指框架内符合isEmpty中pipe2的和最小的一个pos
 * width指bottomLeft的pipe2[0] + width +1 抵到框架
 * height指bottomLeft的pipe2[1] + height +1 抵到框架
 */
internal class PortalUnit(
    val bottomLeft: BlockPos,
    val axis: Axis,
    val width: Int, val height: Int,
    override val config: PortalConfig,
    portals: Set<BlockPos>,
    required: Set<BlockPos>,
    optional: Set<BlockPos>,
) : VectorPortalShape() {
    val axes: AxisSet = AxisSet(axis)

    //对应到[width,height]
//    val pipe2: Array<Axis> = axis2pipe2(axis)

    override val allFrames: Iterable<BlockPos> = Sets.union<BlockPos>(required, optional)
    override val portals: Iterable<Pair<BlockPos, AxisSet>> = portals.map { it to axes }
    override val requiredFrames: Iterable<BlockPos> = required
    override val optionalFrames: Iterable<BlockPos> = optional
    override val units: List<PortalUnit> = listOf(this)

    override fun toString(): String {
        return """{"type":"unit","bottom_left":{"x":%d,"y":%d,"z":%d},"axis":"%s","width":%d,"height":%d}""".trimIndent()
            .format(bottomLeft.x, bottomLeft.y, bottomLeft.z, axis, width, height)
    }

//    val topRight: BlockPos = bottomLeft.relative(pipe2[0], width - 1).relative(pipe2[1], height - 1)
}


private class Compound(
    override val config: PortalConfig,
    private val parts: MutableList<out VectorPortalShape>,
) : VectorPortalShape() {
    private val portal: MutableMap<BlockPos, AxisSet.Mutable> = mutableMapOf()
    private val required: MutableSet<BlockPos> = mutableSetOf()
    private val optional: MutableSet<BlockPos> = mutableSetOf()

    override val portals: Iterable<Pair<BlockPos, AxisSet>> get() = portal.map { it.key to it.value.immutable() }
    override val requiredFrames: Iterable<BlockPos> get() = required
    override val optionalFrames: Iterable<BlockPos> get() = optional

    init {
        for (part in parts) {
            for ((pos, axes) in part.portals) {
                val mutable = portal[pos]
                if (mutable == null) {
                    portal[pos] = axes.mutable() //如果表里面没有就加进去
                } else {
                    mutable.withOr(axes)//对象值已经存在表里面，直接改就行
                }
            }
            part.requiredFrames.forEach(required::add)
            part.optionalFrames.forEach(optional::add)
        }
    }

    override val allFrames: Iterable<BlockPos> = Sets.union<BlockPos>(required, optional)
    override val units: List<PortalUnit> = parts.flatMap { it.units }
    override fun toString(): String {
        return """{"type": "compound", "parts": %s}""".format(parts)
    }
}