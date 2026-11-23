package cn.jawbreakers.candycraftce.fabric.level

import cn.jawbreakers.candycraftce.fabric.CandyCraftCEFabric
import cn.jawbreakers.candycraftce.utils.CUtils.register
import cn.jawbreakers.candycraftce.utils.ICPlatformLevels
import cn.jawbreakers.candycraftce.utils.registry.Entry
import com.mojang.serialization.Codec
import net.fabricmc.fabric.impl.client.rendering.DimensionRenderingRegistryImpl
import net.minecraft.client.renderer.DimensionSpecialEffects
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType
import java.util.function.Supplier

object CFabricLevels : ICPlatformLevels {
    @Suppress("UnstableApiUsage")
    override fun registerDimensionSpecialEffects(id: ResourceLocation, effects: DimensionSpecialEffects) =
        DimensionRenderingRegistryImpl.registerDimensionEffects(id, effects)

    override fun <T : Codec<out ChunkGenerator>> registerChunkGenerator(name: String, codec: Supplier<T>): Entry<T> =
        CandyCraftCEFabric.instance.register("ChunkGenerator", name, codec::get) { id, it ->
            BuiltInRegistries.CHUNK_GENERATOR.register(id, it)
        }

    override fun <T : StructureType<*>> registerStructureType(name: String, type: T): Entry<T> =
        CandyCraftCEFabric.instance.register("StructureType", name, { type }) { id, it ->
            BuiltInRegistries.STRUCTURE_TYPE.register(id, it)
        }

    override fun <F : Feature<*>> registerFeature(name: String, factory: Supplier<F>): Entry<F> =
        CandyCraftCEFabric.instance.register("Feature", name, factory::get) { id, it ->
            BuiltInRegistries.FEATURE.register(id, it)
        }

    override fun <P : FoliagePlacer> registerFoliagePlacer(name: String, codec: Supplier<Codec<P>>) =
        CandyCraftCEFabric.instance.register(
            "FoliagePlacerType",
            name,
            { FoliagePlacerType(codec.get()) }) { id, it ->
            BuiltInRegistries.FOLIAGE_PLACER_TYPE.register(id, it)
        }

    override fun <T : StructurePieceType> registerStructurePieceType(key: String, type: T): Entry<T> =
        CandyCraftCEFabric.instance.register("StructurePieceType", key, { type }) { id, it ->
            BuiltInRegistries.STRUCTURE_PIECE.register(id, it)
        }
}