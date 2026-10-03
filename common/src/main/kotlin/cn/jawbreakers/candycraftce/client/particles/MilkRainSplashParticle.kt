package cn.jawbreakers.candycraftce.client.particles

import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.particle.TextureSheetParticle
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.SimpleParticleType
import kotlin.math.max

class MilkRainSplashParticle private constructor(
    level: ClientLevel, x: Double, y: Double, z: Double,
    xSpeed: Double, ySpeed: Double, zSpeed: Double,
    sprites: SpriteSet,
) : TextureSheetParticle(level, x, y, z, xSpeed, ySpeed, zSpeed) {
    init {
        this.xd *= 0.3
        this.yd = Math.random() * 0.2 + 0.1
        this.zd *= 0.3
        this.setSize(0.01f, 0.01f)
        this.gravity = 0.06f
        this.lifetime = (8.0 / (Math.random() * 0.8 + 0.2)).toInt()
        val color: FloatArray = if (level.random.nextBoolean()) CHOCOLATE else CREAM
        this.setColor(color[0], color[1], color[2])
        pickSprite(sprites)
    }

    override fun tick() {
        this.xo = this.x
        this.yo = this.y
        this.zo = this.z
        if (this.lifetime-- <= 0) {
            this.remove()
            return
        }

        this.yd -= this.gravity.toDouble()
        this.move(this.xd, this.yd, this.zd)
        this.xd *= 0.98
        this.yd *= 0.98
        this.zd *= 0.98
        if (this.onGround) {
            if (Math.random() < 0.5) {
                this.remove()
            }
            this.xd *= 0.7
            this.zd *= 0.7
        }

        val pos = BlockPos.containing(this.x, this.y, this.z)
        val state = this.level.getBlockState(pos)
        val fluid = this.level.getFluidState(pos)
        val surfaceHeight = max(
            state.getCollisionShape(this.level, pos).max(
                Direction.Axis.Y,
                this.x - pos.x, this.z - pos.z
            ),
            fluid.getHeight(this.level, pos).toDouble()
        )
        if (surfaceHeight > 0.0 && this.y < pos.y + surfaceHeight) {
            this.remove()
        }
    }

    override fun getRenderType(): ParticleRenderType {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE
    }

    class Provider(private val sprites: SpriteSet) : ParticleProvider<SimpleParticleType> {
        override fun createParticle(
            type: SimpleParticleType, level: ClientLevel, x: Double, y: Double, z: Double,
            xSpeed: Double, ySpeed: Double, zSpeed: Double,
        ) = MilkRainSplashParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites)
    }

    companion object {
        private val CHOCOLATE = floatArrayOf(0.612f, 0.357f, 0.235f)
        private val CREAM = floatArrayOf(1.0f, 0.878f, 0.639f)
    }
}
