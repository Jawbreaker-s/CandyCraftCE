package cn.jawbreakers.candycraftce.mixin.color;

import cn.jawbreakers.candycraftce.client.PuddingColor;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created in 2026/11/3 23:28 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Inject(method = "allChanged", at = @At("HEAD"))
	private void allChanged(CallbackInfo ci) {
		PuddingColor.INSTANCE.invalidateAll();
	}
}
