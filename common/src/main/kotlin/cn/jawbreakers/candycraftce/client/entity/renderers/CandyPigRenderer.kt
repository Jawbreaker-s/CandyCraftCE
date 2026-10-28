package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.PigRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.animal.Pig

class CandyPigRenderer(context: EntityRendererProvider.Context) : PigRenderer(context) {
    companion object {
        private val texture: ResourceLocation = "candy_pig".modLoc().entityTex()
    }

    override fun getTextureLocation(entity: Pig) = texture
}
