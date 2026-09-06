package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.client.level.CandyEffects
import cn.jawbreakers.candycraftce.client.level.DungeonEffects
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc

object CLevels {
    init {
        CLogUtils.sign()
    }

    val dungeon_effects = "dungeon_effects".modLoc()
    val candy_effects = "candy_effects".modLoc()

    fun initClient() {
        ifClient {
            CPlatformUtils.registerDimensionSpecialEffects(dungeon_effects, DungeonEffects)
            CPlatformUtils.registerDimensionSpecialEffects(candy_effects, CandyEffects)
        }
    }
}