package cn.jawbreakers.candycraftce.client.render

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.mixin.render.RenderTypeAccessor
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.ClientOnly
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import org.joml.Matrix4f

@ClientOnly
class PropolisGlintRenderType private constructor() : RenderStateShard.TexturingStateShard(
    "$MOD_ID:propolis_glint_texturing",
    ::setupPropolisGlintTexturing,
    RenderSystem::resetTextureMatrix
) {
    companion object {
        private val PROPOLIS_GLINT_TEXTURE = "textures/misc/propolis_glint.png".modLoc()
        private const val DEFAULT_SIZE_PERCENT = 115;
        val PROPOLIS_GLINT_TEXTURING = PropolisGlintRenderType()
        val PROPOLIS_ENTITY_GLINT: RenderType.CompositeRenderType = RenderTypeAccessor.callCreate(
            "$MOD_ID:propolis_entity_glint",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER)
                .setTextureState(TextureStateShard(PROPOLIS_GLINT_TEXTURE, true, false))
                .setWriteMaskState(COLOR_WRITE)
                .setCullState(NO_CULL)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setTransparencyState(GLINT_TRANSPARENCY)
                .setTexturingState(PROPOLIS_GLINT_TEXTURING)
                .setLayeringState(POLYGON_OFFSET_LAYERING)
                .createCompositeState(false)
        )

        private fun setupPropolisGlintTexturing() {
            val time = (Util.getMillis() * Minecraft.getInstance().options.glintSpeed().get() * 8.0).toLong()
            val horizontal = (time % 110000L) / 110000.0f
            val vertical = (time % 30000L) / 30000.0f
            val matrix = Matrix4f()
                .translation(-horizontal, vertical, 0.0f)
                .rotateZ(0.17453292f)
                .scale(0.16F * 100.0F / DEFAULT_SIZE_PERCENT)
            RenderSystem.setTextureMatrix(matrix)
        }
    }
}
