package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.client.particles.CaramelPortalType
import cn.jawbreakers.candycraftce.client.particles.CaramelProvider
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.core.particles.ParticleType
import java.util.function.Supplier

/**
 * Created in 2024/4/5 下午5:50
 * Project: candycraftce
 *
 * Author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 *
 *
 */
object CParticles {
    init {
        CLogUtils.sign()
    }

    var caramel_portal_particle_type = register("caramel_portal_particle", ::CaramelPortalType)

    private fun <P : ParticleType<*>> register(id: String, type: Supplier<P>): Entry<P> {
        return CPlatformUtils.registerParticleType(id, type)
    }

    init {
        ifClient {
            CPlatformUtils.clients?.apply {
                registerParticleFactory(caramel_portal_particle_type, ::CaramelProvider)
            }
        }
    }

}