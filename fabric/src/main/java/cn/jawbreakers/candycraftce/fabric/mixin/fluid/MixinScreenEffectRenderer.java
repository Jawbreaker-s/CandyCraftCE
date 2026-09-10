package cn.jawbreakers.candycraftce.fabric.mixin.fluid;

import cn.jawbreakers.candycraftce.fabric.fluid.CFlowingFluid;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FluidState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ScreenEffectRenderer.class)
public abstract class MixinScreenEffectRenderer {
	@Inject(method = "renderScreenEffect", at = @At("HEAD"))
	private static void renderScreenEffect(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
		LocalPlayer player = Objects.requireNonNull(minecraft.player);
		if (!player.isSpectator()) {
			double d = player.getEyeY() - 0.11111111;
			BlockPos blockPos = BlockPos.containing(player.getX(), d, player.getZ());
			if (player.level().getFluidState(player.blockPosition()).getType() instanceof CFlowingFluid cff) {
				FluidState fluidState = player.level().getFluidState(blockPos);
				double e = (float) blockPos.getY() + fluidState.getHeight(player.level(), blockPos);
				if (e > d) {
					ResourceLocation tex = cff.getProperties().getType().getRenderOverlayTexture();
					if (tex != null) {
						candycraftce$renderFluid(minecraft, poseStack, tex);
					}
				}
			}
		}
	}

	/**
	 * copy from {@link ScreenEffectRenderer#renderWater}
	 */
	@Unique
	private static void candycraftce$renderFluid(Minecraft minecraft, PoseStack poseStack, ResourceLocation underwater) {
		LocalPlayer player = Objects.requireNonNull(minecraft.player);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, underwater);
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		BlockPos blockPos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
		float f = LightTexture.getBrightness(player.level().dimensionType(), player.level().getMaxLocalRawBrightness(blockPos));
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(f, f, f, 0.1F);
		float m = -player.getYRot() / 64.0F;
		float n = player.getXRot() / 64.0F;
		Matrix4f matrix4f = poseStack.last().pose();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(matrix4f, -1.0F, -1.0F, -0.5F).uv(4.0F + m, 4.0F + n).endVertex();
		bufferBuilder.vertex(matrix4f, 1.0F, -1.0F, -0.5F).uv(0.0F + m, 4.0F + n).endVertex();
		bufferBuilder.vertex(matrix4f, 1.0F, 1.0F, -0.5F).uv(0.0F + m, 0.0F + n).endVertex();
		bufferBuilder.vertex(matrix4f, -1.0F, 1.0F, -0.5F).uv(4.0F + m, 0.0F + n).endVertex();
		BufferUploader.drawWithShader(bufferBuilder.end());
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
	}
}
