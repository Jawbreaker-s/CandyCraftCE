package cn.jawbreakers.candycraftce.mixin.render;

import cn.jawbreakers.candycraftce.mixin_stub.ISpecialEffectsAddition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DimensionSpecialEffects.class)
public abstract class MixinDimensionSpecialEffects implements ISpecialEffectsAddition {
	@Override
	@Unique
	public boolean candycraftce$renderClouds(@NotNull ClientLevel level, int ticks, float partialTick, @NotNull PoseStack poseStack, double camX, double camY, double camZ, @NotNull Matrix4f projectionMatrix) {
		return ISpecialEffectsAddition.super.candycraftce$renderClouds(level, ticks, partialTick, poseStack, camX, camY, camZ, projectionMatrix);
	}

	@Override
	@Unique
	public boolean candycraftce$renderSky(@NotNull ClientLevel level, int ticks, float partialTick, @NotNull PoseStack poseStack, @NotNull Camera camera, @NotNull Matrix4f projectionMatrix, boolean isFoggy, @NotNull Runnable setupFog) {
		return ISpecialEffectsAddition.super.candycraftce$renderSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
	}

	@Override
	@Unique
	public boolean candycraftce$renderSnowAndRain(@NotNull ClientLevel level, int ticks, float partialTick, @NotNull LightTexture lightTexture, double camX, double camY, double camZ) {
		return ISpecialEffectsAddition.super.candycraftce$renderSnowAndRain(level, ticks, partialTick, lightTexture, camX, camY, camZ);
	}

	@Override
	@Unique
	public boolean candycraftce$tickRain(@NotNull ClientLevel level, int ticks, @NotNull Camera camera) {
		return ISpecialEffectsAddition.super.candycraftce$tickRain(level, ticks, camera);
	}
}
