package cn.jawbreakers.candycraftce.registry.worldgen

import cn.jawbreakers.candycraftce.level.foliage_placer.CandiedCherryFoliagePlacer
import cn.jawbreakers.candycraftce.level.foliage_placer.EnchantFoliagePlacer
import cn.jawbreakers.candycraftce.level.foliage_placer.FancyCaramelFoliagePlacer
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import com.mojang.serialization.Codec
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
import java.util.function.Supplier

object CFoliagePlacers {
    init {
        CLogUtils.sign()
    }

    val candied_cherry = register("cherry") { CandiedCherryFoliagePlacer.codec }
    val fancy_caramel = register("fancy_caramel") { FancyCaramelFoliagePlacer.codec }
    val enchant = register("enchant") { EnchantFoliagePlacer.codec }

    private fun <F : FoliagePlacer> register(name: String, factory: Supplier<Codec<F>>) =
        CPlatformUtils.levels.registerFoliagePlacer(name, factory)
}