package cn.jawbreakers.candycraftce.mixin.level;

import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created in 2026/9/21 23:52 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@Mixin(Biome.class)
public class MixinBiome {
	@Inject(method = "shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Z",
			at = @At("HEAD"),
			cancellable = true)
	private void shouldFreeze(LevelReader level, BlockPos water, boolean mustBeAtEdge, CallbackInfoReturnable<Boolean> cir) {
		Registry<Biome> biomes = level.registryAccess().registry(Registries.BIOME).orElse(null);
		if (biomes != null) {
			ResourceKey<Biome> key = biomes.getResourceKey((Biome) (Object) this).orElse(null);
			if (key != null && CBiomes.INSTANCE.getAllCandyBiomes().contains(key)) {
				cir.setReturnValue(false);
			}
		}
	}
}
