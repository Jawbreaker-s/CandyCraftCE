package cn.jawbreakers.candycraftce.mixin.render;

import cn.jawbreakers.candycraftce.client.entity.layers.CandyProjectileStuckLayer;
import cn.jawbreakers.candycraftce.client.entity.layers.PropolisEntityOverlayLayer;
import cn.jawbreakers.candycraftce.client.entity.layers.PropolisSlimeGlintLayer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Created in 2026/10/3 10:29 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcher {


	@Shadow
	private Map<EntityType<?>, EntityRenderer<?>> renderers;


	@Shadow
	@Final
	private EntityModelSet entityModels;

	@Shadow
	private Map<String, EntityRenderer<? extends Player>> playerRenderers;

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Inject(method = "onResourceManagerReload", at = @At("TAIL"))
	private void loadCandyLayers(ResourceManager resourceManager, CallbackInfo ci) {
		EntityRenderDispatcher dispatcher = (EntityRenderDispatcher) (Object) this;

		for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
			EntityRenderer<?> r = renderers.get(type);
			if (r instanceof LivingEntityRenderer<?, ?> renderer) {
				var accessor = (LivingEntityRendererAccessor) renderer;
				if (renderer instanceof SlimeRenderer slime) {
					accessor.callAddLayer(new PropolisSlimeGlintLayer(slime, entityModels));
				} else {
					accessor.callAddLayer(new PropolisEntityOverlayLayer<>(renderer));
				}
			}
		}
		for (Map.Entry<String, EntityRenderer<? extends Player>> entry : playerRenderers.entrySet()) {
			if (entry.getValue() instanceof LivingEntityRenderer renderer) {
				var accessor = (LivingEntityRendererAccessor) renderer;
				accessor.callAddLayer(new CandyProjectileStuckLayer(dispatcher, renderer));
				accessor.callAddLayer(new PropolisEntityOverlayLayer<>(renderer));
			}
		}
	}

}
