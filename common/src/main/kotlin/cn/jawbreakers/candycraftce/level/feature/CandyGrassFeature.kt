package cn.jawbreakers.candycraftce.level.feature

import cn.jawbreakers.candycraftce.registry.CBiomeTags
import cn.jawbreakers.candycraftce.registry.CBlockTags
import cn.jawbreakers.candycraftce.registry.CBlocks.acid_mint_flower
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import cn.jawbreakers.candycraftce.registry.CBlocks.fraise_tagada_flower
import cn.jawbreakers.candycraftce.registry.CBlocks.sugar_essence_flower
import cn.jawbreakers.candycraftce.registry.CBlocks.sweet_grass_pale
import cn.jawbreakers.candycraftce.registry.CBlocks.sweet_grass_pink
import cn.jawbreakers.candycraftce.registry.CBlocks.sweet_grass_red
import cn.jawbreakers.candycraftce.registry.CBlocks.sweet_grass_yellow
import net.minecraft.core.BlockPos
import net.minecraft.tags.BlockTags
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

class CandyGrassFeature : Feature<NoneFeatureConfiguration>(NoneFeatureConfiguration.CODEC) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val random = context.random()
        val origin = context.origin()

        val surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.x, origin.z)
        val startY: Int = random.nextInt(surfaceY * 2)
        var base = BlockPos(origin.x, startY, origin.z)

        var foundBase = false
        base = base.mutable()
        while (base.y in 56..<128) {
            val state = level.getBlockState(base)
            if (!state.isAir && !state.`is`(BlockTags.LEAVES)) {
                foundBase = true
                break
            }
            base.y--
        }
        if (!foundBase) return false

        base = base.immutable()

        var placed = false
        repeat(128) {
            val x = base.x + random.nextInt(8) - random.nextInt(8)
            val y = base.y + random.nextInt(4) - random.nextInt(4)
            val z = base.z + random.nextInt(8) - random.nextInt(8)
            val target = BlockPos(x, y, z)

            if (y > 58 && level.isEmptyBlock(target) && canPlaceOnCandyDirt(level, target.below())) {
                val plant = getPlant(level, target, random)
                if (plant != null) {
                    level.setBlock(target, plant, 2)
                    placed = true
                }
            }
        }

        return placed
    }

    private fun canPlaceOnCandyDirt(level: WorldGenLevel, pos: BlockPos): Boolean {
        return level.getBlockState(pos).`is`(CBlockTags.candy_soil)
    }

    private fun getPlant(level: WorldGenLevel, pos: BlockPos, random: RandomSource): BlockState? {
        val biome = level.getBiome(pos)

        if (biome.`is`(CBiomeTags.has_essence_flower)) {
            if (random.nextInt(600) == 6) {
                return sugar_essence_flower.defaultBlockState()
            }
        }
        if (biome.`is`(CBiomeTags.is_cold)) return null

        if (random.nextInt(32) == 31 && random.nextBoolean()) {
            return if (biome.`is`(CBiomeTags.has_mint_flower)) {
                acid_mint_flower.defaultBlockState()
            } else {
                fraise_tagada_flower.defaultBlockState()
            }
        }
        return when (random.nextInt(4)) {
            0 -> sweet_grass_pink.defaultBlockState()
            1 -> sweet_grass_pale.defaultBlockState()
            2 -> sweet_grass_yellow.defaultBlockState()
            else -> sweet_grass_red.defaultBlockState()
        }
    }
}
