package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.client.particles.*
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.client.particle.FlameParticle
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.particles.SimpleParticleType
import java.util.function.Supplier

/**
 * Created in 2024/4/5 下午5:50
 * Project: candycraftce
 *
 * Author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 *
 *
 */
object CParticleTypes {
    init {
        CLogUtils.sign()
    }

    val caramel_portal_particle_type = register("caramel_portal_particle", ::SimpleLimited)
    val milk_rain_splash = register("milk_rain_splash", ::SimpleLimited)
    val milk_rain_drop = register("milk_rain_drop", ::SimpleLimited)
    val chocolate_splash = register("chocolate_splash", ::SimpleLimited)
    val alchemy_splash = register("alchemy_splash", ::SimpleLimited)
    val liquid_candy_flame = register("liquid_candy_flame", ::SimpleLimited)

    //jelly
    val strawberry_jelly_fragment = register("strawberry_jelly_fragment", ::SimpleLimited)
    val caramel_jelly_fragment = register("caramel_jelly_fragment", ::SimpleLimited)
    val royal_rations_fragment = register("royal_rations_fragment", ::SimpleLimited)
    val lemon_jelly_fragment = register("lemon_jelly_fragment", ::SimpleLimited)
    val raspberry_jelly_fragment = register("raspberry_jelly_fragment", ::SimpleLimited)
    val mint_jelly_fragment = register("mint_jelly_fragment", ::SimpleLimited)

    class SimpleLimited internal constructor() : SimpleParticleType(false)

    private fun <P : ParticleType<*>> register(id: String, type: Supplier<P>): Entry<P> {
        return CPlatformUtils.registerParticleType(id, type)
    }

    init {
        ifClient {
            registerParticleFactory(caramel_portal_particle_type, CaramelPortalParticle::Provider)
            registerParticleFactory(chocolate_splash, ChocolateSplashParticle::Provider)
            registerParticleFactory(milk_rain_drop, MilkRainDropParticle::Provider)
            registerParticleFactory(milk_rain_splash, MilkRainSplashParticle::Provider)
//                registerParticleFactory(alchemy_splash, AlchemySplashParticle::Provider)
            //TODO
            registerParticleFactory(alchemy_splash, CaramelPortalParticle::Provider)
            registerParticleFactory(liquid_candy_flame, FlameParticle::Provider);

            registerParticleFactory(strawberry_jelly_fragment) {
                JellyFragmentParticle.Provider(it, 1.0F, 0.47F, 0.63F)
            }
            registerParticleFactory(caramel_jelly_fragment) {
                JellyFragmentParticle.Provider(it, 0.93F, 0.43F, 0.08F)
            }
            registerParticleFactory(royal_rations_fragment) {
                JellyFragmentParticle.Provider(it, 0.72F, 0.77F, 0.84F)
            }
            registerParticleFactory(lemon_jelly_fragment) {
                JellyFragmentParticle.Provider(it, 0.85F, 0.86F, 0.40F)
            }
            registerParticleFactory(raspberry_jelly_fragment) {
                JellyFragmentParticle.Provider(it, 0.92F, 0.37F, 0.30F)
            }
            registerParticleFactory(mint_jelly_fragment) {
                JellyFragmentParticle.Provider(it, 0.54F, 0.90F, 0.80F)
            }
        }
    }

}