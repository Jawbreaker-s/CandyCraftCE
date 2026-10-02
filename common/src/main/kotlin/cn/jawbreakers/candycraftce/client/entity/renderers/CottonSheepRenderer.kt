package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.entity.CottonCandySheepEntity
import cn.jawbreakers.candycraftce.entity.WaffleSheepEntity
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.client.renderer.entity.EntityRendererProvider

/**
 * Created in 2026/9/26 11:08 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class CottonSheepRenderer(context: EntityRendererProvider.Context) :
    WaffleSheepRenderer("sheep/cotton".modLoc(), context) {
    override fun hasFur(sheep: WaffleSheepEntity): Boolean {
        return if (sheep is CottonCandySheepEntity) sheep.furry else super.hasFur(sheep)
    }
}