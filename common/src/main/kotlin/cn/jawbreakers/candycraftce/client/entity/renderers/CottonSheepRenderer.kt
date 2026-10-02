package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.entity.CottonCandySheep
import cn.jawbreakers.candycraftce.entity.WaffleSheep
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.client.renderer.entity.EntityRendererProvider

/**
 * Created in 2026/9/26 11:08 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class CottonSheepRenderer(context: EntityRendererProvider.Context) :
    WaffleSheepRenderer("sheep/cotton".modLoc(), context) {
    override fun hasFur(sheep: WaffleSheep): Boolean {
        return if (sheep is CottonCandySheep) sheep.furry else super.hasFur(sheep)
    }
}

class RaspberryCottonSheepRenderer(context: EntityRendererProvider.Context) :
    WaffleSheepRenderer("sheep/raspberry_cotton".modLoc(), context) {
    override fun hasFur(sheep: WaffleSheep): Boolean {
        return if (sheep is CottonCandySheep) sheep.furry else super.hasFur(sheep)
    }
}