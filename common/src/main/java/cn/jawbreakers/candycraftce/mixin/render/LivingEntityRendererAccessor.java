package cn.jawbreakers.candycraftce.mixin.render;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Created in 2026/10/3 11:00 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor {
	@Invoker("addLayer")
	boolean callAddLayer(RenderLayer<?, ?> layer);
}
