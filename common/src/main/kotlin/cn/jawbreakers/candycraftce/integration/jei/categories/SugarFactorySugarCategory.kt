package cn.jawbreakers.candycraftce.integration.jei.categories

import cn.jawbreakers.candycraftce.integration.jei.CJeiRecipes
import cn.jawbreakers.candycraftce.recipe.recipes.SugarFactoryRecipe
import mezz.jei.api.helpers.IGuiHelper

/**
 * Created in 2024/6/30 下午11:43
 * Project: candycraftce
 * 
 * Author [Bread_NiceCat](https://github.com/Bread-Nicecat)
 */
class SugarFactorySugarCategory(helper: IGuiHelper) : SugarFactoryCategory(helper) {
    override fun getRecipeType() = CJeiRecipes.sugar_factory_sugar_jei
    override fun getIcon() = sugaryIcon
    override fun getBg(recipe: SugarFactoryRecipe) = common
//    override fun getProcess(recipe: SugarFactoryRecipe) = sugaryProgress
}
