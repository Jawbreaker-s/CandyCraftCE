package cn.jawbreakers.candycraftce.fabric.fluid

import cn.jawbreakers.candycraftce.fluid.CFluidPresets
import cn.jawbreakers.candycraftce.fluid.CandyFluidOverrides.overrideSpreadTo
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import rc55.mc.rfapi.fluid.ExtendedFluid
import rc55.mc.rfapi.fluid.FluidReference
import rc55.mc.rfapi.fluid.FluidSettings
import java.util.function.BiFunction

class OverridedExtendedFluid(
    settings: FluidSettings,
    reference: FluidReference<OverridedExtendedFluid>,
    source: Boolean,
    val presets: CFluidPresets,
) : ExtendedFluid(settings, reference, source) {
    override fun spreadTo(
        level: LevelAccessor,
        pos: BlockPos,
        blockState: BlockState,
        direction: Direction,
        fluidState: FluidState,
    ) {
        presets.override?.let {
            overrideSpreadTo(
                presets.references,
                level,
                pos,
                blockState,
                direction,
                fluidState
            ) { level, pos, blockState, direction, fluidState ->
                super.spreadTo(level, pos, blockState, direction, fluidState)
            }
        } ?: super.spreadTo(level, pos, blockState, direction, fluidState)

    }

    companion object {
        fun ofStill(presets: CFluidPresets): BiFunction<FluidSettings, FluidReference<OverridedExtendedFluid>, OverridedExtendedFluid> {
            return BiFunction { settings, reference -> OverridedExtendedFluid(settings, reference, true, presets) }
        }

        fun ofFlowing(presets: CFluidPresets): BiFunction<FluidSettings, FluidReference<OverridedExtendedFluid>, OverridedExtendedFluid> {
            return BiFunction { settings, reference -> OverridedExtendedFluid(settings, reference, false, presets) }
        }
    }

}