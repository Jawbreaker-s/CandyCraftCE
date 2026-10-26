package cn.jawbreakers.candycraftce.integration.jei

import cn.jawbreakers.candycraftce.recipe.recipes.SugarFactoryRecipe
import cn.jawbreakers.candycraftce.recipe.recipes.SugarFurnaceRecipe
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import mezz.jei.api.recipe.RecipeType

/**
 * Created in 2024/2/25 8:44
 * Project: candycraftce
 * 
 * Author [Bread_NiceCat](https://github.com/BreadNiceCat)
 *
 */
object CJeiRecipes {
    val sugar_factory_jei = RecipeType("sugar_factory".modLoc(), SugarFactoryRecipe::class.java)
    val sugar_factory_sugar_jei = RecipeType("sugar_factory_sugar".modLoc(), SugarFactoryRecipe::class.java)
    val sugar_furnace_jei = RecipeType("sugar_furnace".modLoc(), SugarFurnaceRecipe::class.java)
}
