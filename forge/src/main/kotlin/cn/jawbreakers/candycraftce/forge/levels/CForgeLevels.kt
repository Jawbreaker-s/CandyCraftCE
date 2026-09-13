package cn.jawbreakers.candycraftce.forge.levels

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.forge.CandyCraftCEForge.Companion.instance
import cn.jawbreakers.candycraftce.utils.ICPlatformLevels
import cn.jawbreakers.candycraftce.utils.registry.Entry
import com.mojang.serialization.Codec
import net.minecraft.client.renderer.DimensionSpecialEffects
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.DeferredRegister.create
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import java.util.function.Supplier

object CForgeLevels : ICPlatformLevels {
    val generator: DeferredRegister<Codec<out ChunkGenerator>> = create(BuiltInRegistries.CHUNK_GENERATOR.key(), MOD_ID)
    val structure: DeferredRegister<StructureType<*>> = create(BuiltInRegistries.STRUCTURE_TYPE.key(), MOD_ID)

    init {
        MOD_BUS.addListener(::onRegisterDimensionSpecialEffects)
        listOf(
            generator,
            structure
        ).forEach { it.register(MOD_BUS) }
    }

    val specialEffects: MutableMap<ResourceLocation, DimensionSpecialEffects> = mutableMapOf()

    fun onRegisterDimensionSpecialEffects(event: RegisterDimensionSpecialEffectsEvent) {
        specialEffects.forEach(event::register)
    }

    override fun registerDimensionSpecialEffects(id: ResourceLocation, effects: DimensionSpecialEffects) {
        specialEffects[id] = effects
    }

    override fun <T : Codec<out ChunkGenerator>> registerChunkGenerator(
        id: ResourceLocation,
        codec: Supplier<T>,
    ): Entry<T> = instance.register(generator, id.path, codec)

    override fun <T : StructureType<*>> registerStructureType(id: ResourceLocation, type: T): Entry<T> =
        instance.register(structure, id.path) { type }

}