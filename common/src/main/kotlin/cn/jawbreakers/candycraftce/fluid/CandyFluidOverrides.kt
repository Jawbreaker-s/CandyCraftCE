package cn.jawbreakers.candycraftce.fluid

import cn.jawbreakers.candycraftce.fluid.IFluidBehaviourOverrides.Companion.ALL_DIRECTIONS
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import cn.jawbreakers.candycraftce.registry.CFluidTags
import cn.jawbreakers.candycraftce.registry.CFluids
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.FluidTags
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Blocks.COBBLESTONE
import net.minecraft.world.level.block.Blocks.OBSIDIAN
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FlowingFluid
import net.minecraft.world.level.material.FluidState

object CandyFluidOverrides : IFluidBehaviourOverrides {
    override fun LiquidBlock.overrideEntityInside(
        ref: CFluidReferences,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        entity: Entity,
    ) {
        if (!level.isClientSide) {
            if (ref == CFluids.liquid_candy || ref == CFluids.liquid_chocolate) {
                if (entity is LivingEntity && entity.tickCount % 10 == 0) {
                    entity.hurt(level.damageSources().hotFloor(), 2f)
                    entity.setSecondsOnFire(15)
                }
            }
        }
    }


    override fun FlowingFluid.overrideSpreadTo(
        ref: CFluidReferences,
        level: LevelAccessor,
        pos: BlockPos,
        state: BlockState,
        direction: Direction,
        fluidState: FluidState,
        doSpread: (LevelAccessor, BlockPos, BlockState, Direction, FluidState) -> Unit,
    ) {
        //这两个是冷液体，会让岩浆凝固
        if (ref == CFluids.caramel || ref == CFluids.grenadine) {
            checkNeighbour(level, pos, ::isVanillaLava) { np, n ->
                level.setBlock(np, (if (n.isSource) OBSIDIAN else COBBLESTONE).defaultBlockState(), 3)
            }
        }
        if (ref == CFluids.grenadine) {
            checkNeighbour(level, pos, ::isVanillaWater) { np, n ->
                level.setBlock(np, CBlocks.fragile_grenadine_ice.defaultBlockState(), 3)
            }
        }

        doSpread(level, pos, state, direction, fluidState)
    }

    override fun LiquidBlock.overrideShouldSpreadLiquid(
        ref: CFluidReferences,
        level: LevelAccessor,
        pos: BlockPos,
        state: BlockState,
    ): Boolean {
        val fluidState = state.fluidState
        val isSource = fluidState.isSource
        return when (ref) {
            CFluids.caramel -> !checkNeighbour(level, pos, ::isHotLiquid, ALL_DIRECTIONS) { _, _ ->
                if (isSource) level.setBlock(pos, CBlocks.caramel_block.defaultBlockState(), 3)
            }

            CFluids.grenadine -> !checkNeighbour(level, pos, ::isHotLiquid, ALL_DIRECTIONS) { _, _ ->
                if (isSource) level.setBlock(pos, CBlocks.grenadine_ice.defaultBlockState(), 3)
            }

            CFluids.liquid_chocolate -> !checkNeighbour(level, pos, ::isColdLiquid, ALL_DIRECTIONS) { _, _ ->
                if (isSource) level.setBlock(pos, CBlocks.milk_chocolate_block.defaultBlockState(), 3)
            }

            CFluids.liquid_candy -> !checkNeighbour(level, pos, ::isColdLiquid, ALL_DIRECTIONS) { _, _ ->
                if (isSource) level.setBlock(pos, CBlocks.pink_crystallized_sugar.defaultBlockState(), 3)
            }

            else -> true
        }
    }

    /**
     * @return if neighbor exists
     * */
    private inline fun checkNeighbour(
        level: LevelAccessor,
        pos: BlockPos,
        predicate: (FluidState) -> Boolean,
        directions: Iterable<Direction> = LiquidBlock.POSSIBLE_FLOW_DIRECTIONS,
        then: (BlockPos, FluidState) -> Unit = { _, _ -> },
    ): Boolean {
        var flag = false
        directions.forEach { direction ->
            val neighborPos = pos.relative(direction)
            val neighbor = level.getBlockState(neighborPos).fluidState
            if (predicate(neighbor)) {
                flag = true
                then(neighborPos, neighbor)
            }
        }
        return flag
    }


    private fun isHotLiquid(state: FluidState): Boolean {
        return !state.isEmpty && (isVanillaLava(state)
                || state.`is`(CFluidTags.liquid_candy)
                || state.`is`(CFluidTags.liquid_chocolate))
    }

    private fun isColdLiquid(state: FluidState): Boolean {
        return !state.isEmpty && (state.`is`(FluidTags.WATER)
                || state.`is`(CFluidTags.caramel)
                || state.`is`(CFluidTags.grenadine))
    }

    private fun isVanillaLava(state: FluidState): Boolean {
        return !state.isEmpty && state.`is`(FluidTags.LAVA)
    }

    private fun isVanillaWater(state: FluidState): Boolean {
        return !state.isEmpty && state.`is`(FluidTags.WATER)
    }
}