package cn.jawbreakers.candycraftce.mixin.level;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Created in 2026/10/9 20:22 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@Mixin(Level.class)
public interface LevelMixinAccessor {
	@Invoker("getEntities")
	LevelEntityGetter<Entity> callGetEntities();
}
