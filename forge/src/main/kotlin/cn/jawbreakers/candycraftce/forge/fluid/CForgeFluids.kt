package cn.jawbreakers.candycraftce.forge.fluid

import cn.jawbreakers.candycraftce.fluid.CFluidProperties
import cn.jawbreakers.candycraftce.fluid.CFluidType
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.whenInitialized
import cn.jawbreakers.candycraftce.utils.registry.Entry
import com.mojang.blaze3d.shaders.FogShape
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.FogRenderer
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions
import net.minecraftforge.fluids.FluidType
import net.minecraftforge.fluids.ForgeFlowingFluid
import org.joml.Vector3f
import java.util.function.Consumer

val candyFluidTypes = mutableMapOf<CFluidType, Entry<out FluidType>>()
val propertiesCache = mutableMapOf<CFluidProperties, ForgeFlowingFluid.Properties>()

fun CFluidProperties.asForge(): ForgeFlowingFluid.Properties {
    return propertiesCache.computeIfAbsent(this) {
        ifClient {
            whenInitialized {
                if (type.isTransparent) {
                    ItemBlockRenderTypes.setRenderLayer(source().get(), RenderType.translucent())
                    ItemBlockRenderTypes.setRenderLayer(flowing().get(), RenderType.translucent())
                }
            }
        }
        ForgeFlowingFluid.Properties(
            type::asForge,
            { source().get() },
            { flowing().get() })
            .apply {
                if (bucket != null) bucket(bucket!!())
                block(block())
                slopeFindDistance(slopeFindDistance)
                levelDecreasePerBlock(levelDecreasePerBlock)
                explosionResistance(explosionResistance)
                tickRate(tickRate)
            }
    }
}

val fluidTypeCache = mutableMapOf<CFluidType, FluidType>()
fun CFluidType.asForge(): FluidType {
    return fluidTypeCache.computeIfAbsent(this) {
        object : FluidType(
            Properties.create()
                .apply {
                    canSwim(canSwim)
                    lightLevel(lightLevel)
                    density(density)
                    temperature(temperature)
                    viscosity(viscosity)
                    rarity(rarity)
                    motionScale(motionScale)
                    canSwim(canSwim)
                    canPushEntity(canPushEntity)
                    canDrown(canDrown)
                }
        ) {
            override fun initializeClient(consumer: Consumer<IClientFluidTypeExtensions>) {
                consumer.accept(object : IClientFluidTypeExtensions {
                    override fun getTintColor(): Int = this@asForge.tintColor
                    override fun getFlowingTexture(): ResourceLocation? = this@asForge.flowingTexture
                    override fun getStillTexture(): ResourceLocation? = this@asForge.stillTexture

                    val renderOverlay by lazy { this@asForge.renderOverlayTexture?.withPath { "textures/$it.png" } }
                    override fun getRenderOverlayTexture(mc: Minecraft): ResourceLocation? = renderOverlay
                    override fun getOverlayTexture(): ResourceLocation? = this@asForge.overlayTexture

                    override fun modifyFogColor(
                        camera: Camera,
                        partialTick: Float,
                        level: ClientLevel,
                        renderDistance: Int,
                        darkenWorldAmount: Float,
                        fluidFogColor: Vector3f,
                    ): Vector3f {
                        return this@asForge.modifyFogColor(
                            camera,
                            partialTick,
                            level,
                            renderDistance,
                            darkenWorldAmount,
                            fluidFogColor
                        )
                            ?: super.modifyFogColor(
                                camera,
                                partialTick,
                                level,
                                renderDistance,
                                darkenWorldAmount,
                                fluidFogColor
                            )
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
                        fogRenderType?.modifyFogRender(
                            camera,
                            mode,
                            renderDistance,
                            partialTick,
                            nearDistance,
                            farDistance,
                            shape
                        )
                            ?: super.modifyFogRender(
                                camera,
                                mode,
                                renderDistance,
                                partialTick,
                                nearDistance,
                                farDistance,
                                shape
                            )
                    }
                })
            }
        }
    }
}