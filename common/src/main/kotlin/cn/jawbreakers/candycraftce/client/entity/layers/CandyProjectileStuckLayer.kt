package cn.jawbreakers.candycraftce.client.entity.layers

import cn.jawbreakers.candycraftce.entity.HoneyArrowEntity
import cn.jawbreakers.candycraftce.entity.HoneyBoltEntity
import cn.jawbreakers.candycraftce.mixin_stub.ICandyStuckProjectileCarrier
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.PlayerModel
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer
import net.minecraft.util.Mth
import net.minecraft.util.Mth.atan2
import net.minecraft.world.entity.Entity

class CandyProjectileStuckLayer(
    private val dispatcher: EntityRenderDispatcher,
    parent: LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>,
) : StuckInBodyLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>(parent) {

    private var honeyArrowsToRender = 0
    private var renderedItems = 0

    /**
     *每次渲染都会先调用这个方法
     *[renderedItems]每帧会归零
     */
    override fun numStuck(player: AbstractClientPlayer): Int {
        val carrier = player as ICandyStuckProjectileCarrier
        honeyArrowsToRender = carrier.`candycraftce$getHoneyArrowCount`()
        renderedItems = 0
        return honeyArrowsToRender + carrier.`candycraftce$getHoneyBoltCount`()
    }

    override fun renderStuckItem(
        poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int,
        target: Entity, x: Float, y: Float, z: Float, partialTick: Float,
    ) {
        val projectile = if (renderedItems++ < honeyArrowsToRender) {
            HoneyArrowEntity(target.level(), target.x, target.y, target.z)
        } else {
            HoneyBoltEntity(target.level(), target.x, target.y, target.z)
        }

        val horizontal = Mth.sqrt(x * x + z * z)
        projectile.yRot = (atan2(x.toDouble(), z.toDouble()) * Mth.RAD_TO_DEG).toFloat()
        projectile.xRot = (atan2(y.toDouble(), horizontal.toDouble()) * Mth.RAD_TO_DEG).toFloat()
        projectile.yRotO = projectile.yRot
        projectile.xRotO = projectile.xRot
        dispatcher.render(
            projectile, 0.0, 0.0, 0.0, 0.0f, partialTick,
            poseStack, buffer, packedLight
        )
    }
}
