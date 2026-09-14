package cn.jawbreakers.candycraftce.mixin.render;

import cn.jawbreakers.candycraftce.mixin_stub.ISpecialEffectsAddition;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DimensionSpecialEffects.class)
public abstract class MixinDimensionSpecialEffects implements ISpecialEffectsAddition {
}
