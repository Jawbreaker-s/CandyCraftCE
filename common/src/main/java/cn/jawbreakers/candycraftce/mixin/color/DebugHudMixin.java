package cn.jawbreakers.candycraftce.mixin.color;

import cn.jawbreakers.candycraftce.client.PuddingColor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugHudMixin {
	@ModifyVariable(
			method = "getSystemInformation",
			at = @At(
					value = "INVOKE_ASSIGN",
					target = "Lcom/google/common/collect/Lists;newArrayList([Ljava/lang/Object;)Ljava/util/ArrayList;", remap = false
			),
			name = "list"
	)
	protected List<String> getLeftText(List<String> list) {
		PuddingColor.INSTANCE.addDebugScreenInfo(list);
		return list;
	}
}