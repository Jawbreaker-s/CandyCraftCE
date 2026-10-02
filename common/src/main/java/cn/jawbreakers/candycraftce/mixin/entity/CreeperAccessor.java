package cn.jawbreakers.candycraftce.mixin.entity;

import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Created in 2026/9/30 23:13 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@Mixin(Creeper.class)
public interface CreeperAccessor {
	@Accessor("maxSwell")
	void setMaxSwell(int maxSwell);

	@Accessor("explosionRadius")
	void setExplosionRadius(int explosionRadius);
}
