package cn.jawbreakers.candycraftce.fabric.mixin.fluid;

import cn.jawbreakers.candycraftce.fabric.fluid.CFabricFluids;
import cn.jawbreakers.candycraftce.fluid.CFluidReferences;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class MixinLiquidBlock {
	@Inject(method = "getDescriptionId", at = @At("HEAD"), cancellable = true)
	private void getDescriptionId(CallbackInfoReturnable<String> cir) {
		if ((Object) this instanceof LiquidBlock self) {
			CFluidReferences ref = CFabricFluids.INSTANCE.getFluids().get(self.fluid);
			if (ref != null) {
				cir.setReturnValue(ref.getPresets().getDescriptionId());
			}
		}
	}
}
