package cn.jawbreakers.candycraftce.mixin.portal;

import cn.jawbreakers.candycraftce.block.CaramelPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created in 2026/9/22 00:02 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@Mixin(Level.class)
public abstract class MixinLevel {
	@Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
			at = @At(value = "RETURN"))
	void setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
		Block block = state.getBlock();
		if (cir.getReturnValue() && block instanceof LiquidBlock liquid) {
			CaramelPortalBlock.onLiquidPlace((Level) (Object) this, pos, liquid.getFluidState(state));
		}
	}
}
