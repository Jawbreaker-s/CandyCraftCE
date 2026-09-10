package cn.jawbreakers.candycraftce.fabric.fluid

import cn.jawbreakers.candycraftce.utils.PlatformFluid
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FlowingFluid

class FabricLiquidBlock(
    fluid: FlowingFluid,
    properties: Properties,
    val overrides: PlatformFluid.LiquidOverrides? = null,
) : LiquidBlock(fluid, properties) {

    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, movedByPiston: Boolean) {
        if (overrides?.onPlace(state, level, pos, oldState, movedByPiston) ?: false) return
        super.onPlace(state, level, pos, oldState, movedByPiston)
    }

    override fun neighborChanged(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        neighborBlock: Block,
        neighborPos: BlockPos,
        movedByPiston: Boolean,
    ) {
        if (overrides?.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston) ?: false) return
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (overrides?.entityInside(state, level, pos, entity) ?: false) return
        super.entityInside(state, level, pos, entity)
    }

    override fun getDescriptionId(): String? = (fluid as? CFlowingFluid)?.properties?.type?.descriptionId
        ?: super.descriptionId
}