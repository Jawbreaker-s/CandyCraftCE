package cn.jawbreakers.candycraftce.level.structure

import cn.jawbreakers.candycraftce.registry.CLevels
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureType
import java.util.*

class FeatureLocatorStructure(
    settings: StructureSettings,
    val feature: ResourceLocation,
) : Structure(settings) {
    companion object {
        val codec: Codec<FeatureLocatorStructure> =
            RecordCodecBuilder.create { instance ->
                instance.group(
                    settingsCodec(instance),
                    ResourceLocation.CODEC.fieldOf("feature").forGetter { it.feature }
                ).apply(instance, ::FeatureLocatorStructure)
            }
    }


    override fun findGenerationPoint(context: GenerationContext): Optional<GenerationStub> {
        return Optional.empty()
    }

    override fun type(): StructureType<*> {
        return CLevels.feature_locator_type.get()
    }
}
