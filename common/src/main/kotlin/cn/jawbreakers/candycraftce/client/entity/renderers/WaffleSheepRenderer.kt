package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.entity.WaffleSheep
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.SheepFurModel
import net.minecraft.client.model.SheepModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.resources.ResourceLocation

open class WaffleSheepRenderer(base: ResourceLocation, context: EntityRendererProvider.Context) :
    MobRenderer<WaffleSheep, SheepModel<WaffleSheep>>(
        context,
        SheepModel(context.bakeLayer(ModelLayers.SHEEP)),
        0.7f
    ) {
    constructor(context: EntityRendererProvider.Context) : this("sheep/waffle".modLoc(), context)

    protected val texture = base.entityTex()
    protected val hurtTexture = base.entityTex("_hurt")
    protected val furTexture = base.entityTex("_fur")
    open fun hasFur(sheep: WaffleSheep): Boolean = true

    init {
        addLayer(FurLayer(context))
    }

    override fun getTextureLocation(entity: WaffleSheep): ResourceLocation {
        return if (entity.hurtTime > 0) hurtTexture else texture
    }

    inner class FurLayer(context: EntityRendererProvider.Context) :
        RenderLayer<WaffleSheep, SheepModel<WaffleSheep>>(this) {

        private val model = SheepFurModel<WaffleSheep>(context.bakeLayer(ModelLayers.SHEEP_FUR))

        override fun render(
            poseStack: PoseStack,
            buffer: MultiBufferSource,
            packedLight: Int,
            sheep: WaffleSheep,
            limbSwing: Float,
            limbSwingAmount: Float,
            partialTick: Float,
            ageInTicks: Float,
            netHeadYaw: Float,
            headPitch: Float,
        ) {
            if (sheep.isInvisible || !hasFur(sheep)) {
                return
            }

            parentModel.copyPropertiesTo(model)
            model.prepareMobModel(sheep, limbSwing, limbSwingAmount, partialTick)
            model.setupAnim(sheep, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch)
            val consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(furTexture))
            model.renderToBuffer(
                poseStack,
                consumer,
                packedLight,
                getOverlayCoords(sheep, 0.0f),
                1.0f,
                1.0f,
                1.0f,
                1.0f
            )
        }
    }
}
