package cn.jawbreakers.candycraftce.mob_effect

import cn.jawbreakers.candycraftce.registry.CMobEffects
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes

class CloyingMobEffect : MobEffect(MobEffectCategory.HARMFUL, 0xD8D6D0) {
    companion object {
        const val UUID = "FE8C948D-D968-44C8-BE65-7EEAD3A6B41D"
        val LivingEntity.cloying: Boolean
            get() = hasEffect(CMobEffects.cloying.get())

    }

    init {
        addAttributeModifier(Attributes.ATTACK_DAMAGE, UUID, -2.0, AttributeModifier.Operation.ADDITION)
    }
}
