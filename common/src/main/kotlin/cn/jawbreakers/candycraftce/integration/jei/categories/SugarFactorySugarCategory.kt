package cn.jawbreakers.candycraftce.integration.jei.categories

import cn.jawbreakers.candycraftce.integration.jei.CJeiPlugin.Companion.sugar_factory_sugar_jei
import cn.jawbreakers.candycraftce.recipe.recipes.SugarFactoryRecipe
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultInstance
import cn.jawbreakers.candycraftce.utils.CUtils.use
import mezz.jei.api.gui.drawable.IDrawable
import mezz.jei.api.helpers.IGuiHelper
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.Items

/**
 * Created in 2024/6/30 下午11:43
 * Project: candycraftce
 * 
 * Author [Bread_NiceCat](https://github.com/Bread-Nicecat)
 */
class SugarFactorySugarCategory(helper: IGuiHelper) : SugarFactoryCategory(helper) {
    private val sugaryIcon: IDrawable =
        //ScaledDrawable(helper.createDrawable(jeiTex, 0, 141, 32, 32), 2)
        SugaryIcon(
            helper.createDrawableItemStack(CBlocks.sugar_factory.defaultInstance),
            helper.createDrawableItemStack(Items.SUGAR.defaultInstance)
        )

    //    protected val sugaryProgress: IDrawableAnimated =
//        guiHelper.createAnimatedDrawable(
//            guiHelper.createDrawable(COMMON_STYLE, 0, 126, 120, 12),
//            ticksPerCycle,
//            IDrawableAnimated.StartDirection.LEFT,
//            false
//        )
    override fun getRecipeType() = sugar_factory_sugar_jei
    override fun getIcon() = sugaryIcon
    override fun getBg(recipe: SugarFactoryRecipe) = common
//    override fun getProcess(recipe: SugarFactoryRecipe) = sugaryProgress
}

/**
 * Created in 2026/10/29 06:31 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class SugaryIcon(val machine: IDrawable, val sugar: IDrawable) : IDrawable {
    override fun getWidth() = machine.width

    override fun getHeight() = machine.height

    override fun draw(guiGraphics: GuiGraphics, xOffset: Int, yOffset: Int) {
        guiGraphics.pose().use {
            translate(xOffset + width / 2f, yOffset + height / 2f, 20f)
            scale(0.5f, 0.5f, 1f)
            sugar.draw(guiGraphics)
        }
        machine.draw(guiGraphics, xOffset, yOffset)
    }
}