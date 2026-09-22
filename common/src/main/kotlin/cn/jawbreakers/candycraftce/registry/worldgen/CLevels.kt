package cn.jawbreakers.candycraftce.registry.worldgen

import cn.jawbreakers.candycraftce.client.level.CandyEffects
import cn.jawbreakers.candycraftce.client.level.DungeonEffects
import cn.jawbreakers.candycraftce.level.CandyChunkGenerator
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.levels
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

@Suppress("UnusedExpression")
object CLevels {
    init {
        CBiomes
        CFoliagePlacers
        CFeatures
        CStructurePieceTypes
        CStructureTypes
        CStructureSets
        CStructures
        CLogUtils.sign()
    }

    val dungeon_effects = "dungeon_effects".modLoc()
    val candy_effects = "candy_effects".modLoc()
    val candy_chunk_generator = levels.registerChunkGenerator("candyland") { CandyChunkGenerator.codec }

    @JvmField
    val candyland: ResourceKey<Level> = ResourceKey.create(Registries.DIMENSION, "candyland".modLoc())
    val dungeons: ResourceKey<Level> = ResourceKey.create(Registries.DIMENSION, "dungeons".modLoc())

    init {
        CPlatformUtils.datagen?.onBootstrap {
            add(Registries.NOISE_SETTINGS, CandyChunkGenerator::bootstrap)
        }
        CPlatformUtils.ifClient {
            levels.registerDimensionSpecialEffects(dungeon_effects, DungeonEffects)
            levels.registerDimensionSpecialEffects(candy_effects, CandyEffects)
        }
    }
}