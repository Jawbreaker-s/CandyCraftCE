package cn.jawbreakers.candycraftce.mixin.loading_screen;

import cn.jawbreakers.candycraftce.mixin_stub.ICandyLoadingScreen;
import cn.jawbreakers.candycraftce.utils.CLogUtils;
import cn.jawbreakers.candycraftce.utils.CPlatformUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created in 2026/10/1 15:38 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

	@Shadow
	@Final
	public Gui gui;
	@Unique
	private ClientLevel candycraftce$nextLevelClient = null;

	@Inject(method = "setLevel",
			at = @At(value = "HEAD")
	)
	void onSetLevel(ClientLevel levelClient, CallbackInfo ci) {
		candycraftce$nextLevelClient = levelClient;
	}

	@SuppressWarnings("ModifyVariableMayUseName")
	@ModifyVariable(method = "setLevel",
			at = @At(value = "STORE", ordinal = 0)
	)
	ProgressScreen modifyScreen(ProgressScreen progressscreen) {
		((ICandyLoadingScreen) progressscreen).candycraftce$setTargetLevel(candycraftce$nextLevelClient);
		return progressscreen;
	}

	@Inject(method = "setScreen", at = @At("HEAD"))
	void onSetScreen(Screen guiScreen, CallbackInfo ci) {
		CPlatformUtils.INSTANCE.ifDev(() -> {
			CLogUtils.INSTANCE.getDebugLog().info("Set screen: {}", guiScreen == null ? null : guiScreen.getClass().getName());
			return null;
		});

	}

}
