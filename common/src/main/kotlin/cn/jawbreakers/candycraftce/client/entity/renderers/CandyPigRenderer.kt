package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.PigRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.animal.Pig

class CandyPigRenderer(context: EntityRendererProvider.Context) : PigRenderer(context) {
    companion object {
        private val texture: ResourceLocation = CEntityTypes.candy_pig.id.entityTex()
    }

    override fun getTextureLocation(entity: Pig) = texture
}
