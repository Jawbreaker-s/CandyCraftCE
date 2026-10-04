package cn.jawbreakers.candycraftce.mixin.item;

import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Created in 2026/10/4 13:18 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@Mixin(ItemProperties.class)
public interface ItemPropertiesAccessor {
	@Invoker("register")
	static void callRegister(Item item, ResourceLocation name, ClampedItemPropertyFunction property) {
		throw new AssertionError("Failed to mixin");
	}
}
