package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.misc.multiblocks.caramel_portal.PortalConfig
import cn.jawbreakers.candycraftce.misc.multiblocks.caramel_portal.VectorPortalShape
import cn.jawbreakers.candycraftce.misc.multiblocks.caramel_portal.axis2pipe2
import cn.jawbreakers.candycraftce.registry.CBlockTags
import cn.jawbreakers.candycraftce.registry.CBlockTags.candy_portal
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CFluidTags
import cn.jawbreakers.candycraftce.registry.CParticles
import cn.jawbreakers.candycraftce.registry.worldgen.CLevels
import cn.jawbreakers.candycraftce.utils.AxisSet
import cn.jawbreakers.candycraftce.utils.CUtils.instance
import cn.jawbreakers.candycraftce.utils.CUtils.use
import cn.jawbreakers.candycraftce.utils.MCTimeUnit.Companion.second
import cn.jawbreakers.candycraftce.utils.UsedByMixin
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Direction.Axis
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * Created in 2023/12/31 9:43
 * Project: candycraftce
 * 
 * Author [Bread_NiceCat](https://github.com/Bread-Nicecat)

 */
class CaramelPortalBlock(properties: Properties) : Block(properties) {
    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(X, true)
                .setValue(Y, false)
                .setValue(Z, false)
        )
    }

    private fun getShapeIndex(state: BlockState): Int {
        var flag = 0
        if (state.getValue(X)) flag = flag or 1
        if (state.getValue(Y)) flag = flag or 2
        if (state.getValue(Z)) flag = flag or 4
        return flag
    }

    @Deprecated("Deprecated in Java")
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return shapes[getShapeIndex(state)]
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        level: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos,
    ): BlockState {
        if (!neighborState.`is`(candy_portal)) {
            //两个方块之间未连接
            val pipe2: Array<Axis> = axis2pipe2(direction.axis)
            for (axis in pipe2) {
                if (state.getValue(axis2Property(axis))) return Blocks.AIR.defaultBlockState()
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos)
    }


    @Deprecated("Deprecated in Java")
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (level.isClientSide()) return
        //		if (entity instanceof ItemEntity itemEntity) {
//			ItemStack stack = itemEntity.getItem();
//			itemEntity.setItem(ItemStack.EMPTY);
        //交易
//			Vec3 delta = itemEntity.getDeltaMovement();
//			for (CaramelPortalRecipe r : RecipeHelper.getAllRecipesFor(CCManagerRecipe.caramel_portal_type.get())) {
//				if (r.matches(stack, level)) {
//					ItemStack result = r.assemble(stack);
//					level.addFreshEntity(new ItemEntity(level, pPos.getX(), pPos.getY(), pPos.getZ(), result, -delta.x(), delta.y(), -delta.z()));
//					return;
//				}
//			}
//		}else
        if (entity.isAlive && !entity.isPassenger && !entity.isVehicle && entity.canChangeDimensions()) {
            //传送
            val destination = getDestination(level, entity)
            if (destination != null) {
                val dest = level.server?.getLevel(destination)
                if (dest != null) {
                    if (entity is LivingEntity) {
                        //cn.breadnicecat.candycraftce.mixin.MixinEntity#findDimensionEntryPoint
                        val newEntity = entity.changeDimension(dest)
                        if (newEntity is LivingEntity) {
                            newEntity.addEffect(
                                MobEffects.DAMAGE_RESISTANCE.instance(10.second, 5)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(
                pos.x.toDouble() + 0.5,
                pos.y.toDouble() + 0.5,
                pos.z.toDouble() + 0.5,
                SoundEvents.PORTAL_AMBIENT,
                SoundSource.BLOCKS,
                0.5f,
                random.nextFloat() * 0.4f + 0.8f,
                false
            )
        }
        repeat(4) {
            var d = pos.x.toDouble() + random.nextDouble()
            val e = pos.y.toDouble() + random.nextDouble()
            var f = pos.z.toDouble() + random.nextDouble()
            var g = (random.nextFloat().toDouble() - 0.5) * 0.5
            val h = (random.nextFloat().toDouble() - 0.5) * 0.5
            var j = (random.nextFloat().toDouble() - 0.5) * 0.5
            val k = random.nextInt(2) * 2 - 1
            if (level.getBlockState(pos.west()).`is`(candy_portal)
                || level.getBlockState(pos.east()).`is`(candy_portal)
            ) {
                f = pos.z.toDouble() + 0.5 + 0.25 * k.toDouble()
                j = (random.nextFloat() * 2.0f * k.toFloat()).toDouble()
            } else {
                d = pos.x.toDouble() + 0.5 + 0.25 * k.toDouble()
                g = (random.nextFloat() * 2.0f * k.toFloat()).toDouble()
            }
            level.addParticle(CParticles.caramel_portal_particle_type.get(), d, e, f, g, h, j)
        }
    }

    /**
     * @return null, 如果无法传送
     */
    private fun getDestination(level: Level, entity: Entity): ResourceKey<Level>? {
        val ori = level.dimension()
        if (ori === Level.OVERWORLD) {
            return CLevels.candyland
        } else if (ori === CLevels.candyland) {
            return Level.OVERWORLD
        }
        return null
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        builder.add(X, Y, Z)
    }

    private fun axis2Property(axis: Axis) = when (axis) {
        Axis.X -> X
        Axis.Y -> Y
        Axis.Z -> Z
    }

    companion object {
        val X: BooleanProperty = BooleanProperty.create("x")
        val Y: BooleanProperty = BooleanProperty.create("y")
        val Z: BooleanProperty = BooleanProperty.create("z")
        fun applyAxes(state: BlockState, axes: AxisSet): BlockState {
            return state.setValue(X, axes.hasX())
                .setValue(Y, axes.hasY())
                .setValue(Z, axes.hasZ())
        }

        fun getAxes(state: BlockState): AxisSet {
            return AxisSet.Mutable().apply {
                withX(state.getValue(X))
                withY(state.getValue(Y))
                withZ(state.getValue(Z))
            }
        }

        val config: PortalConfig = PortalConfig(
            2, 21, 3, 21,
            { b -> b.isAir || b.`is`(CBlockTags.can_light_portal) || b.`is`(candy_portal) },
            { b -> b.`is`(CBlockTags.candy_portal_frame) },
            enableHorizontal = true
        )


        private val DEFAULT: VoxelShape = Shapes.empty()
        private val X_AABB: VoxelShape = box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0)
        private val Y_AABB: VoxelShape = box(0.0, 6.0, 0.0, 16.0, 10.0, 16.0)
        private val Z_AABB: VoxelShape = box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0)
        private val XY_AABB: VoxelShape = Shapes.or(X_AABB, Y_AABB)
        private val XZ_AABB: VoxelShape = Shapes.or(X_AABB, Z_AABB)
        private val YZ_AABB: VoxelShape = Shapes.or(Y_AABB, Z_AABB)
        private val XYZ_AABB: VoxelShape = Shapes.or(X_AABB, Y_AABB, Z_AABB)

        /**
         * index = or-ed
         * 1 x
         * 2 y
         * 4 z
         */
        private val shapes = arrayOf(DEFAULT, X_AABB, Y_AABB, XY_AABB, Z_AABB, XZ_AABB, YZ_AABB, XYZ_AABB)

        @JvmStatic
        @UsedByMixin
        fun onLiquidPlace(level: Level, pos: BlockPos, content: FluidState) {
            level.profiler.use("CheckCaramelPortal") {
                //some mod will use lava tag
                val lava = content.`is`(Fluids.LAVA) || content.`is`(Fluids.FLOWING_LAVA)
                val candy = content.`is`(CFluidTags.liquid_candy)
                if (lava || candy) {
                    VectorPortalShape.findPortal(level, pos, config).ifPresent {
                        val portal = when {
                            candy -> CBlocks.liquid_candy_portal.get()
                            else -> CBlocks.caramel_portal.get()
                        }.defaultBlockState()

                        it.build(level) { level, shape ->
                            shape.portals.forEach { (pos, axes) ->
                                val origin = level.getBlockState(pos)
                                var axes = axes
                                if (!origin.isAir && origin.block is CaramelPortalBlock) {
                                    axes = axes.or(getAxes(origin))
                                }
                                level.setBlockAndUpdate(pos, applyAxes(portal, axes))
                            }
                            true
                        }
                    }

                }
            }
        }
    }
}
