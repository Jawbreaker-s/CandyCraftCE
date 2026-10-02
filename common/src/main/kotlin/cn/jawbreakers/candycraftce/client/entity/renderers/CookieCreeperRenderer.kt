package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.entity.CookieCreeper
import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import net.minecraft.client.renderer.entity.CreeperRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.monster.Creeper

class CookieCreeperRenderer(context: EntityRendererProvider.Context) : CreeperRenderer(context) {
    companion object {
        private val texture = CEntityTypes.cookie_creeper.id.entityTex()
    }

    override fun getTextureLocation(entity: Creeper): ResourceLocation = texture

    override fun getWhiteOverlayProgress(creeper: Creeper, partialTicks: Float): Float {
        if (creeper is CookieCreeper) {
            val f = creeper.getSwelling(partialTicks)
            return when {
                f < 0.9 && (f * (10f / 30f * 80f)).toInt() % 2 == 0 -> 0.0f
                else -> Mth.clamp(f, 0.5f, 1.0f)
            }
        }
        return super.getWhiteOverlayProgress(creeper, partialTicks)
    }
}
