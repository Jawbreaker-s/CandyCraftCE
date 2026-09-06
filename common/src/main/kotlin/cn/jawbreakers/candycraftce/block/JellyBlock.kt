package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.mixin_stub.IPurpleJellyStuckEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import kotlin.math.abs

class JellyBlock(val type: JellyType, properties: Properties) : SameBlockCullBlock(properties) {
    private val shape: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 15.92, 16.0)


    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun getCollisionShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ): VoxelShape {
        return if (type.isFull) super.getCollisionShape(state, level, pos, context) else shape
    }

    override fun fallOn(level: Level, state: BlockState, pos: BlockPos, entity: Entity, fallDistance: Float) {
        if (type.resetFall) {
            if (entity is LivingEntity) {
                entity.resetFallDistance()
                if (type.stuckIn) {
                    (entity as IPurpleJellyStuckEntity).`candycraftce$setPurpleJellyStuck`()
                }
                return
            }
        }
        super.fallOn(level, state, pos, entity, fallDistance * 0.5f)
    }

    @Deprecated("Deprecated in Java")
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        //full后无法inside
        if (entity is LivingEntity && !entity.isShiftKeyDown) {
            val movement = entity.deltaMovement
            //必须踩到果冻里才行
            if (movement.y <= 0.0) {
                if (type.isDisturbing) {
                    entity.setDeltaMovement(
                        if (abs(movement.x) < 0.25) movement.x * 4.0 else movement.x,
                        movement.y + type.jump,
                        if (abs(movement.z) < 0.25) movement.z * 4.0 else movement.z
                    )
                } else {
                    entity.setDeltaMovement(movement.x, movement.y + type.jump, movement.z)
                }
                if (type.resetFall) entity.resetFallDistance()
                entity.hasImpulse = true
            }
        }
    }

}

/**
 * @param jump: 跳跃高度
 * @param resetFall: 落地时是否重置落距
 * @param isFull: 是否是full方块, full方块启用时可能导致后面的参数不生效
 * @param stuckIn: 摔落到其中时是否会卡住
 * @param isDisturbing: 是否扰动xz轴
 * */
enum class JellyType(
    val jump: Double,
    val resetFall: Boolean = false,
    val isFull: Boolean = false,
    //full后会忽略后面的参数
    val stuckIn: Boolean = false,
    val isDisturbing: Boolean = false,
) {
    NONE(0.0, isFull = true),
    PURPLE(2.1, resetFall = true, stuckIn = true),
    BLUE(-1.0, resetFall = true, isFull = true),
    YELLOW(1.0),
    GREEN(2.0),
    RED(4.0)
}