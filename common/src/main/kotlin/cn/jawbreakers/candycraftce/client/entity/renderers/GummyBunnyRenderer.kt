package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.client.entity.models.GummyBunnyModel
import cn.jawbreakers.candycraftce.client.entity.models.GummyBunnyOuterModel
import cn.jawbreakers.candycraftce.entity.GummyBunny
import cn.jawbreakers.candycraftce.utils.CLevelUtils.component1
import cn.jawbreakers.candycraftce.utils.CLevelUtils.component2
import cn.jawbreakers.candycraftce.utils.CLevelUtils.component3
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.LinearGradient.Companion.vecColorNormal
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.resources.ResourceLocation

class GummyBunnyRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<GummyBunny, GummyBunnyModel<GummyBunny>>(
        context,
        GummyBunnyModel(context.bakeLayer(GummyBunnyModel.LAYER)),
        0.3f
    ) {
    companion object {
        val base = "bunny".modLoc()
        private val face = base.entityTex("/face")
        private val body = base.entityTex("/body")
    }

    init {
        addLayer(FurLayer(this, context))
        addLayer(GummyShellLayer(this, context))
    }

    override fun scale(entity: GummyBunny, poseStack: PoseStack, partialTickTime: Float) {
        if (entity.isBaby) {
            poseStack.scale(0.7f, 0.7f, 0.7f)
        }
    }

    override fun getTextureLocation(entity: GummyBunny): ResourceLocation = face
    

    private class FurLayer(renderer: GummyBunnyRenderer, context: EntityRendererProvider.Context) :
        RenderLayer<GummyBunny, GummyBunnyModel<GummyBunny>>(renderer) {

        private val model: GummyBunnyModel<GummyBunny> = GummyBunnyModel(context.bakeLayer(GummyBunnyModel.LAYER))
        override fun render(
            poseStack: PoseStack,
            buffer: MultiBufferSource,
            packedLight: Int,
            bunny: GummyBunny,
            limbSwing: Float,
            limbSwingAmount: Float,
            partialTick: Float,
            ageInTicks: Float,
            netHeadYaw: Float,
            headPitch: Float,
        ) {
            if (bunny.isInvisible || bunny.isSwampVariant) {
                return
            }

            parentModel.copyPropertiesTo(model)
            model.prepareMobModel(bunny, limbSwing, limbSwingAmount, partialTick)
            model.setupAnim(bunny, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch)
            val consumer = buffer.getBuffer(RenderType.entityTranslucent(body))
            val (red, green, blue) = bunny.color.vecColorNormal
            model.renderToBuffer(
                poseStack,
                consumer,
                packedLight,
                getOverlayCoords(bunny, 0.0f),
                red,
                green,
                blue,
                0.58f
            )
        }
    }

    /**
     * Swamp variant uses the gummy mouse style: a tinted body underneath and
     * an inflated translucent shell on top, instead of the coplanar fur pass.
     */
    private class GummyShellLayer(renderer: GummyBunnyRenderer, context: EntityRendererProvider.Context) :
        RenderLayer<GummyBunny, GummyBunnyModel<GummyBunny>>(renderer) {
        private val bodyModel: GummyBunnyModel<GummyBunny> = GummyBunnyModel(context.bakeLayer(GummyBunnyModel.LAYER))

        private val shellModel: GummyBunnyOuterModel<GummyBunny> =
            GummyBunnyOuterModel(context.bakeLayer(GummyBunnyOuterModel.LAYER))

        override fun render(
            poseStack: PoseStack,
            buffer: MultiBufferSource,
            packedLight: Int,
            bunny: GummyBunny,
            limbSwing: Float,
            limbSwingAmount: Float,
            partialTick: Float,
            ageInTicks: Float,
            netHeadYaw: Float,
            headPitch: Float,
        ) {
            if (bunny.isInvisible || !bunny.isSwampVariant) {
                return
            }
            val (red, green, blue) = bunny.color.vecColorNormal

            val overlay = getOverlayCoords(bunny, 0.0f)

            parentModel.copyPropertiesTo(bodyModel)
            bodyModel.prepareMobModel(bunny, limbSwing, limbSwingAmount, partialTick)
            bodyModel.setupAnim(bunny, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch)
            bodyModel.renderToBuffer(
                poseStack, buffer.getBuffer(RenderType.entityTranslucent(face)),
                packedLight, overlay, red, green, blue, 1.0f
            )

            shellModel.prepareMobModel(bunny, limbSwing, limbSwingAmount, partialTick)
            shellModel.setupAnim(bunny, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch)
            shellModel.renderToBuffer(
                poseStack, buffer.getBuffer(RenderType.entityTranslucent(body)),
                packedLight, overlay, red, green, blue, 0.6f
            )
        }
    }
}
