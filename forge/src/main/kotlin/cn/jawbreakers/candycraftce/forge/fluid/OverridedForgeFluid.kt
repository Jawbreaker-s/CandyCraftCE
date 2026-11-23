package cn.jawbreakers.candycraftce.forge.fluid

import cn.jawbreakers.candycraftce.fluid.CFluidPresets
import cn.jawbreakers.candycraftce.fluid.CandyFluidOverrides.overrideSpreadTo
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.minecraftforge.fluids.ForgeFlowingFluid

object OverridedForgeFluid {
    class Source(val presets: CFluidPresets, properties: Properties) : ForgeFlowingFluid.Source(properties) {
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
    }

    class Flowing(val presets: CFluidPresets, properties: Properties) : ForgeFlowingFluid.Flowing(properties) {
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
    }
}