package cn.jawbreakers.candycraftce.mixin.render;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Created in 2026/10/23 20:48 by <a href="https://github.com/Bread-NiceCat">Bread_NiceCat</a>
 * Project: CandyCraftCE
 */
@Mixin(MenuScreens.class)
public interface MenuScreensAccessor {
	@Invoker("register")
	static <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, MenuScreens.ScreenConstructor<M, U> factory) {
		throw new AssertionError("Failed to mixin");
	}
}
