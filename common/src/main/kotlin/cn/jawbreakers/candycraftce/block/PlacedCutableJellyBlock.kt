package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CItems.fork
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.tags.ItemTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SwordItem
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import java.util.function.Supplier

class PlacedCutableJellyBlock(
    properties: Properties,
    private val sliceItem: Supplier<out Item?>,
) : PlacedJellyBlock(properties) {
    companion object {
        val CUTS: IntegerProperty = IntegerProperty.create("cuts", 0, 3)
        private val SHAPES = arrayOf<VoxelShape>(
            box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0),
            Shapes.or(
                box(2.0, 0.0, 2.0, 8.0, 6.0, 14.0),
                box(8.0, 0.0, 2.0, 14.0, 6.0, 8.0)
            ),
            box(2.0, 0.0, 2.0, 8.0, 6.0, 14.0),
            box(2.0, 0.0, 2.0, 8.0, 6.0, 8.0)
        )

        private fun isCuttingTool(stack: ItemStack): Boolean {
            return stack.`is`(fork.get()) || stack.`is`(ItemTags.SWORDS) || stack.item is SwordItem
        }
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(CUTS, 0))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        builder.add(CUTS)
    }

    @Deprecated("Deprecated in Java")
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return SHAPES[state.getValue(CUTS)]
    }
    

    @Deprecated("Deprecated in Java")
    override fun use(
        state: BlockState, level: Level, pos: BlockPos, player: Player,
        hand: InteractionHand, hit: BlockHitResult,
    ): InteractionResult {
        val tool = player.getItemInHand(hand)
        if (!isCuttingTool(tool)) {
            return InteractionResult.PASS
        }
        if (!level.isClientSide) {
            popResource(level, pos, ItemStack(sliceItem.get()))
            val cuts = state.getValue(CUTS)
            if (cuts >= 3) {
                level.removeBlock(pos, false)
            } else {
                level.setBlock(pos, state.setValue(CUTS, cuts + 1), UPDATE_ALL)
            }
            level.playSound(null, pos, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.BLOCKS, 0.8f, 1.15f)
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos)
            player.awardStat(Stats.ITEM_USED.get(tool.item))
            if (!player.abilities.instabuild && tool.isDamageableItem) {
                tool.hurtAndBreak(1, player) { entity ->
                    entity?.broadcastBreakEvent(hand)
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    override fun getDrops(state: BlockState, params: LootParams.Builder): List<ItemStack?> {
        //TODO may use loottable
        val cuts = state.getValue(CUTS)
        if (cuts == 0) {
            return super.getDrops(state, params)
        } else {
            return mutableListOf(ItemStack(sliceItem.get(), 4 - cuts))
        }
    }

}
