package cn.jawbreakers.candycraftce.fabric.mixin.crossbow;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Created in 2026/10/4 15:11 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 * 解决弩无法正常渲染的问题
 */
@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRenderer {
	@WrapOperation(
			method = "renderArmWithItem",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
	)
	private boolean fix(ItemStack instance, Item item, Operation<Boolean> original) {
		if (item == Items.CROSSBOW) {
			return instance.getItem() instanceof CrossbowItem;
		} else {
			return original.call(instance, item);
		}
	}
}
