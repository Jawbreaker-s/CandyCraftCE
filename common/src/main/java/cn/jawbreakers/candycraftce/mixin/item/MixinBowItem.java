package cn.jawbreakers.candycraftce.mixin.item;

import cn.jawbreakers.candycraftce.mixin_stub.BowItemAddition;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BowItem.class)
public abstract class MixinBowItem implements BowItemAddition {


	@SuppressWarnings("ModifyVariableMayUseName")
	@ModifyVariable(
			method = "releaseUsing",
			at = @At(value = "STORE", ordinal = 0)  // 捕获第一次存储 AbstractArrow 的操作
	)
	private AbstractArrow modifyCreatedArrow(AbstractArrow abstractarrow) {
		return candycraftce$customArrow(abstractarrow);
	}

	@Override
	@Unique
	public @NotNull AbstractArrow candycraftce$customArrow(AbstractArrow arrow) {
		return arrow;
	}
}
