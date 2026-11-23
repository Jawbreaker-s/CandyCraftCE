package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.client.particles.CaramelPortalParticle
import cn.jawbreakers.candycraftce.client.particles.ChocolateSplashParticle
import cn.jawbreakers.candycraftce.client.particles.MilkRainDropParticle
import cn.jawbreakers.candycraftce.client.particles.MilkRainSplashParticle
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

    val caramel_portal_particle_type = register("caramel_portal_particle", ::simple)
    val milk_rain_splash = register("milk_rain_splash", ::simple)
    val milk_rain_drop = register("milk_rain_drop", ::simple)
    val chocolate_splash = register("chocolate_splash", ::simple)
    val alchemy_splash = register("alchemy_splash", ::simple)
    val liquid_candy_flame = register("liquid_candy_flame", ::simple)
    fun simple() = object : SimpleParticleType(false) {}

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
        }
    }

}