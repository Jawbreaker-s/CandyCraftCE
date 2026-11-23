package cn.jawbreakers.candycraftce.entity.jelly

import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

/**
 * Created in 2026/10/6 22:10 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 * 自爆型
 */
open class RaspberryJelly(entityType: EntityType<out RaspberryJelly>, level: Level) : TinyJelly(entityType, level) {

    companion object {
        private val particle by lazy {
            ItemParticleOption(
                ParticleTypes.ITEM,
                CItems.raspberry_jelly_ball.defaultInstance
            )
        }

    }

    override fun playerTouch(entity: Player) {
        level().explode(this, x, y, z, 1.0F, Level.ExplosionInteraction.MOB)
        discard()
    }

    override fun getSize(): Int = 2

    override fun getParticleType() = particle
}