package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.client.entity.models.BeetleModel
import cn.jawbreakers.candycraftce.entity.LicoriceBeetle
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation

/**
 * Created in 2026/11/6 13:30 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class LicoriceBeetleRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<LicoriceBeetle, BeetleModel<LicoriceBeetle>>(
        context, BeetleModel(context.bakeLayer(BeetleModel.LAYER)), 1f
    ) {
    companion object {
        private val base = "beetle".modLoc()
        val texture_sleeping = base.entityTex("/licorice_beetle_sleep")
        val texture = base.entityTex("/licorice_beetle")
    }

    override fun getTextureLocation(entity: LicoriceBeetle): ResourceLocation {
        return if (entity.awake) texture else texture_sleeping
    }

    override fun scale(livingEntity: LicoriceBeetle, poseStack: PoseStack, partialTickTime: Float) {
        poseStack.scale(2.0F, 2.0F, 2.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
    }
}