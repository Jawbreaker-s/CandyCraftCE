package cn.jawbreakers.candycraftce.client.particles

import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.*
import net.minecraft.core.particles.SimpleParticleType
import kotlin.math.max

class JellyFragmentParticle private constructor(
    level: ClientLevel, x: Double, y: Double, z: Double,
    xSpeed: Double, ySpeed: Double, zSpeed: Double,
    private val sprites: SpriteSet,
    red: Float, green: Float, blue: Float,
) : TextureSheetParticle(level, x, y, z, xSpeed, ySpeed, zSpeed) {
    private val baseAlpha: Float

    init {
        this.xd = xSpeed
        this.yd = ySpeed
        this.zd = zSpeed
        this.gravity = 0.7f
        this.friction = 0.84f
        this.lifetime = 14 + random.nextInt(9)
        this.quadSize = 0.075f + random.nextFloat() * 0.055f
        this.baseAlpha = 0.68f
        setColor(red, green, blue)
        setAlpha(baseAlpha)
        setSpriteFromAge(sprites)
    }

    override fun tick() {
        super.tick()
        setSpriteFromAge(sprites)
        val fadeStart = lifetime * 0.55f
        if (age > fadeStart) {
            setAlpha(baseAlpha * max(0.0f, (lifetime - age) / (lifetime - fadeStart)))
        }
    }

    override fun getRenderType(): ParticleRenderType {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
    }

    class Provider(
        private val sprites: SpriteSet,
        private val red: Float,
        private val green: Float,
        private val blue: Float,
    ) : ParticleProvider<SimpleParticleType> {
        override fun createParticle(
            type: SimpleParticleType, level: ClientLevel, x: Double, y: Double, z: Double,
            xSpeed: Double, ySpeed: Double, zSpeed: Double,
        ): Particle {
            return JellyFragmentParticle(
                level, x, y, z, xSpeed, ySpeed, zSpeed,
                sprites, red, green, blue
            )
        }
    }
}
