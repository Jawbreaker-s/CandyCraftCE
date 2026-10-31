package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.client.entity.layers.LayerGingerbreadManJob
import cn.jawbreakers.candycraftce.client.entity.models.GingerbreadManModel
import cn.jawbreakers.candycraftce.entity.GingerbreadMan
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation


/**
 * author [Bread_NiceCat](https://gitee.com/Bread_NiceCat)
 * date 2023/1/27 9:56
 */
class GingerbreadManRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<GingerbreadMan, GingerbreadManModel>(
        context,
        GingerbreadManModel(context),
        0.25f
    ) {
    companion object {
        val base = "gingerbread_man".modLoc()
        val main_tex: ResourceLocation = base.entityTex("/main")
    }

    init {
        addLayer(LayerGingerbreadManJob(this))
    }

    override fun getTextureLocation(pEntity: GingerbreadMan): ResourceLocation = main_tex
}
