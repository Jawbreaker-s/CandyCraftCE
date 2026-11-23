package cn.jawbreakers.candycraftce.client.entity.layers

import cn.jawbreakers.candycraftce.client.render.PropolisGlintRenderType
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.SlimeModel
import net.minecraft.client.model.geom.EntityModelSet
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.world.entity.monster.Slime

/**
 * Renders the propolis glint on the slime shell that is actually visible.
 */
class PropolisSlimeGlintLayer(
    parent: RenderLayerParent<Slime, SlimeModel<Slime>>,
    modelSet: EntityModelSet,
) : RenderLayer<Slime, SlimeModel<Slime>>(parent) {
    private val outerModel: SlimeModel<Slime> = SlimeModel<Slime>(modelSet.bakeLayer(ModelLayers.SLIME_OUTER))

    override fun render(
        poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int, entity: Slime,
        limbSwing: Float, limbSwingAmount: Float, partialTick: Float, ageInTicks: Float,
        netHeadYaw: Float, headPitch: Float,
    ) {
        parentModel.copyPropertiesTo(outerModel)
        outerModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch)
        val consumer = buffer.getBuffer(PropolisGlintRenderType.PROPOLIS_ENTITY_GLINT)
        outerModel.renderToBuffer(
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
