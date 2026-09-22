package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CBlocks
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import java.util.function.IntUnaryOperator
import kotlin.math.min

/**
 * Author [Bread_NiceCat](https://gitee.com/Bread_NiceCat)
 * Date 2023/1/20 14:29
 *
 * @param shapes stage->shape
 * @param stages 输入:[0,MAX_AGE],输出:[0,[shapes].size]
 */
open class CandyCropBlock(
    properties: Properties,
    protected val shapes: Array<VoxelShape>,
    protected val stages: IntUnaryOperator,
) : CandyPlantBlock(properties), ISugarTarget {
    init {
        registerDefaultState(stateDefinition.any().setValue(AGE, 0))
    }

    companion object {
        const val MAX_AGE: Int = 7
        val AGE: IntegerProperty = BlockStateProperties.AGE_7
        private val SHAPE_L4 = arrayOf(
            box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),  //0
            box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),  //3
            box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),  //5
            box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),  //7
        )


        fun createL4(properties: Properties): CandyCropBlock {
            return CandyCropBlock(properties, SHAPE_L4) {
                when (it) {
                    0, 1, 2 -> 0
                    3, 4 -> 1
                    5, 6 -> 2
                    else -> 3
                }
            }
        }
    }

    val maxStage: Int = shapes.size - 1
    fun getAge(b: BlockState): Int = b.getValue(AGE)
    fun getStage(b: BlockState): Int = stages.applyAsInt(getAge(b))


    @Deprecated("Deprecated in Java")
    override fun getShape(
        pState: BlockState,
        pLevel: BlockGetter,
        pPos: BlockPos,
        pContext: CollisionContext,
    ): VoxelShape = shapes[getStage(pState)]

    override fun canSurvive(pState: BlockState, pLevel: LevelReader, pPos: BlockPos): Boolean {
        return pLevel.getBlockState(pPos.below()).`is`(CBlocks.pudding_farmland.get())
                && ((pLevel.getRawBrightness(pPos, 0) >= 8 || pLevel.canSeeSky(pPos)) && super.canSurvive(
            pState,
            pLevel,
            pPos
        ))
    }


    //[VanillaCopy]net.minecraft.world.level.block.CropBlock
    @Deprecated("Deprecated in Java")
    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        val age = this.getAge(state)
        if (level.getRawBrightness(pos, 0) >= 9
            && this.getAge(state) < MAX_AGE
            && random.nextInt((25.0f / (getGrowthSpeed(this, level, pos))).toInt() + 1) == 0
        ) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2)
        }
    }

    //[VanillaCopy]net.minecraft.world.level.block.CropBlock
    fun getGrowthSpeed(block: Block, level: BlockGetter, pos: BlockPos): Float {
        val bl2: Boolean
        var f = 1.0f
        val blockPos = pos.below()
        for (i in -1..1) {
            for (j in -1..1) {
                var g = 0.0f
                val blockState = level.getBlockState(blockPos.offset(i, 0, j))
                if (blockState.`is`(CBlocks.pudding_farmland.get())) {
                    g = 1.0f
                    if (blockState.getValue(CandyFarmlandBlock.MOISTURE) > 0) {
                        g = 3.0f
                    }
                }
                if (i != 0 || j != 0) {
                    g /= 4.0f
                }
                f += g
            }
        }
        val blockPos2 = pos.north()
        val blockPos3 = pos.south()
        val blockPos4 = pos.west()
        val blockPos5 = pos.east()
        val bl = level.getBlockState(blockPos4).`is`(block) || level.getBlockState(blockPos5).`is`(block)
        bl2 = level.getBlockState(blockPos2).`is`(block) || level.getBlockState(blockPos3).`is`(block)
        if (bl && bl2) {
            f /= 2.0f
        } else {
            val bl32 = level.getBlockState(blockPos4.north()).`is`(block)
                    || level.getBlockState(blockPos5.north()).`is`(block)
                    || level.getBlockState(blockPos5.south()).`is`(block)
                    || level.getBlockState(blockPos4.south()).`is`(block)
            if (bl32) {
                f /= 2.0f
            }
        }
        return f
    }

    override fun isRandomlyTicking(pState: BlockState): Boolean {
        return getAge(pState) < MAX_AGE
    }

    override fun createBlockStateDefinition(pBuilder: StateDefinition.Builder<Block?, BlockState?>) {
        pBuilder.add(AGE)
    }

    override fun isValidSugarTarget(level: LevelReader, pos: BlockPos, state: BlockState, isClient: Boolean): Boolean {
        return getAge(state) < MAX_AGE
    }


    override fun performSugar(level: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) {
        val age = min(getAge(state) + Mth.nextInt(level.random, 2, 5), MAX_AGE)
        level.setBlock(pos, state.setValue(AGE, age), 2)
    }

    override fun isSugarSuccess(level: Level, random: RandomSource, pos: BlockPos, state: BlockState): Boolean {
        return random.nextFloat() < 0.40
    }


}