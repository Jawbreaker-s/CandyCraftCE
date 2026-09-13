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
import net.minecraft.world.level.levelgen.structure.StructureType
import java.util.function.Supplier

object CFabricLevels : ICPlatformLevels {
    @Suppress("UnstableApiUsage")
    override fun registerDimensionSpecialEffects(id: ResourceLocation, effects: DimensionSpecialEffects) =
        DimensionRenderingRegistryImpl.registerDimensionEffects(id, effects)

    override fun <T : Codec<out ChunkGenerator>> registerChunkGenerator(
        id: ResourceLocation,
        codec: Supplier<T>,
    ): Entry<T> = CandyCraftCEFabric.instance.register("ChunkGenerator", id.path, codec::get) { id, it ->
        BuiltInRegistries.CHUNK_GENERATOR.register(id, it)
    }

    override fun <T : StructureType<*>> registerStructureType(id: ResourceLocation, type: T): Entry<T> =
        CandyCraftCEFabric.instance.register("StructureType", id.path, { type }) { id, it ->
            BuiltInRegistries.STRUCTURE_TYPE.register(id, it)
        }
}