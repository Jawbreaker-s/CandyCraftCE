package cn.jawbreakers.candycraftce.client.entity.renderers.jelly

import cn.jawbreakers.candycraftce.entity.jelly.LemonJelly
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation

/**
 * Created in 2026/10/6 23:44 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class LemonJellyRenderer(context: EntityRendererProvider.Context) : BasicJellyRenderer<LemonJelly>(context) {
    companion object {
        val texture = "jelly/lemon".modLoc().entityTex()
    }

    override fun getTextureLocation(entity: LemonJelly): ResourceLocation = texture
}