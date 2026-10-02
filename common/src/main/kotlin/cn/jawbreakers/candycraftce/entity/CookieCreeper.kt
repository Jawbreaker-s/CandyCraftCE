package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.mixin.entity.CreeperAccessor
import cn.jawbreakers.candycraftce.registry.CItems
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.Creeper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

class CookieCreeper(type: EntityType<out CookieCreeper?>, level: Level) : Creeper(type, level) {
    companion object {
        private const val CREEPER_STALL_TICKS = 20 * 8
    }

    override fun canBeAffected(effect: MobEffectInstance): Boolean {
        return effect.effect !== MobEffects.POISON && super.canBeAffected(effect)
    }

    fun melt() {
        ignite()
        @Suppress("CAST_NEVER_SUCCEEDS")
        (this as CreeperAccessor).apply {
            setMaxSwell(CREEPER_STALL_TICKS)
            setExplosionRadius(10)
        }

    }

    override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(hand)
        val level = level()

        if (stack.`is`(CItems.lollipop.get())) {
            melt()
            if (!level.isClientSide) {
                if (!player.abilities.instabuild) {
                    stack.shrink(1)
                }
                level.playSound(null, blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.7f, 1.1f)
                if (level is ServerLevel) {
                    level.sendParticles(
                        ParticleTypes.HEART,
                        x,
                        y + bbHeight + 0.2,
                        z,
                        6,
                        bbWidth * 0.35,
                        0.25,
                        bbWidth * 0.35,
                        0.02
                    )
                }
//TODO
//            if (player is ServerPlayer) {
//                CCCriteriaTriggers.STALL_CANDY_CREEPER.trigger(player)
//            }
            }
            return InteractionResult.sidedSuccess(level.isClientSide)
        }
        return InteractionResult.PASS
    }


}
