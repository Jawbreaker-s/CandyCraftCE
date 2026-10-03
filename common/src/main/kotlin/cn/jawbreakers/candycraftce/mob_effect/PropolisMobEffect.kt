package cn.jawbreakers.candycraftce.mob_effect

import cn.jawbreakers.candycraftce.registry.CMobEffects
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes

class PropolisMobEffect : MobEffect(MobEffectCategory.HARMFUL, 0xE7B83D) {
    companion object {
        const val UUID = "D43B7B37-8CF5-4A2E-A119-06D36B6559C6"
        val LivingEntity.propolis: Boolean
            get() = hasEffect(CMobEffects.propolis.get())

    }

    init {
        addAttributeModifier(Attributes.MOVEMENT_SPEED, UUID, -0.30, AttributeModifier.Operation.MULTIPLY_TOTAL)
    }
}
