package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CItems

/**
 * Created in 2026/9/25 14:14 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class DragibusCropsBlock(properties: Properties) :
    CandyCropBlock(properties, shapes, { CItems.dragibus.get() }, true, ::stage) {
    companion object {
        private val shapes = arrayOf(
            box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),  //0
            box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),  //3
            box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),  //5
            box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),  //7
        )

        fun stage(age: Int): Int = when (age) {
            0, 1, 2 -> 0
            3, 4 -> 1
            5, 6 -> 2
            else -> 3
        }
    }
}