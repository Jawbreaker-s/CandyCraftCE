package cn.jawbreakers.candycraftce.fabric.fluid

import cn.jawbreakers.candycraftce.fluid.CFluidProperties
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import com.mojang.blaze3d.shaders.FogShape
import net.minecraft.client.Camera
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.FogRenderer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.material.FlowingFluid
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import org.joml.Vector3f

abstract class CFlowingFluid(val properties: CFluidProperties) : FlowingFluid() {

    override fun beforeDestroyingBlock(level: LevelAccessor, pos: BlockPos, state: BlockState) {
        val be = if (state.hasBlockEntity()) level.getBlockEntity(pos) else null
        Block.dropResources(state, level, pos, be)
    }

    override fun canConvertToSource(level: Level): Boolean = false

    override fun getFlowing() = properties.flowing().get()
    override fun getSource() = properties.source().get()
    override fun getSlopeFindDistance(level: LevelReader) = properties.slopeFindDistance
    override fun getDropOff(level: LevelReader) = properties.levelDecreasePerBlock
    override fun getBucket(): Item = properties.bucket?.invoke()?.get() ?: Items.AIR
    override fun getTickDelay(level: LevelReader) = properties.tickRate
    override fun getExplosionResistance(): Float = properties.explosionResistance
    override fun createLegacyBlock(state: FluidState): BlockState {
        return properties.block().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state))
            ?: Blocks.AIR.defaultBlockState()
    }

    fun modifyFogColor(
        camera: Camera,
        partialTick: Float,
        level: ClientLevel,
        renderDistance: Int,
        darkenWorldAmount: Float,
        fluidFogColor: Vector3f,
    ): Vector3f? = properties.type.fogColor?.let(::Vector3f)

    fun modifyFogRender(
        camera: Camera,
        mode: FogRenderer.FogMode,
        renderDistance: Float,
        partialTick: Float,
        nearDistance: Float,
        farDistance: Float,
        shape: FogShape,
    ): Boolean {
        return properties.type.fogRenderType?.run {
            modifyFogRender(
                camera,
                mode,
                renderDistance,
                partialTick,
                nearDistance,
                farDistance,
                shape
            )
            true
        } ?: false
    }

    override fun canBeReplacedWith(
        state: FluidState,
        level: BlockGetter,
        pos: BlockPos,
        fluidIn: Fluid,
        direction: Direction,
    ): Boolean {
        // Based on the water implementation, may need to be overriden for mod fluids that shouldn't behave like water.
        return direction == Direction.DOWN && !isSame(fluidIn)
    }

    override fun isSame(fluid: Fluid): Boolean {
        return fluid == source || fluid == flowing
    }

    class Source(properties: CFluidProperties) : CFlowingFluid(properties) {
        override fun isSource(state: FluidState) = true
        override fun getAmount(state: FluidState): Int = 8
    }

    class Flowing(properties: CFluidProperties) : CFlowingFluid(properties) {
        override fun isSource(state: FluidState) = false

        init {
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7))
        }

        override fun createFluidStateDefinition(builder: StateDefinition.Builder<Fluid?, FluidState?>) {
            super.createFluidStateDefinition(builder)
            builder.add(LEVEL)
        }

        override fun getAmount(state: FluidState): Int = state.getValue(LEVEL)
    }
}
