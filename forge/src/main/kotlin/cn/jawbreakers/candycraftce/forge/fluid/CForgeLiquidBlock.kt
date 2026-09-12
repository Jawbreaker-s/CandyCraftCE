package cn.jawbreakers.candycraftce.forge.fluid

import cn.jawbreakers.candycraftce.fluid.CFluidPresets
import cn.jawbreakers.candycraftce.fluid.CandyFluidOverrides.overrideEntityInside
import cn.jawbreakers.candycraftce.fluid.CandyFluidOverrides.overrideShouldSpreadLiquid
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FlowingFluid
import java.util.function.Supplier

class CForgeLiquidBlock(
    source: Supplier<out FlowingFluid>,
    val presets: CFluidPresets,
    properties: Properties,
) : LiquidBlock(source, properties) {
    override fun getDescriptionId(): String = fluid.fluidType.descriptionId

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        presets.override?.let {
            overrideEntityInside(presets.references, state, level, pos, entity)
        } ?: super.entityInside(state, level, pos, entity)
    }

    override fun neighborChanged(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        block: Block,
        fromPos: BlockPos,
        isMoving: Boolean,
    ) {
        if (presets.override?.let {
                overrideShouldSpreadLiquid(presets.references, level, pos, state)
            } ?: true) {
            super.neighborChanged(state, level, pos, block, fromPos, isMoving)
        }
    }

    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
        if (presets.override?.let {
                overrideShouldSpreadLiquid(presets.references, level, pos, state)
            } ?: true) {
            super.onPlace(state, level, pos, oldState, isMoving)
        }
    }

}