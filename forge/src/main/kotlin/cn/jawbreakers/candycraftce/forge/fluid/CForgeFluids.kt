package cn.jawbreakers.candycraftce.forge.fluid

import cn.jawbreakers.candycraftce.fluid.CFluidPresets
import cn.jawbreakers.candycraftce.fluid.CFluidReferences
import cn.jawbreakers.candycraftce.forge.CandyCraftCEForge
import cn.jawbreakers.candycraftce.forge.ForgeEntry.Companion.asEntry
import cn.jawbreakers.candycraftce.forge.fluid.FluidTypeWithClient.Companion.lavaLike
import cn.jawbreakers.candycraftce.forge.fluid.FluidTypeWithClient.Companion.waterLike
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.whenInitialized
import cn.jawbreakers.candycraftce.utils.ICPlatformFluids
import cn.jawbreakers.candycraftce.utils.registry.Entry
import cn.jawbreakers.candycraftce.utils.registry.LateInitAccessor
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.FlowingFluid
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction
import net.minecraftforge.fluids.FluidType
import net.minecraftforge.fluids.ForgeFlowingFluid
import net.minecraftforge.registries.RegistryObject

object CForgeFluids : ICPlatformFluids {
    private val fluid = CandyCraftCEForge.instance.fluid
    private val fluidType = CandyCraftCEForge.instance.fluidType
    private val blocks = CandyCraftCEForge.instance.blocks

    private fun registerFluid(
        type: RegistryObject<out FluidType>,
        presets: CFluidPresets,
        properties: BlockBehaviour.Properties = liquid(presets.mapColor),
    ): CFluidReferences {
        var source by LateInitAccessor<Entry<out FlowingFluid>>()
        var flowing by LateInitAccessor<Entry<out FlowingFluid>>()
        var block by LateInitAccessor<Entry<out LiquidBlock>>()
        val fluidProperties = ForgeFlowingFluid.Properties(
            type, { source.get() }, { flowing.get() }
        ).block { block.get() }
            .tickRate(presets.tickRate)
            .bucket { presets.references.getBucket() }
        source = fluid.register("${presets.name}_source") {
            OverridedForgeFluid.Source(presets, fluidProperties)
        }.asEntry()
        flowing = fluid.register("${presets.name}_flowing") {
            OverridedForgeFluid.Flowing(presets, fluidProperties)
        }.asEntry()
        block = blocks.register(presets.name) {
            CForgeLiquidBlock(source, presets, properties)
        }.asEntry()
        return CFluidReferences(presets, source, flowing, block).also(::extraSettings)
    }

    private fun extraSettings(ref: CFluidReferences) {
        whenInitialized {
            if (ref.presets.isTransparent) {
                ItemBlockRenderTypes.setRenderLayer(ref.source.get(), RenderType.translucent())
                ItemBlockRenderTypes.setRenderLayer(ref.flowing.get(), RenderType.translucent())
            }
        }
    }

    fun liquid(mapColor: MapColor, light: Int = 0, randomTicks: Boolean = false): BlockBehaviour.Properties =
        BlockBehaviour.Properties.of()
            .replaceable()
            .noCollission()
            .strength(100.0F)
            .pushReaction(PushReaction.DESTROY)
            .noLootTable()
            .liquid()
            .sound(SoundType.EMPTY)
            .lightLevel { light }
            .mapColor(mapColor)
            .apply { if (randomTicks) randomTicks() }


    override fun createBucketItem(
        ref: CFluidReferences,
        properties: Item.Properties,
    ): BucketItem {
        return BucketItem(ref.source, properties)
    }

    override fun registerGrenadine(presets: CFluidPresets): CFluidReferences {
        val type = fluidType.register(presets.name) {
            waterLike(presets)
        }
        return registerFluid(type, presets)
    }

    override fun registerCaramel(presets: CFluidPresets): CFluidReferences {
        val type = fluidType.register(presets.name) {
            waterLike(presets)
        }
        return registerFluid(type, presets)
    }

    override fun registerLiquidChocolate(presets: CFluidPresets): CFluidReferences {
        val type = fluidType.register(presets.name) {
            lavaLike(presets)
        }
        return registerFluid(type, presets)
    }

    override fun registerLiquidCandy(presets: CFluidPresets): CFluidReferences {
        val type = fluidType.register(presets.name) {
            lavaLike(presets)
        }
        return registerFluid(type, presets)
    }
}


