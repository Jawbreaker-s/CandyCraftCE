package cn.jawbreakers.candycraftce.level.foliage_placer

import cn.jawbreakers.candycraftce.registry.worldgen.CFoliagePlacers
import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.tags.BlockTags
import net.minecraft.util.RandomSource
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.world.level.LevelSimulatedReader
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import kotlin.math.abs

/**
 * Created in 2026/9/18 19:49
 * Project: CandyCraftCE
 * Author: [Bread_NiceCat](https://github.com/Bread-NiceCat)
 */
class EnchantFoliagePlacer : FoliagePlacer(ConstantInt.of(6), ConstantInt.of(0)) {
    companion object {
        val codec: Codec<EnchantFoliagePlacer> = Codec.unit(::EnchantFoliagePlacer)
    }

    override fun type(): FoliagePlacerType<*> {
        return CFoliagePlacers.enchant.get()
    }

    override fun createFoliage(
        reader: LevelSimulatedReader,
        setter: FoliageSetter,
        random: RandomSource,
        config: TreeConfiguration,
        maxFreeTreeHeight: Int,
        attachment: FoliageAttachment,
        foliageHeight: Int,
        foliageRadius: Int,
        offset: Int,
    ) {
        val crown = attachment.pos()
        val f = config.foliageProvider
        f.placeSquareLayer(reader, setter, crown.below(3), 4, random, true)
        f.placeSquareLayer(reader, setter, crown.below(2), 5, random, true)
        f.placeSquareLayer(reader, setter, crown.below(), 6, random, true)
        f.placeSquareLayer(reader, setter, crown, 6, random, true)
        f.placeSquareLayer(reader, setter, crown.above(), 5, random, true)
        f.placeSquareLayer(reader, setter, crown.above(2), 3, random, true)
    }

    private fun BlockStateProvider.placeSquareLayer(
        level: LevelSimulatedReader,
        setter: FoliageSetter,
        center: BlockPos,
        radius: Int,
        random: RandomSource,
        softenCorners: Boolean,
    ) {
        val mu = center.mutable()
        for (x in -radius..radius) {
            mu.x = center.x + x
            for (z in -radius..radius) {
                mu.z = center.z + z
                val corner = abs(x) == radius && abs(z) == radius
                if (!softenCorners || !corner || random.nextInt(3) != 0) {
                    if (isReplaceable(level, mu)) {
                        setter.set(mu, getState(random, mu))
                    }
                }
            }
        }
    }

    private fun isReplaceable(level: LevelSimulatedReader, pos: BlockPos): Boolean {
        return level.isStateAtPosition(pos) {
            it.isAir || it.canBeReplaced() || it.`is`(BlockTags.LEAVES)
        }
    }

    override fun foliageHeight(
        random: RandomSource,
        height: Int,
        config: TreeConfiguration,
    ): Int = 6

    override fun shouldSkipLocation(
        random: RandomSource,
        localX: Int,
        localY: Int,
        localZ: Int,
        range: Int,
        large: Boolean,
    ): Boolean {
        TODO("Not yet implemented")
    }
}