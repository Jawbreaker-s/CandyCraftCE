package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.entity.CaramelWolf
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.WolfRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.animal.Wolf

class CaramelWolfRenderer(context: EntityRendererProvider.Context) : WolfRenderer(context) {
    companion object {
        private val base = "wolf".modLoc()
        private val WILD = base.entityTex("/wolfcandy")
        private val ANGRY = base.entityTex("/wolf_angrycandy")
        private val TAME = base.entityTex("/wolf_tamecandy")
        private val TAME_READY = base.entityTex("/wolf_tamecandy2")
    }

    override fun getTextureLocation(entity: Wolf): ResourceLocation {
        if (entity !is CaramelWolf) return WILD
        if (entity.isTame) {
            return if (entity.caramelTime <= 0) TAME_READY else TAME
        }
        return if (entity.isAngry) ANGRY else WILD
    }
}
