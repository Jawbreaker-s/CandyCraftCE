package cn.jawbreakers.candycraftce.client.entity.renderers

import cn.jawbreakers.candycraftce.client.entity.models.CaramelBeeModel
import cn.jawbreakers.candycraftce.entity.CaramelBee
import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.utils.CUtils.entityTex
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation


class CaramelBeeRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<CaramelBee, CaramelBeeModel<CaramelBee>>(
        context,
        CaramelBeeModel(context.bakeLayer(CaramelBeeModel.LAYER)),
        0.22f
    ) {
    companion object {
        private val texture = CEntityTypes.caramel_bee.id.entityTex()
    }

    override fun getTextureLocation(entity: CaramelBee): ResourceLocation = texture
}
