package cn.jawbreakers.candycraftce.item

import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext

class CandiedCherryItem(properties: Properties) : Item(properties) {
    override fun useOn(context: UseOnContext): InteractionResult {
        //todo cherry block
//        val face = context.clickedFace
//        if (face == Direction.UP) return InteractionResult.PASS
//
        val level = context.level
//        val supportPos = context.clickedPos
//        val supportState = level.getBlockState(supportPos)
//        val hanging = face == Direction.DOWN && CherryBlock.isValidLeafSupport(supportState)
//        val sideMounted = face.axis.isHorizontal && CherryBlock.isValidSideSupport(supportState)
//        if (!hanging && !sideMounted) {
//            return InteractionResult.PASS
//        }
//
//        val placePos = supportPos.relative(face)
//        val state: BlockState = CCBlocks.CHERRY_BLOCK.get().defaultBlockState()
//            .setValue(CherryBlock.FACING, if (hanging) Direction.UP else face.opposite)
//        if (!level.getBlockState(placePos).canBeReplaced() || !state.canSurvive(level, placePos)) {
//            return InteractionResult.PASS
//        }
//
//        if (!level.isClientSide) {
//            level.setBlock(placePos, state, 11)
//            val sound = state.getSoundType(level, placePos, context.player)
//            level.playSound(
//                null,
//                placePos,
//                sound.placeSound,
//                SoundSource.BLOCKS,
//                (sound.getVolume() + 1.0f) / 2.0f,
//                sound.getPitch() * 0.8f
//            )
//        if (context.player == null || !context.player!!.abilities.instabuild) {
//            context.itemInHand.shrink(1)
//        }
//        }
//
        return InteractionResult.sidedSuccess(level.isClientSide)
    }
}
