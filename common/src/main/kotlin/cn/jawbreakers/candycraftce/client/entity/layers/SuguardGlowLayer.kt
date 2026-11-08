package cn.jawbreakers.candycraftce.client.entity.layers

import cn.jawbreakers.candycraftce.client.entity.models.SuguardModel
import cn.jawbreakers.candycraftce.entity.Suguard
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.resources.ResourceLocation

/**
 * Created in 2026/11/8 11:31 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class SuguardGlowLayer<S : Suguard>(
    renderer: RenderLayerParent<S, SuguardModel<S>>,
    val glowRenderer: IGlowRenderer<S>,
) :
    RenderLayer<S, SuguardModel<S>>(renderer) {
    override fun render(
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        entity: S,
        limbSwing: Float,
        limbSwingAmount: Float,
        partialTick: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float,
    ) {
        val glow = glowRenderer.getGlowTexture(entity)
        val consumer = buffer.getBuffer(RenderType.eyes(glow))
        parentModel.renderToBuffer(
            poseStack,
            consumer,
            LightTexture.FULL_BRIGHT,
            LivingEntityRenderer.getOverlayCoords(entity, 0.0f),
            1.0f,
            1.0f,
            1.0f,
            1.0f
        )
    }

    fun interface IGlowRenderer<S : Suguard> {
        fun getGlowTexture(entity: S): ResourceLocation
    }
}