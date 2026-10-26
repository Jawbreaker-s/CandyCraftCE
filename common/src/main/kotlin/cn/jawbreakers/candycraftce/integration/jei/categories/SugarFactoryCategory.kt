package cn.jawbreakers.candycraftce.integration.jei.categories

import cn.jawbreakers.candycraftce.client.screen.AdvancedFactoryScreen.Companion.ADVANCED_STYLE
import cn.jawbreakers.candycraftce.client.screen.SugarFactoryScreen.Companion.COMMON_STYLE
import cn.jawbreakers.candycraftce.integration.jei.CJeiPlugin.Companion.jeiTex
import cn.jawbreakers.candycraftce.integration.jei.CJeiRecipes
import cn.jawbreakers.candycraftce.integration.jei.utils.ScaledDrawable
import cn.jawbreakers.candycraftce.recipe.recipes.SugarFactoryRecipe
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultInstance
import cn.jawbreakers.candycraftce.registry.CBlocks.sugar_factory
import cn.jawbreakers.candycraftce.utils.TickUnit.SEC2TICK
import mezz.jei.api.constants.VanillaTypes
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.gui.drawable.IDrawable
import mezz.jei.api.gui.drawable.IDrawableAnimated
import mezz.jei.api.gui.drawable.IDrawableStatic
import mezz.jei.api.gui.ingredient.IRecipeSlotsView
import mezz.jei.api.helpers.IGuiHelper
import mezz.jei.api.recipe.IFocusGroup
import mezz.jei.api.recipe.RecipeIngredientRole
import mezz.jei.api.recipe.category.IRecipeCategory
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

/**
 * Created in 2024/6/6 下午1:05
 * Project: candycraftce
 * 
 * Author [Bread_NiceCat](https://github.com/Bread-Nicecat)
 */
open class SugarFactoryCategory(guiHelper: IGuiHelper) : IRecipeCategory<SugarFactoryRecipe> {
    companion object {
        val TITLE: Component = sugar_factory.get().name
    }

    protected val ticksPerCycle = 4 * SEC2TICK

    protected val common: IDrawableStatic =
        guiHelper.createDrawable(jeiTex, 0, 0, 174, 30)
    protected val advanced: IDrawableStatic =
        guiHelper.createDrawable(jeiTex, 0, 30, 174, 30)
    protected val commonProgress: IDrawableAnimated =
        guiHelper.createAnimatedDrawable(
            guiHelper.createDrawable(COMMON_STYLE, 0, 114, 120, 12),
            ticksPerCycle,
            IDrawableAnimated.StartDirection.LEFT,
            false
        )
    protected val advancedProgress: IDrawableAnimated =
        guiHelper.createAnimatedDrawable(
            guiHelper.createDrawable(ADVANCED_STYLE, 0, 114, 120, 12),
            ticksPerCycle,
            IDrawableAnimated.StartDirection.LEFT,
            false
        )

    //    protected val sugaryProgress: IDrawableAnimated =
//        guiHelper.createAnimatedDrawable(
//            guiHelper.createDrawable(COMMON_STYLE, 0, 126, 120, 12),
//            ticksPerCycle,
//            IDrawableAnimated.StartDirection.LEFT,
//            false
//        )
    protected val bg: IDrawableStatic =
        guiHelper.createBlankDrawable(180, 32)
    protected val commonIcon: IDrawable =
        guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, sugar_factory.defaultInstance)

    //    protected val advancedIcon: IDrawable =
//        guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, advanced_sugar_factory.defaultInstance)
    protected val sugaryIcon: IDrawable =
        ScaledDrawable(guiHelper.createDrawable(jeiTex, 0, 141, 32, 32), 2)

    override fun getRecipeType() = CJeiRecipes.sugar_factory_jei
    override fun getBackground() = bg
    override fun getTitle() = TITLE
    override fun getIcon() = commonIcon
    override fun setRecipe(builder: IRecipeLayoutBuilder, recipe: SugarFactoryRecipe, focuses: IFocusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 7 + 3, 7 + 1).addIngredients(recipe.ingredient)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 151 + 3, 7 + 1).addItemStack(recipe.result.defaultInstance)
    }

    override fun draw(
        recipe: SugarFactoryRecipe,
        recipeSlotsView: IRecipeSlotsView,
        guiGraphics: GuiGraphics,
        mouseX: Double,
        mouseY: Double,
    ) {
        getBg(recipe).draw(guiGraphics, 3, 1)
        getProcess(recipe).draw(guiGraphics, 27 + 3, 9 + 1)
    }

    protected open fun getBg(recipe: SugarFactoryRecipe): IDrawable {
        return if (recipe.advanced) advanced else common
    }

    protected open fun getProcess(recipe: SugarFactoryRecipe): IDrawable {
        return if (recipe.advanced) advancedProgress else commonProgress
    }
}
