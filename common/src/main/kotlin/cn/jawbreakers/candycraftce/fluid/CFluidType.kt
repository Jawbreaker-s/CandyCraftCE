package cn.jawbreakers.candycraftce.fluid

import cn.jawbreakers.candycraftce.utils.ClientOnly
import com.mojang.blaze3d.shaders.FogShape
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Camera
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.FogRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Rarity
import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.math.min

/**
 * @param lightLevel 流体发出的光照等级（0~15）
 * @param density 流体密度(≤0)表示比空气轻
 * @param temperature 流体温度
 * @param viscosity 流体的黏稠度(>0) 数值越大，流体扩散速度越慢（如岩浆黏度远大于水）
 *
 * @param motionScale 流体对实体速度的缩放系数。用于计算流体推动实体时的速度倍率
 * @param canPushEntity 是否允许流体推动实体（如水流推动玩家/物品）
 * @param canSwim 实体是否能在该流体中游泳（按住跳跃键上浮）
 * @param canDrown 生物是否会在该流体中溺水（氧气条消耗）
 *
 * */
class CFluidType(

    val descriptionId: String,
    val lightLevel: Int = 0,
    val density: Int = 1000,
    val temperature: Int = 300,
    val viscosity: Int = 1000,
    val rarity: Rarity = Rarity.COMMON,

    val motionScale: Double = 0.014,
    val canSwim: Boolean = true,
    val canPushEntity: Boolean = true,
    val canDrown: Boolean = true,
    //CLIENTS
    //格式: block/xxx
    val stillTexture: ResourceLocation? = null,
    val flowingTexture: ResourceLocation? = null,
    val overlayTexture: ResourceLocation? = null,//透过透明方块
    //格式: texture/misc/xxx_underwater.png
    val renderOverlayTexture: ResourceLocation? = null,//在其中
    val isTransparent: Boolean = false,
    val fogColor: Vector3fc? = null,
    val fogRenderType: FogRenderType? = null,
    val tintColor: Int = -0x1,
) {

    @ClientOnly
    fun modifyFogColor(
        camera: Camera,
        partialTick: Float,
        level: ClientLevel,
        renderDistance: Int,
        darkenWorldAmount: Float,
        fluidFogColor: Vector3f,
    ): Vector3f? {
        return fogColor?.let(::Vector3f)
    }


}


@ClientOnly
interface FogRenderType {
    companion object {
        val lava = object : FogRenderType {
            override fun modifyFogRender(
                camera: Camera,
                mode: FogRenderer.FogMode,
                renderDistance: Float,
                partialTick: Float,
                nearDistance: Float,
                farDistance: Float,
                shape: FogShape,
            ) {
                val entity = camera.entity
                if (entity.isSpectator) {
                    RenderSystem.setShaderFogStart(-8.0f)
                    RenderSystem.setShaderFogEnd(renderDistance * 0.5f)
                } else if (entity is LivingEntity && entity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                    RenderSystem.setShaderFogStart(0.0f)
                    RenderSystem.setShaderFogEnd(3.0f)
                } else {
                    RenderSystem.setShaderFogStart(0.25f)
                    RenderSystem.setShaderFogEnd(1.0f)
                }
                RenderSystem.setShaderFogShape(shape)
            }
        }

        fun water(maxDistance: Float) = object : FogRenderType {
            override fun modifyFogRender(
                camera: Camera,
                mode: FogRenderer.FogMode,
                renderDistance: Float,
                partialTick: Float,
                nearDistance: Float,
                farDistance: Float,
                shape: FogShape,
            ) {
                RenderSystem.setShaderFogStart(-8.0f)
                RenderSystem.setShaderFogEnd(min(maxDistance, renderDistance))
                RenderSystem.setShaderFogShape(shape)
            }
        }
    }

    @ClientOnly
    fun modifyFogRender(
        camera: Camera,
        mode: FogRenderer.FogMode,
        renderDistance: Float,
        partialTick: Float,
        nearDistance: Float,
        farDistance: Float,
        shape: FogShape,
    )
}