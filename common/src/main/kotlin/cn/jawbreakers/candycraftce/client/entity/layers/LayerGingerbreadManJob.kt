package cn.jawbreakers.candycraftce.client.entity.layers


import cn.jawbreakers.candycraftce.client.entity.models.GingerbreadManModel
import cn.jawbreakers.candycraftce.client.entity.renderers.GingerbreadManRenderer
import cn.jawbreakers.candycraftce.entity.GingerbreadMan
import cn.jawbreakers.candycraftce.utils.CUtils.buildEnumMap
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.resources.ResourceLocation

/**
 * author [Bread_NiceCat](https://gitee.com/Bread_NiceCat)
 * date 2023/1/27 20:25
 * kotlinize 2026/10/31
 */
class LayerGingerbreadManJob(pRenderer: RenderLayerParent<GingerbreadMan, GingerbreadManModel>) :
    RenderLayer<GingerbreadMan, GingerbreadManModel>(pRenderer) {
    companion object {
        private val base = GingerbreadManRenderer.base
        val textures = buildEnumMap<GingerbreadMan.GingerbreadJob, ResourceLocation?> {
            if (it != GingerbreadMan.GingerbreadJob.NONE) base.entityTex("/${it.id}") else null
        }

    }

    override fun render(
        pPoseStack: PoseStack,
        pBuffer: MultiBufferSource,
        pPackedLight: Int,
        entity: GingerbreadMan,
        pLimbSwing: Float,
        pLimbSwingAmount: Float,
        pPartialTick: Float,
        pAgeInTicks: Float,
        pNetHeadYaw: Float,
        pHeadPitch: Float,
    ) {
        val tex = textures[entity.job] ?: return
        parentModel.setupAnim(entity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch)
        parentModel.renderToBuffer(
            pPoseStack,
            pBuffer.getBuffer(RenderType.entityCutout(tex)),
            pPackedLight,
            LivingEntityRenderer.getOverlayCoords(entity, 0.0f),
            1f,
            1f,
            1f,
            1f
        )
    }
}
