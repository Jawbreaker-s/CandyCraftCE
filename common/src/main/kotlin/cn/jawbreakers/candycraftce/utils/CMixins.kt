package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.mixin.item.AxeItemAccessor
import net.minecraft.world.level.block.Block

object CMixins {
    fun addStrippables(log: Block, stripped: Block) {
        var map = AxeItemAccessor.getStrippedBlocks()
        if (map !is HashMap) {
            map = HashMap(map)
            AxeItemAccessor.setStrippedBlocks(map)
        }
        map[log] = stripped
    }
}