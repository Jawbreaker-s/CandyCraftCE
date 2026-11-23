package cn.jawbreakers.candycraftce.client.entity.layers

import cn.jawbreakers.candycraftce.entity.CottonCandySpider
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.client.model.SpiderModel
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.EyesLayer

class CottonCandySpiderEyesLayer(parent: RenderLayerParent<CottonCandySpider, SpiderModel<CottonCandySpider>>) :
    EyesLayer<CottonCandySpider, SpiderModel<CottonCandySpider>>(parent) {
    companion object {
        private val pink_glowing_eyes: RenderType = RenderType.eyes("spider/cottonspider_eyes".modLoc().entityTex())
    }

    override fun renderType(): RenderType = pink_glowing_eyes
}
