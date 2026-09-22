package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * Created in 2024/2/18 13:13
 * Project: candycraftce
 *
 * Author [Bread_NiceCat](https://github.com/BreadNiceCat)
 *
 *
 */
class LollipopStemBlock(properties: Properties) : CandyCropBlock(properties, shapes, ::stage) {
    companion object {
        val shapes = arrayOf<VoxelShape>(
            box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
            box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
            Shapes.block()
        )

        fun stage(age: Int): Int {
            return when (age) {
                0, 1, 2, 3 -> 0
                4, 5, 6 -> 1
                else -> 2
            }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (getAge(state) < MAX_AGE) {
            super.randomTick(state, level, pos, random)
        } else {
            val above: BlockPos = pos.above()
            if (level.getBlockState(above).isAir) {
                if (random.nextFloat() < (25f / getGrowthSpeed(this, level, pos))) {
                    level.setBlockAndUpdate(above, CBlocks.lollipop_fruit.defaultBlockState())
                }
            }
        }
    }

    override fun getCloneItemStack(level: BlockGetter, pos: BlockPos, state: BlockState): ItemStack {
        return if (state.getValue(AGE) == MAX_AGE) {
            asItem().defaultInstance
        } else {
            CItems.lollipop_seeds.defaultInstance
        }
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        return if (context.itemInHand.`is`(CItems.lollipop_stem.get())) {
            defaultBlockState().setValue(AGE, MAX_AGE)
        } else {
            super.getStateForPlacement(context)
        }
    }

    override fun isRandomlyTicking(pState: BlockState): Boolean = pState.getValue(AGE) < MAX_AGE

    override fun asItem() = CItems.lollipop_stem.get()
}
