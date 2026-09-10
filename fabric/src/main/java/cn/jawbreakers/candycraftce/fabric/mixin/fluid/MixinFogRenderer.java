package cn.jawbreakers.candycraftce.fabric.mixin.fluid;

import cn.jawbreakers.candycraftce.fabric.fluid.CFlowingFluid;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class MixinFogRenderer {

	@Shadow
	private static float fogRed;

	@Shadow
	private static float fogGreen;

	@Shadow
	private static float fogBlue;

	@Inject(method = "setupColor",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V", remap = false)
	)
	private static void setupColor(Camera camera, float partialTicks, ClientLevel level, int renderDistanceChunks, float bossColorModifier, CallbackInfo ci) {
		if (level.getFluidState(camera.getBlockPosition()).getType() instanceof CFlowingFluid cff) {
			Vector3f color = cff.modifyFogColor(camera, partialTicks, level, renderDistanceChunks, bossColorModifier, new Vector3f(fogRed, fogGreen, fogBlue));
			if (color != null) {
				fogRed = color.x;
				fogGreen = color.y;
				fogBlue = color.z;
			}

		}
	}

	@Inject(method = "setupFog", at = @At(value = "TAIL"))
	private static void setupFog(Camera camera, FogRenderer.FogMode fogMode, float farPlaneDistance, boolean bl, float particleTicks, CallbackInfo ci,
	                             @Local FogRenderer.FogData fogData) {
		if (camera.getEntity().level().getFluidState(camera.getBlockPosition()).getType() instanceof CFlowingFluid cff) {
			cff.modifyFogRender(camera, fogMode, farPlaneDistance, particleTicks, fogData.start, fogData.end, fogData.shape);
		}
	}
}