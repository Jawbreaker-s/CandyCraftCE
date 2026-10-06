package cn.jawbreakers.candycraftce.client.entity.renderers.jelly

import cn.jawbreakers.candycraftce.entity.jelly.BasicJelly
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.SlimeModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer
import net.minecraft.util.Mth

//copy from SlimeRenderer
abstract class BasicJellyRenderer<T : BasicJelly>(context: EntityRendererProvider.Context) :
    MobRenderer<T, SlimeModel<T>>(context, SlimeModel<T>(context.bakeLayer(ModelLayers.SLIME)), 0.25f) {
    init {
        this.addLayer(SlimeOuterLayer(this, context.modelSet))
    }

    override fun render(
        entity: T,
        entityYaw: Float,
        partialTicks: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
    ) {
        this.shadowRadius = 0.25f * entity.size.toFloat()
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight)
    }

    override fun scale(livingEntity: T, poseStack: PoseStack, partialTickTime: Float) {
        poseStack.scale(0.999f, 0.999f, 0.999f)
        poseStack.translate(0.0f, 0.001f, 0.0f)
        val f1 = livingEntity.size.toFloat()
        val f2 = Mth.lerp(partialTickTime, livingEntity.oSquish, livingEntity.squish) / (f1 * 0.5f + 1.0f)
        val f3 = 1.0f / (f2 + 1.0f)
        poseStack.scale(f3 * f1, 1.0f / f3 * f1, f3 * f1)
    }


}
