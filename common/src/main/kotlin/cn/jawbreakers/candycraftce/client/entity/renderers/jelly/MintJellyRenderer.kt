package cn.jawbreakers.candycraftce.client.entity.renderers.jelly

import cn.jawbreakers.candycraftce.entity.jelly.MintJelly
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth

/**
 * Created in 2026/10/6 23:37 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class MintJellyRenderer(context: EntityRendererProvider.Context) : BasicJellyRenderer<MintJelly>(context) {
    companion object {
        val texture = "jelly/mint".modLoc().entityTex()
    }

    override fun setupRotations(
        entity: MintJelly,
        poseStack: PoseStack,
        ageInTicks: Float,
        rotationYaw: Float,
        partialTicks: Float,
    ) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks)
        if (!entity.onGround()) {
            val flip = (entity.tickCount + partialTicks) * 28.0f
            poseStack.mulPose(Axis.XP.rotationDegrees(flip))
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(((entity.tickCount + partialTicks) * 0.35f)) * 14.0f))
        }
    }

    override fun getTextureLocation(entity: MintJelly): ResourceLocation = texture
}