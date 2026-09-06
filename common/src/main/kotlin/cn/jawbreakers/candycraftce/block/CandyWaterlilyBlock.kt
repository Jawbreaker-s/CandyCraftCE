package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.WaterlilyBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class CandyWaterlilyBlock(private val flower: Boolean, properties: Properties) : WaterlilyBlock(properties) {
    @Deprecated("Deprecated in Java")
    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (!flower && level.getRawBrightness(pos.above(), 0) >= 9
            && random.nextInt(80) == 0
        ) {
            level.setBlockAndUpdate(pos, CBlocks.marshmallow_slice_flower.defaultBlockState())
        }
    }

    override fun isRandomlyTicking(state: BlockState): Boolean = !flower

    @Deprecated("Deprecated in Java")
    override fun use(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult,
    ): InteractionResult {
        if (!flower) {
            val stack = player.getItemInHand(hand)
            //加花
            if (stack.`is`(CItems.marshmallow_flower.get())) {
                if (!level.isClientSide) {
                    stack.shrink(1)
                    level.setBlockAndUpdate(pos, CBlocks.marshmallow_slice_flower.defaultBlockState())
                }
                return InteractionResult.sidedSuccess(level.isClientSide)
            }
        } else {
            //拿花
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, CBlocks.marshmallow_slice.defaultBlockState())
                popResource(level, pos, CItems.marshmallow_flower.defaultInstance)
            }
            return InteractionResult.sidedSuccess(level.isClientSide)
        }
        return InteractionResult.PASS
    }


}
