//package cn.jawbreakers.candycraftce.client.particles
//
//import cn.jawbreakers.candycraftce.block.entity.AlchemyLiquidKind
//import cn.jawbreakers.candycraftce.block.entity.AlchemyTableBlockEntity
//import cn.jawbreakers.candycraftce.block.entity.AlchemyTableBlockEntityimport
//import net.minecraft.client.multiplayer.ClientLevel net.minecraft.client.multiplayer.ClientLevel
//import net.minecraft.client.particle.*
//import net.minecraft.core.BlockPos
//import net.minecraft.core.particles.SimpleParticleType
//import kotlin.math.max
//
//class AlchemySplashParticle private constructor(
//    level: ClientLevel, x: Double, y: Double, z: Double,
//    xSpeed: Double, ySpeed: Double, zSpeed: Double, private val sprites: SpriteSet
//) : TextureSheetParticle(level, x, y, z, xSpeed, ySpeed, zSpeed) {
//    private val liquidKind: AlchemyLiquidKind
//    private val translucent: Boolean
//    private val baseAlpha: Float
//
//    init {
//        this.xd = xSpeed
//        this.yd = ySpeed
//        this.zd = zSpeed
//        this.gravity = 0.09f
//        this.friction = 0.88f
//        this.lifetime = 7 + level.random.nextInt(5)
//        // Match the vanilla underwater suspended particle's texture and size,
//        // while retaining gravity so this still behaves as a liquid splash.
//        this.quadSize = 0.12f * (level.random.nextFloat() * 0.6f + 0.25f)
//        this.liquidKind = kindAt(level, BlockPos.containing(x, y, z))
//        this.translucent = isTranslucent(liquidKind)
//        this.setSprite(sprites.get(level.random))
//        val color: FloatArray = color(liquidKind)
//        this.setColor(color[0], color[1], color[2])
//        this.baseAlpha = if (translucent) 0.72f else 1.0f
//        this.setAlpha(this.baseAlpha)
//    }
//
//    override fun tick() {
//        super.tick()
//        if (!removed) {
//            this.setSpriteFromAge(this.sprites)
//            // Fade out over the last 40% of the lifetime so droplets dissolve
//            // instead of popping out of existence.
//            val fade = 1.0f - max(
//                0.0f,
//                (this.age - this.lifetime * 0.6f) / (this.lifetime * 0.4f)
//            )
//            this.setAlpha(this.baseAlpha * fade)
//        }
//        if (this.onGround) {
//            this.remove()
//        }
//    }
//
//    override fun getRenderType(): ParticleRenderType {
//        return if (translucent)
//            ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
//        else
//            ParticleRenderType.PARTICLE_SHEET_OPAQUE
//    }
//
//    class Provider(private val sprites: SpriteSet) : ParticleProvider<SimpleParticleType> {
//        override fun createParticle(
//            type: SimpleParticleType, level: ClientLevel, x: Double, y: Double, z: Double,
//            xSpeed: Double, ySpeed: Double, zSpeed: Double
//        ): Particle {
//            return AlchemySplashParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites)
//        }
//    }
//
//    companion object {
//        private fun kindAt(level: ClientLevel, pos: BlockPos): AlchemyLiquidKind {
//            for (candidate in BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 0, 1))) {
//                val table = level.getBlockEntity(candidate)
//        if (table is AlchemyTableBlockEntity) {
//                    return table.getLiquidKind()
//                }
//            }
//            return AlchemyLiquidKind.WATER
//        }
//
//        private fun isTranslucent(kind: AlchemyLiquidKind): Boolean {
//            return when (kind) {
//                AlchemyLiquidKind.GRENADINE, WATER, CARAMEL -> true
//                else -> false
//            }
//        }
//
//        private fun color(kind: AlchemyLiquidKind): FloatArray {
//            val base = when (kind) {
//                AlchemyLiquidKind.GRENADINE -> floatArrayOf(0.95f, 0.16f, 0.15f)
//                WATER -> floatArrayOf(0.25f, 0.55f, 1.0f)
//                AlchemyLiquidKind.MILK -> floatArrayOf(0.95f, 0.95f, 0.88f)
//                CHOCOLATE -> floatArrayOf(0.45f, 0.20f, 0.08f)
//                AlchemyLiquidKind.LIQUID_CANDY -> floatArrayOf(0.90f, 0.40f, 0.77f)
//                LAVA -> floatArrayOf(1.0f, 0.32f, 0.02f)
//                CARAMEL -> floatArrayOf(0.92f, 0.46f, 0.10f)
//                NONE -> floatArrayOf(1.0f, 1.0f, 1.0f)
//            }
//            // Splash droplets read better a bit lighter than the liquid surface.
//            for (i in 0..2) {
//                base[i] += (1.0f - base[i]) * 0.35f
//            }
//            return base
//        }
//    }
//}
