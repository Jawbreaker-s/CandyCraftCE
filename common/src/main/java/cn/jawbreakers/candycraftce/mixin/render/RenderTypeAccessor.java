package cn.jawbreakers.candycraftce.mixin.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Created in 2026/10/3 13:52 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@Mixin(RenderType.class)
public interface RenderTypeAccessor {
	@Invoker("create")
	static RenderType.CompositeRenderType callCreate(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, RenderType.CompositeState state) {
		throw new AssertionError("Mixin failed to inject");
	}
}
