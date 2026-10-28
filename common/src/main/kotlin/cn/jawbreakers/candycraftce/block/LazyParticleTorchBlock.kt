package cn.jawbreakers.candycraftce.block

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.TorchBlock
import net.minecraft.world.level.block.WallTorchBlock
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Supplier

class LazyParticleTorchBlock(properties: Properties, private val particleSupplier: Supplier<out ParticleOptions>) :
    TorchBlock(properties, ParticleTypes.FLAME) {
    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        val x = pos.x + 0.5
        val y = pos.y + 0.7
        val z = pos.z + 0.5
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0)
        level.addParticle(particleSupplier.get(), x, y, z, 0.0, 0.0, 0.0)
    }

    class Wall(properties: Properties, private val particleSupplier: Supplier<out ParticleOptions?>) :
        WallTorchBlock(properties, ParticleTypes.FLAME) {
        override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
            val direction = state.getValue(FACING).opposite
            val x = pos.x + 0.5 + 0.27 * direction.stepX
            val y = pos.y + 0.92
            val z = pos.z + 0.5 + 0.27 * direction.stepZ
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0)
            level.addParticle(particleSupplier.get(), x, y, z, 0.0, 0.0, 0.0)
        }
    }
}
