package cn.jawbreakers.candycraftce.client.entity.layers

import cn.jawbreakers.candycraftce.client.render.PropolisGlintRenderType
import cn.jawbreakers.candycraftce.mob_effect.PropolisMobEffect.Companion.propolis
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.EntityModel
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.world.entity.LivingEntity

/**
 * CandyCraft's final surface pass, rendered after an entity's body and clothing layers.
 */
class PropolisEntityOverlayLayer<T : LivingEntity, M : EntityModel<T>>(parent: RenderLayerParent<T, M>) :
    RenderLayer<T, M>(parent) {


    override fun render(
        poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int, entity: T,
        limbSwing: Float, limbSwingAmount: Float, partialTick: Float, ageInTicks: Float,
        netHeadYaw: Float, headPitch: Float,
    ) {
        if (!entity.propolis || entity.isInvisible) return

        val consumer = buffer.getBuffer(PropolisGlintRenderType.PROPOLIS_ENTITY_GLINT)
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
}
