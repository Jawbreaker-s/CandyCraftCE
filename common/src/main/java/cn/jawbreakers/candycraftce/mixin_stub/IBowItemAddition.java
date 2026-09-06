package cn.jawbreakers.candycraftce.mixin_stub;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.jetbrains.annotations.NotNull;

public interface IBowItemAddition {

	@NotNull AbstractArrow candycraftce$customArrow(AbstractArrow arrow);
}
