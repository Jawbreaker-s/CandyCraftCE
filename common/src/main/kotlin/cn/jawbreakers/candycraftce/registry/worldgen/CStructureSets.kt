package cn.jawbreakers.candycraftce.registry.worldgen

import cn.jawbreakers.candycraftce.registry.worldgen.CStructureTypes.floating_island_type
import cn.jawbreakers.candycraftce.registry.worldgen.CStructures.floating_island
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.structure.StructureSet
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType

/**
 * Created in 2026/9/20 01:10
 * Project: CandyCraftCE
 * Author: [Bread_NiceCat](https://github.com/Bread-NiceCat)
 */
object CStructureSets {
    init {
        CLogUtils.sign()
        CPlatformUtils.datagen?.onBootstrap {
            add(Registries.STRUCTURE_SET, ::bootstrap)
        }
    }

    val floating_island_set = set(floating_island_type)

    private fun set(entry: Entry<out StructureType<*>>): ResourceKey<StructureSet> {
        return ResourceKey.create(Registries.STRUCTURE_SET, entry.id)
    }

    fun bootstrap(ctx: BootstapContext<StructureSet>) {
        val structures = ctx.lookup(Registries.STRUCTURE)
        ctx.register(
            floating_island_set,
            StructureSet(
                structures.getOrThrow(floating_island),
                RandomSpreadStructurePlacement(8, 2, RandomSpreadType.LINEAR, 14357619)
            )
        )

    }

}