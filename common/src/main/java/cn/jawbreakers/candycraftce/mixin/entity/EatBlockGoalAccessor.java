package cn.jawbreakers.candycraftce.mixin.entity;

import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Created in 2026/9/30 22:23 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@Mixin(EatBlockGoal.class)
public interface EatBlockGoalAccessor {

	@Accessor("eatAnimationTick")
	int getEatAnimationTick();

	@Accessor("eatAnimationTick")
	void setEatAnimationTick(int eatAnimationTick);
}
