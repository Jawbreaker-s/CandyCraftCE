package cn.jawbreakers.candycraftce.utils

typealias Ticks = Int
typealias Seconds = Float

object TickUnit {
    const val SEC2TICK = 20
    const val TICK2SEC = 1f / 20f
    val Int.second: Ticks get() = this * SEC2TICK
    val Float.second: Ticks get() = (this * SEC2TICK).toInt()
    val Int.tick: Ticks get() = this
}