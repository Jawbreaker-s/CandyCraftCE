package cn.jawbreakers.candycraftce.utils

sealed interface MCTimeUnit {
    val toTick: Int
    val toSecond: Float
    val toMillis: Long

    companion object {
        val Int.tick get() = Tick(this)
        val Float.second get() = Second(this)
        val Int.second get() = Second(this.toFloat())
        val Long.ms get() = Millis(this)
        val Int.ms get() = Millis(this.toLong())

        val tps = 1.second.toTick
    }

    class Tick(override val toTick: Int) : MCTimeUnit {
        override val toSecond by lazy { this.toTick / 20f }
        override val toMillis by lazy { this.toTick * 50L }
    }


    class Second(override val toSecond: Float) : MCTimeUnit {
        override val toTick by lazy { (this.toSecond * 20).toInt() }
        override val toMillis by lazy { (this.toSecond * 1000).toLong() }
    }

    class Millis(override val toMillis: Long) : MCTimeUnit {
        override val toTick by lazy { (this.toMillis / 50).toInt() }
        override val toSecond by lazy { toMillis / 1000f }

    }

}