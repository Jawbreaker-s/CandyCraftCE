package cn.jawbreakers.candycraftce.forge.levels

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.forge.CandyCraftCEForge.Companion.instance
import cn.jawbreakers.candycraftce.utils.ICPlatformLevels
import cn.jawbreakers.candycraftce.utils.registry.Entry
import com.mojang.serialization.Codec
import net.minecraft.client.renderer.DimensionSpecialEffects
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.DeferredRegister.create
import net.minecraftforge.registries.ForgeRegistries
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import java.util.function.Supplier

object CForgeLevels : ICPlatformLevels {
    val generator: DeferredRegister<Codec<out ChunkGenerator>> = create(Registries.CHUNK_GENERATOR, MOD_ID)
    val structure: DeferredRegister<StructureType<*>> = create(Registries.STRUCTURE_TYPE, MOD_ID)
    val feature: DeferredRegister<Feature<*>> = create(ForgeRegistries.FEATURES, MOD_ID)
    val foliage_placer: DeferredRegister<FoliagePlacerType<*>> = create(ForgeRegistries.FOLIAGE_PLACER_TYPES, MOD_ID)
    val structure_piece_type: DeferredRegister<StructurePieceType> = create(Registries.STRUCTURE_PIECE, MOD_ID)

    init {
        MOD_BUS.addListener(::onRegisterDimensionSpecialEffects)
        listOf(
            generator,
            structure,
            feature,
            foliage_placer
        ).forEach { it.register(MOD_BUS) }
    }

    val specialEffects: MutableMap<ResourceLocation, DimensionSpecialEffects> = mutableMapOf()

    fun onRegisterDimensionSpecialEffects(event: RegisterDimensionSpecialEffectsEvent) {
        specialEffects.forEach(event::register)
    }

    override fun registerDimensionSpecialEffects(id: ResourceLocation, effects: DimensionSpecialEffects) {
        specialEffects[id] = effects
    }

    override fun <T : Codec<out ChunkGenerator>> registerChunkGenerator(name: String, codec: Supplier<T>): Entry<T> =
        instance.register(generator, name, codec)

    override fun <T : StructureType<*>> registerStructureType(name: String, type: T): Entry<T> =
        instance.register(structure, name) { type }

    override fun <F : Feature<*>> registerFeature(name: String, factory: Supplier<F>): Entry<F> =
        instance.register(feature, name, factory)

    override fun <P : FoliagePlacer> registerFoliagePlacer(name: String, codec: Supplier<Codec<P>>) =
        instance.register(foliage_placer, name) { FoliagePlacerType(codec.get()) }

    override fun <T : StructurePieceType> registerStructurePieceType(key: String, type: T): Entry<T> =
        instance.register(structure_piece_type, key) { type }
}