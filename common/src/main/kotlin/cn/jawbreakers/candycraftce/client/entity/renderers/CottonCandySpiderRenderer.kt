package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.client.entity.layers.CottonCandySpiderEyesLayer
import cn.jawbreakers.candycraftce.entity.CottonCandySpider
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.SpiderRenderer
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer

class CottonCandySpiderRenderer(context: EntityRendererProvider.Context) : SpiderRenderer<CottonCandySpider>(context) {
    companion object {
        private val texture = "spider/cottonspider".modLoc().entityTex()
    }

    init {
        layers.removeIf(SpiderEyesLayer::class.java::isInstance)
        addLayer(CottonCandySpiderEyesLayer(this))
    }

    override fun getTextureLocation(entity: CottonCandySpider) = texture
}
