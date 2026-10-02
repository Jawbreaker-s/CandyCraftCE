package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.client.entity.models.CranfishModel
import cn.jawbreakers.candycraftce.entity.Cranfish
import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation

class CranfishRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<Cranfish, CranfishModel<Cranfish>>(
        context,
        CranfishModel(context.bakeLayer(CranfishModel.LAYER)),
        0.18f
    ) {
    companion object {
        private val texture = CEntityTypes.cranfish.id.entityTex()
    }

    protected override fun setupRotations(
        entity: Cranfish,
        poseStack: PoseStack,
        ageInTicks: Float,
        rotationYaw: Float,
        partialTick: Float,
    ) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTick)
        if (!entity.isInWater) {
            poseStack.translate(0.0f, 0.05f, 0.0f)
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f))
            poseStack.translate(0.0f, -0.40f, 0.0f)
        }
    }

    override fun getTextureLocation(entity: Cranfish): ResourceLocation = texture
}
