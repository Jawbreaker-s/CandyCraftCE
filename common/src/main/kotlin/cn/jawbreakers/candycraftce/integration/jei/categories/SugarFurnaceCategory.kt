package cn.jawbreakers.candycraftce.integration.jei.categories

import cn.jawbreakers.candycraftce.client.screen.ChocolateFurnaceScreen.Companion.CHOCOLATE
import cn.jawbreakers.candycraftce.integration.jei.CJeiPlugin.Companion.jeiTex
import cn.jawbreakers.candycraftce.integration.jei.CJeiRecipes
import cn.jawbreakers.candycraftce.recipe.recipes.SugarFurnaceRecipe
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultInstance
import cn.jawbreakers.candycraftce.registry.CSugarFuels
import cn.jawbreakers.candycraftce.utils.TickUnit.SEC2TICK
import cn.jawbreakers.candycraftce.utils.TickUnit.TICK2SEC
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
 * 
 */
class SugarFurnaceCategory(helper: IGuiHelper) : IRecipeCategory<SugarFurnaceRecipe> {
    companion object {
        val TITLE: Component = Component.translatable("jei.sugar_furnace.title")
    }

    private val tickPerCycle = 4 * SEC2TICK
    private val bg: IDrawableStatic =
        helper.createDrawable(jeiTex, 0, 60, 176, 80)
    private val icon: IDrawable =
        helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, CBlocks.licorice_furnace.defaultInstance)
    private val fireAnim: IDrawableAnimated =
        helper.createAnimatedDrawable(
            helper.createDrawable(CHOCOLATE, 176, 0, 14, 14),
            tickPerCycle,
            IDrawableAnimated.StartDirection.TOP,
            true
        )
    private val progressAnim: IDrawableAnimated =
        helper.createAnimatedDrawable(
            helper.createDrawable(CHOCOLATE, 176, 14, 22, 16),
            tickPerCycle,
            IDrawableAnimated.StartDirection.LEFT,
            false
        )

    override fun getRecipeType() = CJeiRecipes.sugar_furnace_jei
    override fun getTitle(): Component = TITLE
    override fun getBackground(): IDrawable = bg
    override fun getIcon() = icon

    override fun setRecipe(builder: IRecipeLayoutBuilder, recipe: SugarFurnaceRecipe, focuses: IFocusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 56, 17).addIngredients(recipe.ingredient)
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 56, 53)
            .addItemStacks(CSugarFuels.listFuels().map { it.key.defaultInstance }.toList())
            .addTooltipCallback { ingredients, tooltip ->
                ingredients.displayedItemStack.ifPresent { fuel ->
                    tooltip.add(Component.literal((CSugarFuels.getBurnDuration(fuel) * TICK2SEC).toString() + "s"))
                }
            }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 116, 35).addItemStack(recipe.result.defaultInstance)
    }

    override fun draw(
        recipe: SugarFurnaceRecipe,
        recipeSlotsView: IRecipeSlotsView,
        guiGraphics: GuiGraphics,
        mouseX: Double,
        mouseY: Double,
    ) {
        fireAnim.draw(guiGraphics, 56, 36)
        progressAnim.draw(guiGraphics, 80, 35)
    }
}
