package cn.jawbreakers.candycraftce.mixin.loading_screen;

import cn.jawbreakers.candycraftce.client.level.CandyLoadingScreen;
import cn.jawbreakers.candycraftce.mixin_stub.ICandyLoadingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created in 2026/10/2 10:43 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@Mixin(Screen.class)
public abstract class MixinScreen implements ICandyLoadingScreen {

	@Unique
	private ClientLevel candycraftce$targetLevel = null;

	@Override
	public void candycraftce_setTargetLevel(ClientLevel level) {
		candycraftce$targetLevel = level;
	}

	@Inject(method = "renderDirtBackground", at = @At("HEAD"), cancellable = true)
	public void onRenderDirtBackground(GuiGraphics guiGraphics, CallbackInfo ci) {
		ClientLevel level = candycraftce$targetLevel == null ? Minecraft.getInstance().level : candycraftce$targetLevel;
		if (level != null) {
			if (CandyLoadingScreen.INSTANCE.renderCandyBackground(guiGraphics, level, (Screen) (Object) this)) {
				ci.cancel();
			}
		}
	}
}
