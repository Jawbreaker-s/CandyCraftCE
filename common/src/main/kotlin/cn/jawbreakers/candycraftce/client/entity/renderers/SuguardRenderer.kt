package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.client.entity.layers.SuguardGlowLayer
import cn.jawbreakers.candycraftce.client.entity.models.SuguardModel
import cn.jawbreakers.candycraftce.entity.Suguard
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer
import net.minecraft.resources.ResourceLocation

/**
 * Created in 2026/11/8 10:58 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class SuguardRenderer<S : Suguard>(context: EntityRendererProvider.Context) : MobRenderer<S, SuguardModel<S>>(
    context,
    SuguardModel(context.bakeLayer(SuguardModel.LAYER)),
    0.5f
), SuguardGlowLayer.IGlowRenderer<S> {
    companion object {
        val base = "suguard".modLoc()
        private val suguard_tex = base.entityTex("/suguard")
        private val soldier_tex = base.entityTex("/suguard_soldier")
    }

    init {
        addLayer(ItemInHandLayer(this, context.itemInHandRenderer))
        addLayer(SuguardGlowLayer(this, this))
    }

    override fun getTextureLocation(entity: S): ResourceLocation = suguard_tex
    private val glows = hashMapOf<ResourceLocation, ResourceLocation>()
    override fun getGlowTexture(entity: S): ResourceLocation = glows.computeIfAbsent(getTextureLocation(entity)) {
        getTextureLocation(entity).withPath { it.replace(".png", "_glow.png") }
    }
}