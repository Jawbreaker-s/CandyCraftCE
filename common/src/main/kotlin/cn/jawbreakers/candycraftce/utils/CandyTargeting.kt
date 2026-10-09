package cn.jawbreakers.candycraftce.utils

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player

object CandyTargeting {
    fun canAttackPlayer(player: Player, attacker: LivingEntity): Boolean {
        return player.isAlive && !player.abilities.instabuild && !player.isSpectator
    }


    fun canAttackEntity(entity: Entity, attacker: LivingEntity): Boolean {
        return entity.isAlive && (entity !is Player || canAttackPlayer(entity, attacker))
    }
}
