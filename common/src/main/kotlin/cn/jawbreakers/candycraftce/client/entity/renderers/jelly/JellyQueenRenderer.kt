package cn.jawbreakers.candycraftce.client.entity.renderers.jelly

import cn.jawbreakers.candycraftce.entity.jelly.JellyQueen
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation

/**
 * Created in 2026/10/7 15:14 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class JellyQueenRenderer(context: EntityRendererProvider.Context) : BasicJellyRenderer<JellyQueen>(context) {
    companion object {
        private val texture_normal = "jelly/queen_normal".modLoc().entityTex()
        private val texture_angry = "jelly/queen_angry".modLoc().entityTex()
        private val texture_frenzied = "jelly/queen_frenzied".modLoc().entityTex()
    }

    override fun getTextureLocation(entity: JellyQueen): ResourceLocation {
        return when (entity.state) {
            JellyQueen.STATE_SLEEPING -> texture_sleeping
            JellyQueen.STATE_ANGRY -> texture_angry
            JellyQueen.STATE_FRENZIED -> texture_frenzied
            else -> texture_normal
        }
    }
}