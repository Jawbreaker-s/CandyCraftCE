package cn.jawbreakers.candycraftce.mixin;

import cn.jawbreakers.candycraftce.mixin_stub.BowItemAddition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BowItem.class)
public abstract class MixinBowItem implements BowItemAddition {

	@Redirect(
			method = "releaseUsing",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ArrowItem;createArrow(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/entity/projectile/AbstractArrow;"
			)
	)
	private AbstractArrow redirectCreateArrow(ArrowItem instance, Level level, ItemStack stack, LivingEntity shooter) {
		AbstractArrow arrow = instance.createArrow(level, stack, shooter);
		return candycraftce$customArrow(arrow);
	}


	@Override
	@Unique
	public @NotNull AbstractArrow candycraftce$customArrow(AbstractArrow arrow) {
		return arrow;
	}
}
