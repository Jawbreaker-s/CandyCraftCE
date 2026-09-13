package cn.jawbreakers.candycraftce.fluid

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.LevelEvent
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FlowingFluid
import net.minecraft.world.level.material.FluidState

interface IFluidBehaviourOverrides {
    companion object {
        fun fizz(level: LevelAccessor, pos: BlockPos) = level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0)
        val ALL_DIRECTIONS = Direction.entries
    }

    /**
     * 主动过去把neighbour变成固体
     * 用于 岩浆（主动）->水 = 石头
     * @see [net.minecraft.world.level.material.LavaFluid.overrideSpreadTo]
     * @see [FlowingFluid.overrideSpreadTo]
     * */
    fun FlowingFluid.overrideSpreadTo(
        ref: CFluidReferences,
        level: LevelAccessor,
        pos: BlockPos,
        state: BlockState,
        direction: Direction,
        fluidState: FluidState,
        doSpread: (LevelAccessor, BlockPos, BlockState, Direction, FluidState) -> Unit,
    ) = doSpread(level, pos, state, direction, fluidState)

    /**
     * 检测被变成固体
     * 用于黑曜石、圆石等被动的检测
     * @see [net.minecraft.world.level.block.LiquidBlock.overrideShouldSpreadLiquid]
     * */
    fun LiquidBlock.overrideShouldSpreadLiquid(
        ref: CFluidReferences,
        level: LevelAccessor,
        pos: BlockPos,
        state: BlockState,
    ) = true

    fun LiquidBlock.overrideEntityInside(
        ref: CFluidReferences,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        entity: Entity,
    )
}