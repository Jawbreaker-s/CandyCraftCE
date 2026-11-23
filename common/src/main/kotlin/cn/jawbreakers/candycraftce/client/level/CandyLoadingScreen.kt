package cn.jawbreakers.candycraftce.client.level

import cn.jawbreakers.candycraftce.mixin.loading_screen.MixinScreen
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.worldgen.CLevels
import cn.jawbreakers.candycraftce.utils.ClientOnly
import cn.jawbreakers.candycraftce.utils.UsedByMixin
import cn.jawbreakers.candycraftce.utils.registry.Entry
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.GenericDirtMessageScreen
import net.minecraft.client.gui.screens.ProgressScreen
import net.minecraft.client.gui.screens.ReceivingLevelScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block


/**
 * Created in 2026/10/1 14:57 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@ClientOnly
object CandyLoadingScreen {
    private val pudding_top = CBlocks.custard_pudding_block.getBlockTexture("_side")
    private val pudding = CBlocks.pudding_block.getBlockTexture()
    private val jawbreaker = CBlocks.jawbreaker_block.getBlockTexture()
    private val jawbreaker_light = CBlocks.jawbreaker_light.getBlockTexture()

    private fun renderDungeonLoadingBackground(graphics: GuiGraphics, width: Int, height: Int) {
        renderTiledBackground(graphics, jawbreaker, width, height, 32, -0x1)
        val tile = 32
        var x = 0
        while (x < width + tile) {
            var y = ((x / tile) % 2) * tile * 2
            while (y < height + tile) {
                graphics.blit(jawbreaker_light, x, y, 0f, 0f, tile, tile, tile, tile)
                y += tile * 4
            }
            x += tile * 4
        }
    }

    private fun renderCandyWorldLoadingBackground(graphics: GuiGraphics, width: Int, height: Int) {
        val tile = 32
        renderTiledBackground(graphics, pudding, width, height, tile, -0x1)
        var x = 0
        while (x < width + tile) {
            graphics.blit(pudding_top, x, 0, 0f, 0f, tile, tile, tile, tile)
            x += tile
        }
    }

    private fun renderTiledBackground(
        graphics: GuiGraphics,
        texture: ResourceLocation,
        width: Int,
        height: Int,
        tile: Int,
        color: Int,
    ) {
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        var x = 0
        while (x < width + tile) {
            var y = 0
            while (y < height + tile) {
                graphics.blit(texture, x, y, 0f, 0f, tile, tile, tile, tile)
                y += tile
            }
            x += tile
        }
        if ((color ushr 24) < 255) {
            graphics.fill(0, 0, width, height, color)
        }
        RenderSystem.disableBlend()
    }

    fun isLoadingScreen(screen: Screen): Boolean {
        return screen is GenericDirtMessageScreen
                || screen is ProgressScreen
                || screen is ReceivingLevelScreen
    }

    @UsedByMixin(MixinScreen::class)
    fun renderCandyBackground(graphics: GuiGraphics, level: ClientLevel, screen: Screen): Boolean {
        if (!isLoadingScreen(screen)) return false
        val minecraft = Minecraft.getInstance()
        val dim = level.dimension()
        val width = minecraft.window.guiScaledWidth
        val height = minecraft.window.guiScaledHeight

        when (dim) {
            CLevels.dungeons -> renderDungeonLoadingBackground(graphics, width, height)
            CLevels.candyland -> renderCandyWorldLoadingBackground(graphics, width, height)
            else -> return false
        }
        return true
    }

}

private fun Entry<out Block>.getBlockTexture(suffix: String = ""): ResourceLocation =
    id.withPath { "textures/block/$it$suffix.png" }
