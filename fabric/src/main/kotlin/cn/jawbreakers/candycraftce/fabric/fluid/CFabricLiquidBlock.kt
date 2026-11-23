package cn.jawbreakers.candycraftce.fabric.fluid

import cn.jawbreakers.candycraftce.fluid.CFluidPresets
import cn.jawbreakers.candycraftce.fluid.CandyFluidOverrides.overrideEntityInside
import cn.jawbreakers.candycraftce.fluid.CandyFluidOverrides.overrideShouldSpreadLiquid
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FlowingFluid

class CFabricLiquidBlock(fluid: FlowingFluid, val presets: CFluidPresets, properties: Properties) :
    LiquidBlock(fluid, properties) {

    override fun getDescriptionId(): String = presets.descriptionId

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        presets.override?.let {
            overrideEntityInside(presets.references, state, level, pos, entity)
        } ?: super.entityInside(state, level, pos, entity)
    }

    override fun shouldSpreadLiquid(level: Level, pos: BlockPos, state: BlockState): Boolean {
        return presets.override?.let {
            return overrideShouldSpreadLiquid(presets.references, level, pos, state)
        } ?: super.shouldSpreadLiquid(level, pos, state)
    }
}