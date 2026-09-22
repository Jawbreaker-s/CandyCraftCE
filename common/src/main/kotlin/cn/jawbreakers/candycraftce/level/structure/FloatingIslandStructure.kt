package cn.jawbreakers.candycraftce.level.structure

import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.chocolate_stone
import cn.jawbreakers.candycraftce.registry.CBlocks.custard_pudding_block
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import cn.jawbreakers.candycraftce.registry.CBlocks.pudding_block
import cn.jawbreakers.candycraftce.registry.CBlocks.pudding_farmland
import cn.jawbreakers.candycraftce.registry.worldgen.CStructurePieceTypes.floating_island_piece
import cn.jawbreakers.candycraftce.registry.worldgen.CStructureTypes
import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.RandomSource
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure
import net.minecraft.world.level.levelgen.structure.StructureType


/**
 * Created in 2026/9/19 23:42
 * Project: CandyCraftCE
 * Author: [Bread_NiceCat](https://github.com/Bread-NiceCat)
 */
class FloatingIslandStructure(settings: StructureSettings) : SinglePieceStructure(
    ::FloatingIslandPiece, SIZE_X, SIZE_Y, settings,
) {
    companion object {
        const val SIZE_X = 32
        const val SIZE_Y = 32
        val codec: Codec<FloatingIslandStructure> = simpleCodec(::FloatingIslandStructure)
    }

    override fun type(): StructureType<*> = CStructureTypes.floating_island_type.get()

    class FloatingIslandPiece : ScatteredFeaturePiece {
        constructor(random: RandomSource, x: Int, z: Int) : super(
            floating_island_piece.get(),
            x, 128, z,
            SIZE_X, random.nextInt(3) + 7, SIZE_Y,
            getRandomHorizontalDirection(random)
        )

        constructor(tag: CompoundTag) : super(floating_island_piece.get(), tag)

        override fun postProcess(
            level: WorldGenLevel,
            structureManager: StructureManager,
            generator: ChunkGenerator,
            random: RandomSource,
            box: BoundingBox,
            chunkPos: ChunkPos,
            pos: BlockPos,
        ) {
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

            val type = random.nextInt(10)
            when (type) {
                0 -> Unit//boss
                in 1..3 -> Unit//house
                else -> decoratePigFeedIsland(level, random, top, box)
            }
            if (type == 0 || type == 1) {
            }
            if (type == 1) {
//                val house = base.offset(14 + random.nextInt(4) - 2, height - 1, 14 + random.nextInt(4) - 2)
//            LegacyStructureFeature.buildVillageHouse(level, random, house, random.nextInt(4), true)
            }
            if (type == 2) {
//            LegacyStructureFeature.decorateChewingGumIsland(level, random, top)
//            LegacyStructureFeature.spawnBossBeetle(level, base.offset(16, height + 2, 16))
            }
        }

        private fun placeFloatingIslandColumn(
            level: WorldGenLevel, random: RandomSource,
            layer: Array<BooleanArray>,
            x: Int, y: Int, z: Int, box: BoundingBox,
        ) {
            if (x !in 0..<32 || z !in 0..32) return
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
                if (random.nextInt(3) == 0) {
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
//
//        private fun buildVillageHouse(
//            level: WorldGenLevel,
//            random: RandomSource,
//            base: BlockPos,
//            side: Int,
//            window: Boolean,
//        ) {
//            val metadata = random.nextInt(3)
//            val planks = LegacyStructureFeature.marshmallowPlanks(metadata)
//            val roofPlanks = CCBlocks.MARSHMALLOW_PLANKS.get().defaultBlockState()
//            val logs = LegacyStructureFeature.marshmallowLog(metadata, Direction.Axis.Y)
//            val logX = LegacyStructureFeature.marshmallowLog(metadata, Direction.Axis.X)
//            val logZ = LegacyStructureFeature.marshmallowLog(metadata, Direction.Axis.Z)
//            val slab = LegacyStructureFeature.marshmallowSlab(metadata)
//                .setValue<SlabType?, SlabType?>(SlabBlock.TYPE, SlabType.TOP)
//            for (dx in 0..4) {
//                for (dz in 0..4) {
//                    LegacyStructureFeature.set(
//                        level,
//                        base.offset(dx, 0, dz),
//                        CCBlocks.CHOCOLATE_STONE.get().defaultBlockState()
//                    )
//                    LegacyStructureFeature.set(level, base.offset(dx, 3, dz), roofPlanks)
//                }
//            }
//            for (y in 1..2) {
//                for (dx in 0..4) {
//                    for (dz in 0..4) {
//                        val corner = (dx == 0 || dx == 4) && (dz == 0 || dz == 4)
//                        val edge = dx == 0 || dx == 4 || dz == 0 || dz == 4
//                        LegacyStructureFeature.set(
//                            level,
//                            base.offset(dx, y, dz),
//                            if (corner) logs else if (edge) planks else Blocks.AIR.defaultBlockState()
//                        )
//                    }
//                }
//            }
//            for (dx in 1..3) {
//                LegacyStructureFeature.set(level, base.offset(dx, 3, 0), logX)
//                LegacyStructureFeature.set(level, base.offset(dx, 3, 4), logX)
//            }
//            for (dz in 1..3) {
//                LegacyStructureFeature.set(level, base.offset(0, 3, dz), logZ)
//                LegacyStructureFeature.set(level, base.offset(4, 3, dz), logZ)
//            }
//
//            if (window) {
//                val glass = LegacyStructureFeature.houseWindowPos(base, side, random.nextInt(3))
//                LegacyStructureFeature.set(
//                    level, glass, if (random.nextInt(3) == 0)
//                        CCBlocks.CARAMEL_PANE.get().defaultBlockState()
//                    else
//                        if (random.nextBoolean()) CCBlocks.CARAMEL_PANE_ROUND.get()
//                            .defaultBlockState() else CCBlocks.CARAMEL_PANE_DIAMOND.get().defaultBlockState()
//                )
//                LegacyStructureFeature.connectCaramelPane(level, glass)
//            }
//            val door = LegacyStructureFeature.houseWallPos(base, side, random.nextInt(3))
//            LegacyStructureFeature.set(level, door, Blocks.AIR.defaultBlockState())
//            LegacyStructureFeature.set(level, door.above(), slab)
//            LegacyStructureFeature.set(level, base.offset(0, 3, 0), Blocks.AIR.defaultBlockState())
//            LegacyStructureFeature.set(level, base.offset(4, 3, 0), Blocks.AIR.defaultBlockState())
//            LegacyStructureFeature.set(level, base.offset(4, 3, 4), Blocks.AIR.defaultBlockState())
//            LegacyStructureFeature.set(level, base.offset(0, 3, 4), Blocks.AIR.defaultBlockState())
//            LegacyStructureFeature.spawnGingerbread(
//                level,
//                base.offset(2, 2, 2),
//                if (base.getY() > 100) GingerbreadManEntity.ELDER else -1
//            )
//        }

        //	private static boolean floatingIsland(WorldGenLevel level, RandomSource random, BlockPos origin) {
        //		BlockPos base = origin.offset(-16, 0, -16);
        //		int nX = random.nextInt(8) - 4;
        //		int nZ = random.nextInt(8) - 4;
        //		int[][] lastLayer = new int[32][32];
        //		lastLayer[16][16] = 2;
        //		lastLayer[16 + nX][16 + nZ] = 2;
        //		int maxHeight = random.nextInt(3) + 7;
        //
        //		for (int y = 0; y < maxHeight; y++) {
        //			int[][] newLayer = new int[32][32];
        //			for (int x = 1; x < 31; x++) {
        //				for (int z = 1; z < 31; z++) {
        //					if (lastLayer[x][z] != 2) {
        //						continue;
        //					}
        //					placeFloatingIslandColumn(level, random, base, newLayer, x, y, z, maxHeight);
        //					if (random.nextInt(4) < 3 || y == maxHeight - 1) {
        //						placeFloatingIslandColumn(level, random, base, newLayer, x + 1, y, z, maxHeight);
        //					}
        //					if (random.nextInt(4) < 3 || y == maxHeight - 1) {
        //						placeFloatingIslandColumn(level, random, base, newLayer, x - 1, y, z, maxHeight);
        //					}
        //					if (random.nextInt(4) < 3 || y == maxHeight - 1) {
        //						placeFloatingIslandColumn(level, random, base, newLayer, x, y, z - 1, maxHeight);
        //					}
        //					if (random.nextInt(4) < 3 || y == maxHeight - 1) {
        //						placeFloatingIslandColumn(level, random, base, newLayer, x, y, z + 1, maxHeight);
        //					}
        //					if (random.nextInt(4) < 1) {
        //						placeFloatingIslandColumn(level, random, base, newLayer, x - 1, y, z - 1, maxHeight);
        //					}
        //					if (random.nextInt(4) < 1) {
        //						placeFloatingIslandColumn(level, random, base, newLayer, x + 1, y, z + 1, maxHeight);
        //					}
        //					if (random.nextInt(4) < 1) {
        //						placeFloatingIslandColumn(level, random, base, newLayer, x + 1, y, z - 1, maxHeight);
        //					}
        //					if (random.nextInt(4) < 1) {
        //						placeFloatingIslandColumn(level, random, base, newLayer, x - 1, y, z + 1, maxHeight);
        //					}
        //				}
        //			}
        //			lastLayer = newLayer;
        //		}
        //
        //		List<BlockPos> top = new ArrayList<>();
        //		for (int x = 0; x < 32; x++) {
        //			for (int z = 0; z < 32; z++) {
        //				if (lastLayer[x][z] == 2) {
        //					top.add(base.offset(x, maxHeight - 1, z));
        //				}
        //			}
        //		}
        //
        //		int type = random.nextInt(3);
        //		if (type == 0 || type == 1) {
        //			decoratePigFeedIsland(level, random, top);
        //		}
        //		if (type == 1) {
        //			BlockPos house = base.offset(14 + random.nextInt(4) - 2, maxHeight - 1, 14 + random.nextInt(4) - 2);
        //			buildVillageHouse(level, random, house, random.nextInt(4), true);
        //		}
        //		if (type == 2) {
        //			decorateChewingGumIsland(level, random, top);
        //			spawnBossBeetle(level, base.offset(16, maxHeight + 2, 16));
        //		}
        //		return true;
        //	}
        //
        //	private static void placeFloatingIslandColumn(WorldGenLevel level, RandomSource random, BlockPos base, int[][] layer,
        //	                                              int x, int y, int z, int maxHeight) {
        //		if (x < 0 || x >= 32 || z < 0 || z >= 32) {
        //			return;
        //		}
        //		layer[x][z] = 2;
        //		set(level, base.offset(x, y, z), floatingIslandBlockForHeight(y, maxHeight, random));
        //	}
        //
        //	private static BlockState floatingIslandBlockForHeight(int height, int maxHeight, RandomSource random) {
        //		int distance = maxHeight - height;
        //		if (distance == 1) {
        //			return CCBlocks.PUDDING.get().defaultBlockState();
        //		}
        //		if (distance == 2) {
        //			return CCBlocks.FLOUR.get().defaultBlockState();
        //		}
        //		if (distance > 2 && distance <= 6) {
        //			return random.nextInt(5) < distance
        //					? CCBlocks.CHOCOLATE_STONE.get().defaultBlockState()
        //					: CCBlocks.FLOUR.get().defaultBlockState();
        //		}
        //		return CCBlocks.CHOCOLATE_STONE.get().defaultBlockState();
        //	}
        //	private static void decoratePigFeedIsland(WorldGenLevel level, RandomSource random, List<BlockPos> top) {
        //		for (BlockPos pos : top) {
        //			BlockPos above = pos.above();
        //			if (random.nextInt(3) == 0) {
        //				set(level, pos, CCBlocks.CANDY_FARMLAND.get().defaultBlockState());
        //				set(level, above, CCBlocks.DRAGIBUS_CROPS.get().defaultBlockState().setValue(CropBlock.AGE, 7));
        //			} else if (level.isEmptyBlock(above) && random.nextBoolean()) {
        //				set(level, above, randomSweetGrass(random));
        //			}
        //		}
        //	}
//        private fun decorateChewingGumIsland(level: WorldGenLevel, random: RandomSource, top: MutableList<BlockPos>) {
//            for (pos in top) {
//                val above = pos.above()
//                if (!level.isEmptyBlock(above)) {
//                    continue
//                }
//                if (random.nextBoolean()) {
//                    LegacyStructureFeature.set(level, above, CBlocks.chewing_gum_block.get().defaultBlockState())
//                } else if (random.nextInt(3) == 0) {
//                    LegacyStructureFeature.set(level, above, LegacyStructureFeature.randomSweetGrass(random))
//                }
//            }
//        }

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