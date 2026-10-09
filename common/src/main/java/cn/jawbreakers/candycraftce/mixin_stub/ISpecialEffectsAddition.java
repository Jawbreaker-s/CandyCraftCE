package cn.jawbreakers.candycraftce.mixin_stub;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;

public interface ISpecialEffectsAddition {

	default boolean candycraftce_renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix) {
		return false;
	}

	/**
	 * Renders the sky of this dimension.
	 *
	 * @return true to prevent vanilla sky rendering
	 */
	default boolean candycraftce_renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
		return false;
	}

	/**
	 * Renders the snow and rain effects of this dimension.
	 *
	 * @return true to prevent vanilla snow and rain rendering
	 */
	default boolean candycraftce_renderSnowAndRain(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, double camX, double camY, double camZ) {
		return false;
	}

	/**
	 * Ticks the rain of this dimension.
	 *
	 * @return true to prevent vanilla rain ticking
	 */
	default boolean candycraftce_tickRain(ClientLevel level, int ticks, Camera camera) {
		return false;
	}
}