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
        val base = BlockPos(origin.x, surfaceY, origin.z)

        var placed = false
        repeat(128) {
            val x = base.x + random.nextInt(8) - random.nextInt(8)
            val y = base.y + random.nextInt(4) - random.nextInt(4)
            val z = base.z + random.nextInt(8) - random.nextInt(8)
            val target = BlockPos(x, y, z)

            if (y > 58 && level.isEmptyBlock(target)) {
                val plant = getPlant(level, target, level.getBlockState(target.below()), random)
                if (plant != null && plant.canSurvive(level, target)) {
                    level.setBlock(target, plant, 2)
                    placed = true
                }
            }
        }

        return placed
    }

    private fun getPlant(level: WorldGenLevel, pos: BlockPos, below: BlockState, random: RandomSource): BlockState? {
        val isCandySoil = below.`is`(CBlockTags.candy_soil)
        val isIceSoil = below.`is`(CBlockTags.ice_soil)

        val biome = level.getBiome(pos)
        val isColdBiome = biome.`is`(CBiomeTags.is_cold)

        if (biome.`is`(CBiomeTags.has_essence_flower)) {
            if (isCandySoil || isIceSoil) {
                var chance = 400
                if (isColdBiome) chance /= 2
                if (isIceSoil) chance /= 2
                if (pos.y >= 100) chance /= 2
                if (pos.y >= 125) chance /= 2
                if (pos.y >= 150) chance /= 2
                if (random.nextInt(chance) == 0) {
                    return sugar_essence_flower.defaultBlockState()
                }
            }
        }
        //cold只能生成金花
        if (isColdBiome) return null
        //以下都是基于#candy_soil生成
        if (!isCandySoil) return null

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
