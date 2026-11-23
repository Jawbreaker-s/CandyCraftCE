package cn.jawbreakers.candycraftce.misc.multiblocks.caramel_portal

import cn.jawbreakers.candycraftce.utils.AxisSet
import com.google.common.collect.Sets
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Direction.Axis
import net.minecraft.world.level.Level

/**
 * Created in 2024/3/10 19:48
 * Project: candycraftce
 * 
 * Author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 *
 * Kotlinize in 2026/9/20 19:55 by Bread_NiceCat
 * 
 */
sealed class CaramelPortalShape {
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

    fun validate(level: Level, config: PortalConfig, ignoreOptional: Boolean = false): Boolean {
        for (frame in this.allFrames) {
            if (!config.isFrame(level.getBlockState(frame))) return false
        }
        for ((portal, _) in this.portals) {
            if (!config.isEmpty(level.getBlockState(portal))) return false
        }
        if (!ignoreOptional) for (frame in this.optionalFrames) {
            if (!config.isFrame(level.getBlockState(frame))) return false
        }

        return true
    }

    /**
     * 返回所有的单位传送门切片
     * */
    internal abstract val units: List<UnitPortal>

    /**
     * 构建传送门，这里传进来的传送门方块必须有Axis属性
     * 注意，构建前应当运行[validate]
     * @return true构建成功
     */
    fun build(level: Level, igniter: PortalIgniter): Boolean {
        return igniter.ignite(level, this)
    }
}

/**
 * 这里的width和height都是泛义的
 * bottomLeft指框架内符合isEmpty中pipe2的和最小的一个pos
 * width指bottomLeft的pipe2[0] + width +1 抵到框架
 * height指bottomLeft的pipe2[1] + height +1 抵到框架
 */
internal class UnitPortal(
    val bottomLeft: BlockPos,
    val axis: Axis,
    val width: Int, val height: Int,
    portals: Set<BlockPos>,
    required: Set<BlockPos>,
    optional: Set<BlockPos>,
) : CaramelPortalShape() {
    val axes: AxisSet = AxisSet(axis)

    //对应到[width,height]
    val pipe2: Array<Axis> = axis2pipe2(axis)

    val topRight: BlockPos = bottomLeft
        .relative(pipe2[0], width - 1)
        .relative(pipe2[1], height - 1)

    override val allFrames: Iterable<BlockPos> = Sets.union<BlockPos>(required, optional)

    override val portals: Iterable<Pair<BlockPos, AxisSet>> = portals.map { it to axes }
    override val requiredFrames: Iterable<BlockPos> = required
    override val optionalFrames: Iterable<BlockPos> = optional
    override val units: List<UnitPortal> = listOf(this)
    override fun toString(): String {
        return """{"type":"unit","bottom_left":{"x":%d,"y":%d,"z":%d},"top_right":{"x":%d,"y":%d,"z":%d},"axis":"%s","width":%d,"height":%d}""".trimIndent()
            .format(bottomLeft.x, bottomLeft.y, bottomLeft.z, topRight.x, topRight.y, topRight.z, axis, width, height)
    }

//    val topRight: BlockPos = bottomLeft.relative(pipe2[0], width - 1).relative(pipe2[1], height - 1)

}

internal class CompoundPortal(
    private val parts: MutableList<out CaramelPortalShape>,
) : CaramelPortalShape() {
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

    override val units: List<UnitPortal> = parts.flatMap { it.units }
    override fun toString(): String {
        return """{"type": "compound", "parts": %s}""".format(parts)
    }
}

fun interface PortalIgniter {
    fun ignite(level: Level, portal: CaramelPortalShape): Boolean
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
