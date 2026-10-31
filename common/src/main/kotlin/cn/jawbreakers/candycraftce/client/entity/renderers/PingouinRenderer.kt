package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.client.entity.models.PingouinModel
import cn.jawbreakers.candycraftce.entity.Pingouin
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation

class PingouinRenderer(context: EntityRendererProvider.Context) : MobRenderer<Pingouin, PingouinModel<Pingouin>>(
    context,
    PingouinModel(context.bakeLayer(PingouinModel.LAYER)),
    0.28f
) {
    companion object {
        private val base = "pingouin".modLoc()
        private val textures = Array(Pingouin.COLOR_VARIANTS) {
            base.entityTex("/pingouin_${it}")
        }
    }

    override fun render(
        entity: Pingouin, entityYaw: Float, partialTicks: Float, poseStack: PoseStack,
        buffer: MultiBufferSource, packedLight: Int,
    ) {
        shadowRadius = if (entity.isBaby) 0.14f else 0.28f
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight)
    }

    override fun scale(entity: Pingouin, poseStack: PoseStack, partialTickTime: Float) {
        if (entity.isBaby) {
            poseStack.scale(0.5f, 0.5f, 0.5f)
        }
    }

    override fun getTextureLocation(entity: Pingouin): ResourceLocation {
        return textures[entity.color]
    }
}
