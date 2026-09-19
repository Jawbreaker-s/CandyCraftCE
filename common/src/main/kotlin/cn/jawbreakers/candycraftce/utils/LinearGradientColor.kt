package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.utils.LinearGradient.Companion.rgb
import net.minecraft.util.FastColor
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.awt.Color
import java.util.*

/**
 * Created by NiceCat on 2026/1/18.
 * Project: candycraftce
 * @author <a href="https://github.com/BreadNiceCat">Bread_NiceCat</a>
 * example
 * <pre>
 *  LinearGradient {
 *      0xb0ecff.rgb % 0f
 *      0xa376da.rgb % .5f  //50%时是该颜色
 *      0xb0b0ff.rgb % 1f
 *  }
 * </pre>
 */

class LinearGradient(vararg colors: LinearGradientColor) {
    companion object {
        val String.rgb
            get() = run {
                require(startsWith("#") || startsWith("0x")) { "Invalid color format" }
                Color(Integer.decode(this))
            }
        val Color.normal get() = Vec3(red / 255.0, green / 255.0, blue / 255.0)
        val Int.vecColor: Vector3f
            get() = Vector3f(
                ((this shr 16) and 0xFF) / 255.0f,
                ((this shr 8) and 0xFF) / 255.0f,
                (this and 0xFF) / 255.0f
            )
        val Vec3.rgb get() = Color((x * 255).toInt(), (y * 255).toInt(), (z * 255).toInt())

        val Int.alpha get() = FastColor.ARGB32.alpha(this)
        val Int.red get() = FastColor.ARGB32.red(this)
        val Int.green get() = FastColor.ARGB32.green(this)
        val Int.blue get() = FastColor.ARGB32.blue(this)
        fun rgb(red: Int, green: Int, blue: Int) = FastColor.ARGB32.color(0xff, red, green, blue)
    }

    constructor(action: GradientScope.() -> Unit) : this(*GradientScope().apply(action).get())

    private val colors: TreeMap<Float, LinearGradientColor> = TreeMap(colors.associateBy { it.ratio })

    init {
        require(this.colors.size >= 2) { "At least two colors are required." }
        for (entry in this.colors.values) {
            require(entry.ratio in 0f..1f) { "Ratio must be between 0 and 1." }
        }
    }

    fun getColor(ratio: Float): LinearGradientColor {
        if (ratio in colors) {
            return colors[ratio]!!
        } else if (ratio < colors.firstKey()) {
            val e = colors.firstEntry()
            return LinearGradientColor(e.value.rgb, e.key)
        } else if (ratio > colors.lastKey()) {
            val e = colors.lastEntry()
            return LinearGradientColor(e.value.rgb, e.key)
        } else {
            //前前一个
            var prev1 = colors.firstEntry()
            //前一个
            var prev2 = colors.firstEntry()
            for (entry in colors.entries) {
                prev1 = prev2
                prev2 = entry
                if (entry.key > ratio) {
                    break
                }
            }
            val r = (ratio - prev1.key) / (prev2.key - prev1.key)
            val c1 = prev1.value
            val c2 = prev2.value
            return LinearGradientColor(
                (c1.red + (c2.red - c1.red) * r).toInt(),
                (c1.green + (c2.green - c1.green) * r).toInt(),
                (c1.blue + (c2.blue - c1.blue) * r).toInt(),
                r
            )
        }
    }
}

class GradientScope internal constructor() {
    private val colors = LinkedList<LinearGradientColor>()
    operator fun Color.rem(ratio: Number) {
        colors.add(LinearGradientColor(this.rgb, ratio.toFloat()))
    }

    operator fun Vec3.rem(ratio: Number) {
        colors.add(LinearGradientColor(this.rgb.rgb, ratio.toFloat()))
    }

    operator fun String.rem(ratio: Number) {
        this.rgb % ratio
    }

    fun get(): Array<LinearGradientColor> = colors.toTypedArray()
}

class LinearGradientColor private constructor(
    val rgb: Int,
    val red: Int, val green: Int, val blue: Int,
    val ratio: Float,
) {
    constructor(rgb: Int, ratio: Float) : this(
        rgb,
        rgb ushr 16 and 0xff,
        rgb ushr 8 and 0xff,
        rgb and 0xff,
        ratio
    )

    constructor(red: Int, green: Int, blue: Int, ratio: Float) : this(
        (red shl 16) or (green shl 8) or blue,
        red,
        green,
        blue,
        ratio
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LinearGradientColor) return false
        return rgb == other.rgb && ratio == other.ratio
    }

    override fun hashCode(): Int = ratio.hashCode()

    val normRed by lazy { red / 255.0f }
    val normGreen by lazy { green / 255.0f }
    val normBlue by lazy { blue / 255.0f }
    val normal by lazy { Vec3(normRed.toDouble(), normGreen.toDouble(), normBlue.toDouble()) }
}
