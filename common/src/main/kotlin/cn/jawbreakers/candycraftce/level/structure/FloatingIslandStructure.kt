package cn.jawbreakers.candycraftce.level.structure

import cn.jawbreakers.candycraftce.registry.CBlocks.chocolate_stone
import cn.jawbreakers.candycraftce.registry.CBlocks.custard_pudding_block
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import cn.jawbreakers.candycraftce.registry.CBlocks.pudding_block
import cn.jawbreakers.candycraftce.registry.worldgen.CStructurePieceTypes.floating_island_piece
import cn.jawbreakers.candycraftce.registry.worldgen.CStructureTypes
import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.RandomSource
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.WorldGenLevel
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
            val base = BlockPos(box.minX(), pos.y, box.minZ())
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

            val top = mutableListOf<BlockPos>()
            for (x in 0..31) {
                for (z in 0..31) {
                    if (lastLayer[x][z]) {
                        top.add(base.offset(x, height - 1, z))
                    }
                }
            }

//            val type = random.nextInt(3)
//        if (type == 0 || type == 1) {
////            LegacyStructureFeature.decoratePigFeedIsland(level, random, top)
//        }
//        if (type == 1) {
//            val house = base.offset(14 + random.nextInt(4) - 2, height - 1, 14 + random.nextInt(4) - 2)
////            LegacyStructureFeature.buildVillageHouse(level, random, house, random.nextInt(4), true)
//        }
//        if (type == 2) {
////            LegacyStructureFeature.decorateChewingGumIsland(level, random, top)
////            LegacyStructureFeature.spawnBossBeetle(level, base.offset(16, height + 2, 16))
//        }
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

//    private fun decoratePigFeedIsland(level: WorldGenLevel, random: RandomSource, top: MutableList<BlockPos>) {
//        for (pos in top) {
//            val above = pos.above()
//            if (random.nextInt(3) == 0) {
//                set(level, pos, CCBlocks.CANDY_FARMLAND.get().defaultBlockState())
//                set(level, above, CCBlocks.DRAGIBUS_CROPS.get().defaultBlockState().setValue(CropBlock.AGE, 7))
//            } else if (level.isEmptyBlock(above) && random.nextBoolean()) {
//                set(level, above, randomSweetGrass(random))
//            }
//        }
//    }
    }

}