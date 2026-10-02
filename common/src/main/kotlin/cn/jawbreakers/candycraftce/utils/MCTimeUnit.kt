package cn.jawbreakers.candycraftce.utils

typealias Ticks = Int
typealias Seconds = Float

val Int.second: Seconds get() = this.toFloat()
val Float.second: Seconds get() = this
val Int.tick: Ticks get() = this
fun Seconds.toTicks(): Ticks = (this * 20).toInt()
fun Ticks.toSeconds(): Seconds = this / 20f
