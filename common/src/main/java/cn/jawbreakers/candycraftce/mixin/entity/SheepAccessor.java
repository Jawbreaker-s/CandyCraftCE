package cn.jawbreakers.candycraftce.mixin.entity;

import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.animal.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Created in 2026/9/30 22:09 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@Mixin(Sheep.class)
public interface SheepAccessor {
	@Accessor("eatBlockGoal")
	void setEatBlockGoal(EatBlockGoal goal);

	@Accessor("eatBlockGoal")
	EatBlockGoal getEatBlockGoal();
}
