package cn.jawbreakers.candycraftce.client.entity.renderers.jelly

import cn.jawbreakers.candycraftce.entity.jelly.RaspberryJelly
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation

/**
 * Created in 2026/10/6 23:45 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class RaspberryJellyRenderer(context: EntityRendererProvider.Context) : BasicJellyRenderer<RaspberryJelly>(context) {
    companion object {
        val texture = "jelly/raspberry".modLoc().entityTex()
    }

    override fun getTextureLocation(entity: RaspberryJelly): ResourceLocation = texture
}