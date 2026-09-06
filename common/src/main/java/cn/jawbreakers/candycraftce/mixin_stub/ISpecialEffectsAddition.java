package cn.jawbreakers.candycraftce.mixin_stub;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public interface ISpecialEffectsAddition {
	default boolean candycraftce$renderClouds(@NotNull ClientLevel level, int ticks, float partialTick, @NotNull PoseStack poseStack, double camX, double camY, double camZ, @NotNull Matrix4f projectionMatrix) {
		return false;
	}

	/**
	 * Renders the sky of this dimension.
	 *
	 * @return true to prevent vanilla sky rendering
	 */
	default boolean candycraftce$renderSky(@NotNull ClientLevel level, int ticks, float partialTick, @NotNull PoseStack poseStack, @NotNull Camera camera, @NotNull Matrix4f projectionMatrix, boolean isFoggy, @NotNull Runnable setupFog) {
		return false;
	}

	/**
	 * Renders the snow and rain effects of this dimension.
	 *
	 * @return true to prevent vanilla snow and rain rendering
	 */
	default boolean candycraftce$renderSnowAndRain(@NotNull ClientLevel level, int ticks, float partialTick, @NotNull LightTexture lightTexture, double camX, double camY, double camZ) {
		return false;
	}

	/**
	 * Ticks the rain of this dimension.
	 *
	 * @return true to prevent vanilla rain ticking
	 */
	default boolean candycraftce$tickRain(@NotNull ClientLevel level, int ticks, @NotNull Camera camera) {
		return false;
	}

}
