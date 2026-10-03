package cn.jawbreakers.candycraftce.client.particles

import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.particle.TextureSheetParticle
import net.minecraft.core.particles.SimpleParticleType

class MilkRainDropParticle private constructor(
    level: ClientLevel, x: Double, y: Double, z: Double,
    xSpeed: Double, ySpeed: Double, zSpeed: Double,
    sprites: SpriteSet,
) : TextureSheetParticle(level, x, y, z, xSpeed, ySpeed, zSpeed) {
    init {
        this.xd = xSpeed * 0.15
        this.yd = -0.65 - level.random.nextDouble() * 0.25
        this.zd = zSpeed * 0.15
        this.gravity = 0.35f
        this.friction = 0.98f
        this.lifetime = 18 + level.random.nextInt(8)
        this.quadSize = 0.055f
        this.setColor(1.0f, 1.0f, 1.0f)
        this.setAlpha(1.0f)
        pickSprite(sprites)
        this.hasPhysics = true
    }

    public override fun getLightColor(partialTick: Float): Int {
        return 0xF000F0
    }

    override fun getRenderType(): ParticleRenderType {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
    }

    class Provider(private val sprites: SpriteSet) : ParticleProvider<SimpleParticleType> {
        override fun createParticle(
            type: SimpleParticleType, level: ClientLevel, x: Double, y: Double, z: Double,
            xSpeed: Double, ySpeed: Double, zSpeed: Double,
        ) = MilkRainDropParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites)
    }
}
