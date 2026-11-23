package cn.jawbreakers.candycraftce.level.feature

import cn.jawbreakers.candycraftce.registry.CBlockTags
import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.registry.CEntityTypes.create
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

/**
 * Created in 2026/11/24 00:28 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class JellyBossFeature : Feature<NoneFeatureConfiguration>(NoneFeatureConfiguration.CODEC) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val origin = context.origin()

        val surfaceY = level.getHeight(
            Heightmap.Types.WORLD_SURFACE_WG,
            origin.x,
            origin.z
        )
        val surfacePos = BlockPos(origin.x, surfaceY, origin.z)
        val surfaceState = level.getBlockState(surfacePos)

        if (!surfaceState.`is`(CBlockTags.jelly_queen_spawnable_on)) {
            return false
        }

        val spawnPos = surfacePos.above()
        val boss = CEntityTypes.jelly_queen.create(level.level) ?: return false
        boss.moveTo(spawnPos.x + 0.5, spawnPos.y.toDouble(), spawnPos.z + 0.5, 0f, 0f)
        boss.finalizeSpawn(
            level,
            level.getCurrentDifficultyAt(surfacePos),
            MobSpawnType.STRUCTURE,
            null,
            null
        )
        level.addFreshEntity(boss)
        return true
    }
}