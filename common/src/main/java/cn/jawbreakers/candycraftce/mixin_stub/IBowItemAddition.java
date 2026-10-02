package cn.jawbreakers.candycraftce.mixin_stub;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.jetbrains.annotations.NotNull;

public interface IBowItemAddition {

	default @NotNull AbstractArrow candycraftce$customArrow(AbstractArrow arrow) {
		return arrow;
	}
}
