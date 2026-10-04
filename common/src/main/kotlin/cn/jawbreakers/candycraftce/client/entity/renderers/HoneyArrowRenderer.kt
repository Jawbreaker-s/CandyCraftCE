package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.entity.HoneyArrow
import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import net.minecraft.client.renderer.entity.ArrowRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation


class HoneyArrowRenderer(context: EntityRendererProvider.Context) : ArrowRenderer<HoneyArrow>(context) {
    override fun getTextureLocation(entity: HoneyArrow): ResourceLocation {
        return texture
    }

    companion object {
        private val texture = CEntityTypes.honey_arrow.id.entityTex()
    }
}
