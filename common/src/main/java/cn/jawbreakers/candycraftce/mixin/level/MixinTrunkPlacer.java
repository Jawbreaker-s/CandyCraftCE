package cn.jawbreakers.candycraftce.mixin.level;

import cn.jawbreakers.candycraftce.registry.CBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//防止TrunkPlacer替换糖果树底部为泥土
@Mixin(TrunkPlacer.class)
public abstract class MixinTrunkPlacer {
	@Inject(method = "isDirt", at = @At("HEAD"), cancellable = true)
	private static void isDirt(LevelSimulatedReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (level.isStateAtPosition(pos, state -> state.is(CBlockTags.INSTANCE.getCandy_soil()))) {
			cir.setReturnValue(true);
		}
	}
}
