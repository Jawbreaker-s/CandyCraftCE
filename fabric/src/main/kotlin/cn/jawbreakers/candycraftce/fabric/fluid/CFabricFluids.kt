package cn.jawbreakers.candycraftce.fabric.fluid

import cn.jawbreakers.candycraftce.fluid.CFluidPresets
import cn.jawbreakers.candycraftce.fluid.CFluidReferences
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.ICPlatformFluids
import cn.jawbreakers.candycraftce.utils.ICPlatformFluids.Companion.FLOWING_SUFFIX
import cn.jawbreakers.candycraftce.utils.ICPlatformFluids.Companion.SOURCE_SUFFIX
import cn.jawbreakers.candycraftce.utils.registry.EntryWrapper.Companion.wrapAsEntry
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.FlowingFluid
import rc55.mc.rfapi.fluid.FluidReference
import rc55.mc.rfapi.fluid.FluidRegistry
import rc55.mc.rfapi.fluid.FluidSettings
import rc55.mc.rfapi.fluid.FluidSettings.ColorSettings.FogType.LAVA

private fun registerSimple(
    presets: CFluidPresets,
    settings: FluidSettings.Builder,
    blockFactory: FlowingFluid.(BlockBehaviour.Properties) -> LiquidBlock = { CFabricLiquidBlock(this, presets, it) },
): FluidReference<OverridedExtendedFluid> {
    val id = presets.name.modLoc()
    return FluidRegistry.register(
        id.withSuffix(SOURCE_SUFFIX),
        id.withSuffix(FLOWING_SUFFIX),
        id,
        OverridedExtendedFluid.ofStill(presets),
        OverridedExtendedFluid.ofFlowing(presets),
        blockFactory
    ) { settings }
}

object CFabricFluids : ICPlatformFluids {
    val fluids: MutableMap<FlowingFluid, CFluidReferences> = mutableMapOf()
    override fun createBucketItem(ref: CFluidReferences, properties: Item.Properties): BucketItem =
        BucketItem(ref.source.get(), properties)

    private fun FluidSettings.Builder.apply(presets: CFluidPresets): FluidSettings.Builder {
        bucket { presets.bucket?.invoke()?.get() ?: Items.AIR }
        color(
            FluidSettings.ColorSettings.builder()
                .fog(LAVA, presets.fogColor!!)
                .mapColor(presets.mapColor)
        )
        tickRate(presets.tickRate)
        return this
    }

    private fun FluidReference<*>.wrap(presets: CFluidPresets): CFluidReferences {
        extraSettings(presets)
        val cref = CFluidReferences(
            presets,
            still.wrapAsEntry(stillId),
            flowing.wrapAsEntry(flowingId),
            (block as LiquidBlock).wrapAsEntry(blockId)
        )
        fluids[still] = cref
        fluids[flowing] = cref
        return cref
    }

    private fun FluidReference<*>.extraSettings(preset: CFluidPresets) {
        CPlatformUtils.ifClient {
            FluidRenderHandlerRegistry.INSTANCE.register(
                still, flowing,
                SimpleFluidRenderHandler(
                    preset.stillTexture,
                    preset.flowingTexture,
                    preset.overlayTexture,
                    preset.tintColor
                )
            )
            if (preset.isTransparent) {
                BlockRenderLayerMap.INSTANCE.putFluids(
                    RenderType.translucent(),
                    still, flowing
                )
            }
        }
    }

    override fun registerGrenadine(presets: CFluidPresets): CFluidReferences {
        return registerSimple(
            presets,
            FluidSettings.waterLike()
                .isInfinite(false)
                .apply(presets)
        ).wrap(presets)
    }

    override fun registerCaramel(presets: CFluidPresets): CFluidReferences {
        return registerSimple(
            presets,
            FluidSettings.waterLike()
                .isInfinite(false)
                .apply(presets)
        ).wrap(presets)
    }

    override fun registerLiquidChocolate(presets: CFluidPresets): CFluidReferences {
        return registerSimple(
            presets,
            FluidSettings.lavaLike()
                .isInfinite(false)
                .apply(presets)
        ).wrap(presets)
    }

    override fun registerLiquidCandy(presets: CFluidPresets): CFluidReferences {
        return registerSimple(
            presets,
            FluidSettings.lavaLike()
                .isInfinite(false)
                .apply(presets)
        ).wrap(presets)
    }

}