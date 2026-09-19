package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.level.foliage_placer.CandiedCherryFoliagePlacer
import cn.jawbreakers.candycraftce.level.foliage_placer.EnchantFoliagePlacer
import cn.jawbreakers.candycraftce.level.foliage_placer.FancyCaramelFoliagePlacer
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.levels

object CFoliagePlacers {
    init {
        CLogUtils.sign()
    }

    val candied_cherry = levels.registerFoliagePlacer("cherry") { CandiedCherryFoliagePlacer.codec }
    val fancy_caramel = levels.registerFoliagePlacer("fancy_caramel") { FancyCaramelFoliagePlacer.codec }
    val enchant = levels.registerFoliagePlacer("enchant") { EnchantFoliagePlacer.codec }
}