package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CBlocks.pudding_block
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

class CustardPuddingBlock(properties: Properties) : Block(properties), ISugarTarget {
    //TODO mapColor for pudding
    //
    //	public MapColor getMapColor(BlockState state, BlockGetter level, BlockPos pos, MapColor defaultColor) {
    //		if (level instanceof LevelReader reader && pos != null) {
    //			String biome = reader.getBiome(pos).unwrapKey()
    //					.map(key -> key.location())
    //					.filter(id -> CandyCraft.MODID.equals(id.getNamespace()))
    //					.map(ResourceLocation::getPath)
    //					.orElse("");
    //			if ("ice_cream_plains".equals(biome) || "ice_cream_sky_mountains".equals(biome) || "sugar_hell_mountains".equals(biome)) {
    //				return MapColor.SNOW;
    //			}
    //		}
    //		return defaultColor;
    //	}
    @Deprecated("Deprecated in Java")
    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (level.getMaxLocalRawBrightness(pos.above()) < 4 && level.getBlockState(pos.above())
                .getLightBlock(level, pos.above()) > 2
        ) {
            level.setBlockAndUpdate(pos, pudding_block.get().defaultBlockState())
        }
    }

    override fun isValidSugarTarget(level: LevelReader, pos: BlockPos, state: BlockState, isClient: Boolean) = true

    override fun isSugarSuccess(level: Level, random: RandomSource, pos: BlockPos, state: BlockState) = true

    override fun performSugar(level: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) {
        val origin = pos.above()
        for (i in 0 until 128) {
            var target = origin
            for (step in 0 until i / 16) {
                target = target.offset(
                    random.nextInt(3) - 1,
                    (random.nextInt(3) - 1) * random.nextInt(3) / 2,
                    random.nextInt(3) - 1
                )
                if (!level.getBlockState(target.below()).`is`(this) || !level.getBlockState(target).isAir) {
                    break
                }
            }

            if (target != null && level.isEmptyBlock(target)) {
                val growth: BlockState = if (random.nextInt(8) == 0)
                    TODO()
//                CBlocks.FRAISE_TAGADA_FLOWER.get().defaultBlockState()
                else randomSweetGrass(random)

                if (growth.canSurvive(level, target)) {
                    level.setBlockAndUpdate(target, growth)
                }
            }
        }
    }

    companion object {
        const val DEFAULT_COLOR: Int = 0xDDA7AA

        private fun randomSweetGrass(random: RandomSource?): BlockState {
            return TODO()
            //		return switch (random.nextInt(4)) {
//			case 0 -> CCBlocks.SWEET_GRASS_PINK.get().defaultBlockState();
//			case 1 -> CCBlocks.SWEET_GRASS_PALE.get().defaultBlockState();
//			case 2 -> CCBlocks.SWEET_GRASS_YELLOW.get().defaultBlockState();
//			default -> CCBlocks.SWEET_GRASS_RED.get().defaultBlockState();
//		};
        }
    }
}
