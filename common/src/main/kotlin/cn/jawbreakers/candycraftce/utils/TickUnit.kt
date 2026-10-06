package cn.jawbreakers.candycraftce.utils

typealias Ticks = Int
typealias Seconds = Float

object TickUnit {
    val Int.second: Ticks get() = this * 20
    val Float.second: Ticks get() = (this * 20).toInt()
    val Int.tick: Ticks get() = this
}