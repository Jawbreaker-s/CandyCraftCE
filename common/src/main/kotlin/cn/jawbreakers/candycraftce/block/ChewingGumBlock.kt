package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.misc.EmblemHelper
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class ChewingGumBlock(properties: Properties) : Block(properties) {
    companion object {
        private val SHAPE: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0)
        fun isChewingGumNative(entity: Entity): Boolean {
            return false //TODO
            //		return entity.getType() == CCEntityTypes.BEETLE.get()
//				|| entity.getType() == CCEntityTypes.BOSS_BEETLE.get()
//				|| entity.getType() == CCEntityTypes.KING_BEETLE.get()
//				|| entity instanceof GummyBallEntity ball && ball.isBossBeetleProjectile();
        }

    }

    @Deprecated("Deprecated in Java")
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return SHAPE
    }

    @Deprecated("Deprecated in Java")
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (isChewingGumNative(entity)) {
            return
        }
        if (entity is Player && EmblemHelper.isChewingGumImmune(entity)) {
            return
        }
        val movement = entity.deltaMovement
        entity.setDeltaMovement(movement.x * 0.2, 0.0, movement.z * 0.2)
    }

}
