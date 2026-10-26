package cn.jawbreakers.candycraftce.integration.jei.utils

import cn.jawbreakers.candycraftce.utils.CUtils.use
import mezz.jei.api.gui.drawable.IDrawable
import net.minecraft.client.gui.GuiGraphics

class ScaledDrawable(drawable: IDrawable, division: Int) : IDrawable {
    private val drawable: IDrawable
    private val division: Int

    init {
        val width = drawable.width
        val height = drawable.height
        require(width % division == 0) { "drawable width $width must be divisible by the division $division" }
        require(height % division == 0) { "drawable height $height must be divisible by the division $division" }
        this.drawable = drawable
        this.division = division
    }

    override fun getWidth(): Int = drawable.width / division

    override fun getHeight(): Int = drawable.height / division

    override fun draw(guiGraphics: GuiGraphics, xOffset: Int, yOffset: Int) {
        val poseStack = guiGraphics.pose()
        poseStack.use {
            poseStack.translate(xOffset.toFloat(), yOffset.toFloat(), 0f)
            poseStack.scale(1 / division.toFloat(), 1 / division.toFloat(), 1f)
            drawable.draw(guiGraphics)
        }
    }

    override fun draw(guiGraphics: GuiGraphics) {
        val poseStack = guiGraphics.pose()
        poseStack.use {
            poseStack.scale(1 / division.toFloat(), 1 / division.toFloat(), 1f)
            drawable.draw(guiGraphics)
        }
    }
}