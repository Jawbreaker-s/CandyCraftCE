package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.mob_effect.CloyingMobEffect
import cn.jawbreakers.candycraftce.mob_effect.PropolisMobEffect
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.world.effect.MobEffect
import java.util.function.Supplier

/**
 * Created in 2026/10/3 09:59 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
object CMobEffects {
    init {
        CLogUtils.sign()
    }

    val cloying = register("cloying", ::CloyingMobEffect)
    val propolis = register("propolis", ::PropolisMobEffect)


    fun <M : MobEffect> register(name: String, factory: Supplier<M>): Entry<M> {
        return CPlatformUtils.registerMobEffect(name, factory)
    }

}