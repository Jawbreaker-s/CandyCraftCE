package cn.jawbreakers.candycraftce.utils

import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.stream.Stream
import kotlin.math.max
import kotlin.math.min

/**
 * Author [Bread_NiceCat](https://gitee.com/Bread_NiceCat)
 * Date 2022/12/24 18:41
 */
object CLevelUtils {
    operator fun ChunkPos.contains(pos: BlockPos): Boolean {
        return pos.x in minBlockX..maxBlockX && pos.z in minBlockZ..maxBlockZ
    }

    fun spawnItemEntity(level: Level, pos: Vec3i, stack: ItemStack): ItemEntity? {
        return spawnItemEntity(level, Vec3.atCenterOf(pos), stack)
    }

    fun spawnItemEntity(level: Level, pos: Vec3, stack: ItemStack): ItemEntity? {
        return spawnItemEntity(level, pos.x(), pos.y(), pos.z(), stack)
    }

    fun spawnItemEntity(level: Level, posX: Double, posY: Double, posZ: Double, stack: ItemStack): ItemEntity? {
        if (!stack.isEmpty) {
            val entity = ItemEntity(level, posX, posY, posZ, stack)
            level.addFreshEntity(entity)
            return entity
        }
        return null
    }

    fun BlockPos.move(direction: Direction, distance: Int): BlockPos {
        return when (direction) {
            Direction.DOWN -> below(distance)
            Direction.UP -> above(distance)
            Direction.NORTH -> north(distance)
            Direction.SOUTH -> south(distance)
            Direction.WEST -> west(distance)
            Direction.EAST -> east(distance)
        }
    }

    fun getNeighbourPos(
        pos: BlockPos,
        enableX: Boolean = true,
        enableY: Boolean = true,
        enableZ: Boolean = true,
    ): Stream<BlockPos> {
        return Direction.entries.stream()
            .filter {
                when (it) {
                    Direction.UP, Direction.DOWN -> enableY
                    Direction.EAST, Direction.WEST -> enableX
                    Direction.SOUTH, Direction.NORTH -> enableZ
                }
            }
            .map { pos.move(it, 1) }
    }

    fun particleBlock(particle: ParticleOptions, level: ClientLevel, pos: BlockPos, step: Double) {
        particleBlock(
            particle,
            level,
            pos.x.toDouble(),
            pos.y.toDouble(),
            pos.z.toDouble(),
            pos.x.toDouble(),
            pos.y.toDouble(),
            pos.z.toDouble(),
            step
        )
    }

    fun particleBlock(particle: ParticleOptions, level: ClientLevel, from: BlockPos, to: BlockPos, step: Double) {
        particleBlock(
            particle,
            level,
            from.x.toDouble(),
            from.y.toDouble(),
            from.z.toDouble(),
            to.x.toDouble(),
            to.y.toDouble(),
            to.z.toDouble(),
            step
        )
    }

    /**
     * 在 [(x,y,z),(x2,y2,z2)](两端都包括) 生成粒子块，就像领地插件圈地时的粒子效果
     */
    fun particleBlock(
        particle: ParticleOptions, level: ClientLevel,
        x: Double, y: Double, z: Double,
        x2: Double, y2: Double, z2: Double,
        step: Double,
    ) {
        val xo = min(x, x2)
        val yo = min(y, y2)
        val zo = min(z, z2)
        val xm = max(x, x2) + 1
        val ym = max(y, y2) + 1
        val zm = max(z, z2) + 1
        var u = xo
        while (u < xm + step) {
            level.addParticle(particle, u, yo, zo, 0.0, 0.0, 0.0)
            level.addParticle(particle, u, ym, zo, 0.0, 0.0, 0.0)
            level.addParticle(particle, u, yo, zm, 0.0, 0.0, 0.0)
            level.addParticle(particle, u, ym, zm, 0.0, 0.0, 0.0)
            u += step
        }
        var v = yo
        while (v < ym + step) {
            level.addParticle(particle, xo, v, zo, 0.0, 0.0, 0.0)
            level.addParticle(particle, xm, v, zo, 0.0, 0.0, 0.0)
            level.addParticle(particle, xo, v, zm, 0.0, 0.0, 0.0)
            level.addParticle(particle, xm, v, zm, 0.0, 0.0, 0.0)
            v += step
        }
        var w = zo
        while (w < zm + step) {
            level.addParticle(particle, xo, yo, w, 0.0, 0.0, 0.0)
            level.addParticle(particle, xm, yo, w, 0.0, 0.0, 0.0)
            level.addParticle(particle, xo, ym, w, 0.0, 0.0, 0.0)
            level.addParticle(particle, xm, ym, w, 0.0, 0.0, 0.0)
            w += step
        }
    }
}
