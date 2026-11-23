package cn.jawbreakers.candycraftce.client.particles

import cn.jawbreakers.candycraftce.utils.ClientOnly
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.particle.TextureSheetParticle
import net.minecraft.core.particles.SimpleParticleType

@ClientOnly
class ChocolateSplashParticle private constructor(
    level: ClientLevel, x: Double, y: Double, z: Double,
    xSpeed: Double, ySpeed: Double, zSpeed: Double,
    private val sprites: SpriteSet,
) : TextureSheetParticle(level, x, y, z, xSpeed, ySpeed, zSpeed) {
    init {
        this.xd = xSpeed
        this.yd = ySpeed
        this.zd = zSpeed
        this.gravity = 0.06f
        this.friction = 0.92f
        this.lifetime = 6 + this.random.nextInt(4)
        this.quadSize = 0.045f + this.random.nextFloat() * 0.035f
        this.setColor(0.34f, 0.16f, 0.055f)
        this.setAlpha(0.88f)
        this.setSpriteFromAge(sprites)
    }

    override fun tick() {
        super.tick()
        this.setSpriteFromAge(this.sprites)
    }

    override fun getRenderType(): ParticleRenderType {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
    }

    class Provider(private val sprites: SpriteSet) : ParticleProvider<SimpleParticleType> {
        override fun createParticle(
            type: SimpleParticleType, level: ClientLevel, x: Double, y: Double, z: Double,
            xSpeed: Double, ySpeed: Double, zSpeed: Double,
        ) = ChocolateSplashParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites)
    }
}
