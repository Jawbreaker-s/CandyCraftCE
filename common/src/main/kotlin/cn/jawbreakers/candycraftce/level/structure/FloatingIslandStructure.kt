package cn.jawbreakers.candycraftce.level.structure

import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.chocolate_stone
import cn.jawbreakers.candycraftce.registry.CBlocks.custard_pudding_block
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import cn.jawbreakers.candycraftce.registry.CBlocks.pudding_block
import cn.jawbreakers.candycraftce.registry.CBlocks.pudding_farmland
import cn.jawbreakers.candycraftce.registry.worldgen.CStructurePieceTypes.floating_island_piece
import cn.jawbreakers.candycraftce.registry.worldgen.CStructureTypes
import cn.jawbreakers.candycraftce.utils.CLevelUtils.component1
import cn.jawbreakers.candycraftce.utils.CLevelUtils.component2
import cn.jawbreakers.candycraftce.utils.CLevelUtils.component3
import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.RandomSource
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext
import java.util.function.Supplier


/**
 * Created in 2026/9/19 23:42
 * Project: CandyCraftCE
 * Author: [Bread_NiceCat](https://github.com/Bread-NiceCat)
 */
class FloatingIslandStructure(settings: StructureSettings) : SinglePieceStructure(
    ::FloatingIslandPiece, SIZE_X, SIZE_Z, settings,
) {
    companion object {
        const val SIZE_X = 32
        const val SIZE_Z = 32
        val codec: Codec<FloatingIslandStructure> = simpleCodec(::FloatingIslandStructure)
    }

    override fun type(): StructureType<*> = CStructureTypes.floating_island_type.get()

    class FloatingIslandPiece : ScatteredFeaturePiece {
        val seed: Long

        constructor(random: RandomSource, x: Int, z: Int) : super(
            floating_island_piece.get(),
            x, 128, z,
            SIZE_X, random.nextInt(3) + 7, SIZE_Z,
            getRandomHorizontalDirection(random)
        ) {
            seed = random.nextLong()
        }

        constructor(tag: CompoundTag) : super(floating_island_piece.get(), tag) {
            seed = tag.getLong("seed")
        }

        override fun addAdditionalSaveData(context: StructurePieceSerializationContext, tag: CompoundTag) {
            super.addAdditionalSaveData(context, tag)
            tag.putLong("seed", seed)
        }


        override fun postProcess(
            level: WorldGenLevel,
            structureManager: StructureManager,
            generator: ChunkGenerator,
            random: RandomSource,
            box: BoundingBox,
            chunkPos: ChunkPos,
            pos: BlockPos,
        ) {
            val random = RandomSource.create(seed)
            val type = random.nextInt(10)
            val nX = random.nextInt(8) - 4
            val nZ = random.nextInt(8) - 4
            var lastLayer = Array(32) { BooleanArray(32) }
            lastLayer[16][16] = true
            lastLayer[16 + nX][16 + nZ] = true

            for (y in 0..<height) {
                val newLayer = Array(32) { BooleanArray(32) }
                for (x in 1..30) {
                    for (z in 1..30) {
                        if (!lastLayer[x][z]) {
                            continue
                        }
                        placeFloatingIslandColumn(level, random, newLayer, x, y, z, box)
                        if (random.nextInt(4) < 3 || y == height - 1) {
                            placeFloatingIslandColumn(level, random, newLayer, x + 1, y, z, box)
                        }
                        if (random.nextInt(4) < 3 || y == height - 1) {
                            placeFloatingIslandColumn(level, random, newLayer, x - 1, y, z, box)
                        }
                        if (random.nextInt(4) < 3 || y == height - 1) {
                            placeFloatingIslandColumn(level, random, newLayer, x, y, z - 1, box)
                        }
                        if (random.nextInt(4) < 3 || y == height - 1) {
                            placeFloatingIslandColumn(level, random, newLayer, x, y, z + 1, box)
                        }
                        if (random.nextInt(4) < 1) {
                            placeFloatingIslandColumn(level, random, newLayer, x - 1, y, z - 1, box)
                        }
                        if (random.nextInt(4) < 1) {
                            placeFloatingIslandColumn(level, random, newLayer, x + 1, y, z + 1, box)
                        }
                        if (random.nextInt(4) < 1) {
                            placeFloatingIslandColumn(level, random, newLayer, x + 1, y, z - 1, box)
                        }
                        if (random.nextInt(4) < 1) {
                            placeFloatingIslandColumn(level, random, newLayer, x - 1, y, z + 1, box)
                        }
                    }
                }
                lastLayer = newLayer
            }

            val top = mutableListOf<Vec3i>()
            for (x in 0..31) {
                for (z in 0..31) {
                    if (lastLayer[x][z]) {
                        top.add(Vec3i(x, height - 1, z))
                    }
                }
            }

            when (type) {
                0 -> {
                    decorateChewingGumIsland(level, random, top, box)
//            LegacyStructureFeature.spawnBossBeetle(level, base.offset(16, height + 2, 16))
                }

                1, 2, 3 -> {
                    val offset = Vec3i(14 + random.nextInt(4) - 2, height - 1, 14 + random.nextInt(4) - 2)
                    buildVillageHouse(level, random, offset, box)
                }

                else -> decoratePigFeedIsland(level, random, top, box)
            }
        }

        private fun placeFloatingIslandColumn(
            level: WorldGenLevel, random: RandomSource,
            layer: Array<BooleanArray>,
            x: Int, y: Int, z: Int, box: BoundingBox,
        ) {
            if (x !in 0 until 32 || z !in 0 until 32) return
            layer[x][z] = true
            placeBlock(level, floatingIslandBlockForHeight(y, random), x, y, z, box)
        }

        private fun floatingIslandBlockForHeight(offset: Int, random: RandomSource): BlockState {
            val distance = height - offset
            if (distance == 1) {
                return custard_pudding_block.defaultBlockState()
            }
            if (distance == 2) {
                return pudding_block.defaultBlockState()
            }
            if (distance in 3..6) {
                return if (random.nextInt(5) < distance)
                    chocolate_stone.defaultBlockState()
                else
                    pudding_block.defaultBlockState()
            }
            return chocolate_stone.defaultBlockState()
        }

        private fun decoratePigFeedIsland(
            level: WorldGenLevel,
            random: RandomSource,
            top: MutableList<Vec3i>,
            box: BoundingBox,
        ) {
            for (pos in top) {
                val above = pos.above()
                if (random.nextInt(3) > 0) {
                    placeBlock(level, pudding_farmland.defaultBlockState(), pos.x, pos.y, pos.z, box)
                    placeBlock(
                        level,
                        CBlocks.dragibus_crops.defaultBlockState().setValue(CropBlock.AGE, 7),
                        above.x, above.y, above.z,
                        box
                    )
                }
            }
        }

        private fun decorateChewingGumIsland(
            level: WorldGenLevel,
            random: RandomSource,
            top: MutableList<Vec3i>,
            box: BoundingBox,
        ) {
            for (pos in top) {
                if (random.nextBoolean()) {
                    placeBlock(
                        level,
                        CBlocks.chewing_gum_puddle.get().defaultBlockState(),
                        pos.x,
                        pos.y + 1,
                        pos.z,
                        box
                    )
                }
            }
        }

        private data class WoodFamily(
            val planks: BlockState,
            val logs: BlockState,
            val slabTop: BlockState,
        ) {
            val logZ: BlockState = logs.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z)
            val logX: BlockState = logs.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X)
            val roof = CBlocks.marshmallow_planks.defaultBlockState()

            constructor(planks: Block, logs: RotatedPillarBlock, slabTop: SlabBlock) : this(
                planks.defaultBlockState(),
                logs.defaultBlockState(),
                slabTop.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP)
            )

            constructor(
                planks: Supplier<out Block>,
                logs: Supplier<out RotatedPillarBlock>,
                slabTop: Supplier<out SlabBlock>,
            ) : this(planks.get(), logs.get(), slabTop.get())
        }

        private val woods = arrayOf(
            WoodFamily(CBlocks.marshmallow_planks, CBlocks.marshmallow_log, CBlocks.marshmallow_family.slab!!),
            WoodFamily(
                CBlocks.light_marshmallow_planks,
                CBlocks.light_marshmallow_log,
                CBlocks.light_marshmallow_family.slab!!
            ),
            WoodFamily(
                CBlocks.dark_marshmallow_planks,
                CBlocks.dark_marshmallow_log,
                CBlocks.dark_marshmallow_family.slab!!
            ),
        )

        private fun buildVillageHouse(
            level: WorldGenLevel,
            random: RandomSource,
            offset: Vec3i,
            box: BoundingBox,
        ) {
            val (x, y, z) = offset
            val materials = woods[random.nextInt(3)]
            val air = Blocks.AIR.defaultBlockState()
            for (dx in 0..4) {
                val x = x + dx
                for (dz in 0..4) {
                    val z = z + dz
                    placeBlock(level, chocolate_stone.defaultBlockState(), x, y, z, box)
                    placeBlock(level, materials.roof, dx + offset.x, y + 3, z, box)
                }
            }

            for (dy in 1..2) {
                val y = y + dy
                for (dx in 0..4) {
                    val x = x + dx
                    for (dz in 0..4) {
                        val z = z + dz

                        val corner = (dx == 0 || dx == 4) && (dz == 0 || dz == 4)
                        val edge = dx == 0 || dx == 4 || dz == 0 || dz == 4
                        placeBlock(
                            level,
                            when {
                                corner -> materials.logs
                                edge -> materials.planks
                                else -> Blocks.AIR.defaultBlockState()
                            },
                            x, y, z, box
                        )
                    }
                }
            }
            for (dx in 1..3) {
                placeBlock(level, materials.logX, x + dx, y + 3, z, box)
                placeBlock(level, materials.logX, x + dx, y + 3, z + 4, box)
            }
            for (dz in 1..3) {
                placeBlock(level, materials.logZ, x, y + 3, z + dz, box)
                placeBlock(level, materials.logZ, x + 4, y + 3, z + dz, box)
            }
            val glass = when (random.nextInt(3)) {
                0 -> CBlocks.caramel_pane.defaultBlockState()
                1 -> CBlocks.caramel_pane_round.defaultBlockState()
                else -> CBlocks.caramel_pane_diamond.defaultBlockState()
            }
            placeBlock(level, glass, x + 4, y + 2, z + 1 + random.nextInt(3), box)
            run {
                val y = y + 1
                val z = z + 1 + random.nextInt(3)
                placeBlock(level, air, x, y, z, box)
                placeBlock(level, materials.slabTop, x, y + 1, z, box)
                placeBlock(level, air, x, y + 3, z, box)
                placeBlock(level, air, x + 4, y + 3, z, box)
                placeBlock(level, air, x + 4, y + 3, z + 4, box)
                placeBlock(level, air, x, y + 3, z + 4, box)

            }
//            LegacyStructureFeature.spawnGingerbread(
//                level,
//                base.offset(2, 2, 2),
//                if (base.getY() > 100) GingerbreadManEntity.ELDER else -1
//            )
        }


        private fun spawnBossBeetle(level: WorldGenLevel?, pos: BlockPos) {
            //TODO
//            if (level !is WorldGenRegion) {
//                return
//            }
//            val entity: BasicCandySpiderEntity? = CCEntityTypes.BOSS_BEETLE.get().create(level.getLevel())
//            if (entity == null) {
//                return
//            }
//            entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f)
//            entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null)
//            level.addFreshEntity(entity)
        }

    }

}