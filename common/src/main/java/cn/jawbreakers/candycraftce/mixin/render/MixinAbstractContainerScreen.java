package cn.jawbreakers.candycraftce.mixin.render;

import cn.jawbreakers.candycraftce.menu.slot.SimpleResultSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * Created in 2026/10/24 19:44 by <a href="https://github.com/Bread-NiceCat">Bread_NiceCat</a>
 * Project: CandyCraftCE
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen {
	@Shadow
	@Final
	protected AbstractContainerMenu menu;

	@Shadow
	@Nullable
	protected Slot hoveredSlot;

	@Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
	void renderSlot(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
		if (slot.isActive())
			if (slot instanceof SimpleResultSlot srs) {
				srs.renderExtra(guiGraphics);
			}
	}

	@Inject(method = "renderTooltip", at = @At(value = "HEAD"))
	void renderTooltip(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci) {
		if (menu.getCarried().isEmpty() && hoveredSlot != null) {
			if (hoveredSlot instanceof SimpleResultSlot srs) {
				srs.renderExtraTooltip(guiGraphics, x, y);
			}
		}
	}

}
