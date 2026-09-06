package cn.jawbreakers.candycraftce.mixin.render;

import cn.jawbreakers.candycraftce.mixin_stub.ISpecialEffectsAddition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {
	@Shadow
	private ClientLevel level;

	@Shadow
	private int ticks;

	@Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
	private void renderClouds(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
		ISpecialEffectsAddition addition = ((ISpecialEffectsAddition) level.effects());
		if (addition.candycraftce$renderClouds(level, ticks, partialTick, poseStack, camX, camY, camZ, projectionMatrix)) {
			ci.cancel();
		}
	}

	@Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
	private void renderSky(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
		ISpecialEffectsAddition addition = ((ISpecialEffectsAddition) level.effects());
		if (addition.candycraftce$renderSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, skyFogSetup)) {
			ci.cancel();
		}
	}

	@Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true)
	private void candycraftce$renderSnowAndRain(LightTexture lightTexture, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
		ISpecialEffectsAddition addition = ((ISpecialEffectsAddition) level.effects());
		if (addition.candycraftce$renderSnowAndRain(level, ticks, partialTick, lightTexture, camX, camY, camZ)) {
			ci.cancel();
		}
	}

	@Inject(method = "tickRain", at = @At("HEAD"), cancellable = true)
	private void tickRain(Camera camera, CallbackInfo ci) {
		ISpecialEffectsAddition addition = ((ISpecialEffectsAddition) level.effects());
		if (addition.candycraftce$tickRain(level, ticks, camera)) {
			ci.cancel();
		}
	}
}
