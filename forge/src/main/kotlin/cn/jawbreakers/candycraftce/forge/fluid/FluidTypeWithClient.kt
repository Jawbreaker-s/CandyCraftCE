package cn.jawbreakers.candycraftce.forge.fluid

import cn.jawbreakers.candycraftce.fluid.CFluidPresets
import cn.jawbreakers.candycraftce.utils.LinearGradient.Companion.vecColor
import com.mojang.blaze3d.shaders.FogShape
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.FogRenderer
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.FogType
import net.minecraft.world.level.pathfinder.BlockPathTypes
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions
import net.minecraftforge.common.SoundActions
import net.minecraftforge.fluids.FluidType
import org.joml.Vector3f
import java.util.function.Consumer
import kotlin.math.min

open class FluidTypeWithClient(
    properties: Properties,
    val fogType: FogType,
    val presets: CFluidPresets,
) : FluidType(properties) {
    companion object {
        fun waterLike(
            presets: CFluidPresets,
            modifier: (Properties) -> Unit = {},
        ) = object : FluidTypeWithClient(
            Properties.create()
                .descriptionId(presets.descriptionId)
                .fallDistanceModifier(0F)
                .canExtinguish(true)
                .canConvertToSource(true)
                .supportsBoating(true)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
                .canHydrate(true)
                .also(modifier),
            FogType.WATER,
            presets
        ) {
            override fun getBlockPathType(
                state: FluidState,
                level: BlockGetter,
                pos: BlockPos,
                mob: Mob?,
                canFluidLog: Boolean,
            ): BlockPathTypes? {
                return if (canFluidLog) super.getBlockPathType(state, level, pos, mob, true) else null
            }

        }

        fun lavaLike(
            presets: CFluidPresets,
            modifier: (Properties) -> Unit = {},
        ) = object : FluidTypeWithClient(
            Properties.create()
                .descriptionId(presets.descriptionId)
                .canSwim(false)
                .canDrown(false)
                .pathType(BlockPathTypes.LAVA)
                .adjacentPathType(null)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                .lightLevel(15)
                .density(3000)
                .viscosity(6000)
                .temperature(1300)
                .motionScale(0.0023333333333333335)
                .also(modifier),
            FogType.LAVA,
            presets,
        ) {
            override fun motionScale(entity: Entity): Double {
                return if (entity.level().dimensionType().ultraWarm()) 0.007 else 0.0023333333333333335
            }

            override fun setItemMovement(entity: ItemEntity) {
                val vec3 = entity.deltaMovement
                entity.setDeltaMovement(
                    vec3.x * 0.95,
                    vec3.y + (if (vec3.y < 0.06) 5.0E-4f else 0.0f).toDouble(),
                    vec3.z * 0.95
                )
            }

        }
    }

    override fun initializeClient(consumer: Consumer<IClientFluidTypeExtensions>) {
        consumer.accept(
            object : IClientFluidTypeExtensions {
                private fun waterFog(renderDistance: Float, shape: FogShape, maxDistance: Float = 48F) {
                    RenderSystem.setShaderFogStart(-8.0f)
                    RenderSystem.setShaderFogEnd(min(maxDistance, renderDistance))
                    RenderSystem.setShaderFogShape(shape)
                }


                private fun lavaFog(camera: Camera, renderDistance: Float, shape: FogShape) {
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

                override fun modifyFogRender(
                    camera: Camera,
                    mode: FogRenderer.FogMode,
                    renderDistance: Float,
                    partialTick: Float,
                    nearDistance: Float,
                    farDistance: Float,
                    shape: FogShape,
                ) {
                    when (fogType) {
                        FogType.WATER -> waterFog(renderDistance, shape)
                        FogType.LAVA -> lavaFog(camera, renderDistance, shape)
                        else -> {}
                    }
                }

                val fogColor by lazy { presets.fogColor?.vecColor }
                override fun modifyFogColor(
                    camera: Camera,
                    partialTick: Float,
                    level: ClientLevel,
                    renderDistance: Int,
                    darkenWorldAmount: Float,
                    fluidFogColor: Vector3f,
                ): Vector3f = fogColor ?: fluidFogColor

                override fun getFlowingTexture() = presets.flowingTexture
                override fun getStillTexture() = presets.stillTexture
                override fun getOverlayTexture() = presets.overlayTexture
                override fun getRenderOverlayTexture(mc: Minecraft) = presets.underwaterTexture
                override fun getTintColor(): Int = presets.tintColor ?: super.tintColor
            }
        )
    }
}