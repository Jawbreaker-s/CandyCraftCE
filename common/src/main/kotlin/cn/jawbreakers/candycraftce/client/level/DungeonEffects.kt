package cn.jawbreakers.candycraftce.client.level

import cn.jawbreakers.candycraftce.mixin_stub.ISpecialEffectsAddition
import cn.jawbreakers.candycraftce.utils.CUtils.use
import cn.jawbreakers.candycraftce.utils.ClientOnly
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Camera
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.DimensionSpecialEffects
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f

@ClientOnly
object DungeonEffects : DimensionSpecialEffects(0.0f, false, SkyType.NONE, false, false),
    ISpecialEffectsAddition {
    override fun getBrightnessDependentFogColor(color: Vec3, brightness: Float): Vec3 {
        return Vec3.ZERO
    }

    override fun isFoggyAt(x: Int, z: Int): Boolean = false

    override fun `candycraftce$renderClouds`(
        level: ClientLevel,
        ticks: Int,
        partialTick: Float,
        poseStack: PoseStack,
        camX: Double,
        camY: Double,
        camZ: Double,
        projectionMatrix: Matrix4f,
    ): Boolean = true

    override fun `candycraftce$renderSky`(
        level: ClientLevel,
        ticks: Int,
        partialTick: Float,
        poseStack: PoseStack,
        camera: Camera,
        projectionMatrix: Matrix4f,
        isFoggy: Boolean,
        setupFog: Runnable,
    ): Boolean {
        setupFog.run()
        RenderSystem.depthMask(false)
        RenderSystem.disableBlend()
        RenderSystem.disableCull()
        RenderSystem.setShader { GameRenderer.getPositionShader() }
        RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f)
        poseStack.use {
            drawBlackSkyBox(poseStack.last().pose(), 128.0f)
        }
        RenderSystem.enableCull()
        RenderSystem.depthMask(true)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        return true
    }

    private fun drawBlackSkyBox(matrix: Matrix4f, radius: Float) {
        val tesselator = Tesselator.getInstance()
        val buffer = tesselator.builder
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION)

        buffer.vertex(matrix, -radius, -radius, -radius).endVertex()
        buffer.vertex(matrix, radius, -radius, -radius).endVertex()
        buffer.vertex(matrix, radius, radius, -radius).endVertex()
        buffer.vertex(matrix, -radius, radius, -radius).endVertex()

        buffer.vertex(matrix, radius, -radius, radius).endVertex()
        buffer.vertex(matrix, -radius, -radius, radius).endVertex()
        buffer.vertex(matrix, -radius, radius, radius).endVertex()
        buffer.vertex(matrix, radius, radius, radius).endVertex()

        buffer.vertex(matrix, -radius, -radius, radius).endVertex()
        buffer.vertex(matrix, -radius, -radius, -radius).endVertex()
        buffer.vertex(matrix, -radius, radius, -radius).endVertex()
        buffer.vertex(matrix, -radius, radius, radius).endVertex()

        buffer.vertex(matrix, radius, -radius, -radius).endVertex()
        buffer.vertex(matrix, radius, -radius, radius).endVertex()
        buffer.vertex(matrix, radius, radius, radius).endVertex()
        buffer.vertex(matrix, radius, radius, -radius).endVertex()

        buffer.vertex(matrix, -radius, radius, -radius).endVertex()
        buffer.vertex(matrix, radius, radius, -radius).endVertex()
        buffer.vertex(matrix, radius, radius, radius).endVertex()
        buffer.vertex(matrix, -radius, radius, radius).endVertex()

        buffer.vertex(matrix, -radius, -radius, radius).endVertex()
        buffer.vertex(matrix, radius, -radius, radius).endVertex()
        buffer.vertex(matrix, radius, -radius, -radius).endVertex()
        buffer.vertex(matrix, -radius, -radius, -radius).endVertex()
        tesselator.end()
    }

}