package cn.jawbreakers.candycraftce.registry.worldgen

import cn.jawbreakers.candycraftce.level.structure.FloatingIslandStructure
import cn.jawbreakers.candycraftce.registry.CBiomeTags.has_floating_island
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment

/**
 * Created in 2026/9/20 01:15
 * Project: CandyCraftCE
 * Author: [Bread_NiceCat](https://github.com/Bread-NiceCat)
 */
object CStructures {
    init {
        CLogUtils.sign()
        CPlatformUtils.datagen?.onBootstrap {
            add(Registries.STRUCTURE, ::bootstrap)
        }
    }

    val floating_island = structure(CStructureTypes.floating_island_type)
    private fun structure(entry: Entry<out StructureType<*>>): ResourceKey<Structure> {
        return ResourceKey.create(Registries.STRUCTURE, entry.id)
    }

    fun bootstrap(ctx: BootstapContext<Structure>) {
        val lookup = ctx.lookup(Registries.BIOME)
        fun structure(
            biomes: TagKey<Biome>,
            spawns: Map<MobCategory, StructureSpawnOverride> = mapOf(),
            step: GenerationStep.Decoration = GenerationStep.Decoration.SURFACE_STRUCTURES,
            terrainAdaptation: TerrainAdjustment = TerrainAdjustment.NONE,
        ): Structure.StructureSettings {
            return Structure.StructureSettings(lookup.getOrThrow(biomes), spawns, step, terrainAdaptation)
        }

        ctx.register(floating_island, FloatingIslandStructure(structure(has_floating_island)))
    }
}