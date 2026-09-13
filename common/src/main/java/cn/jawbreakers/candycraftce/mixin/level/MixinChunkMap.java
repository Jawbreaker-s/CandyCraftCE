package cn.jawbreakers.candycraftce.mixin.level;

import cn.jawbreakers.candycraftce.level.CandyChunkGenerator;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChunkMap.class)
public abstract class MixinChunkMap {

	@Shadow
	private ChunkGenerator generator;

	@ModifyArg(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/levelgen/RandomState;create(Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;Lnet/minecraft/core/HolderGetter;J)Lnet/minecraft/world/level/levelgen/RandomState;"
			),
			index = 0
	)
	private NoiseGeneratorSettings applySettings(NoiseGeneratorSettings original) {
		if (generator instanceof CandyChunkGenerator ccg) {
			return ccg.getSettings().value();
		}
		return original;
	}
}
