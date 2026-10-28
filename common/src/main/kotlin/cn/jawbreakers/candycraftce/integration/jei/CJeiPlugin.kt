package cn.jawbreakers.candycraftce.integration.jei

import cn.jawbreakers.candycraftce.CandyCraftCE.onPostWork
import cn.jawbreakers.candycraftce.blockentity.SugarFactoryBE
import cn.jawbreakers.candycraftce.client.screen.LicoriceFurnaceScreen
import cn.jawbreakers.candycraftce.client.screen.SugarFactoryScreen
import cn.jawbreakers.candycraftce.integration.jei.CJeiRecipes.sugar_factory_jei
import cn.jawbreakers.candycraftce.integration.jei.CJeiRecipes.sugar_factory_sugar_jei
import cn.jawbreakers.candycraftce.integration.jei.CJeiRecipes.sugar_furnace_jei
import cn.jawbreakers.candycraftce.integration.jei.categories.SugarFactoryCategory
import cn.jawbreakers.candycraftce.integration.jei.categories.SugarFactorySugarCategory
import cn.jawbreakers.candycraftce.integration.jei.categories.SugarFurnaceCategory
import cn.jawbreakers.candycraftce.recipe.recipes.SugarFactoryRecipe
import cn.jawbreakers.candycraftce.registry.CBlocks.advanced_sugar_factory
import cn.jawbreakers.candycraftce.registry.CBlocks.chocolate_furnace
import cn.jawbreakers.candycraftce.registry.CBlocks.licorice_furnace
import cn.jawbreakers.candycraftce.registry.CBlocks.sugar_factory
import cn.jawbreakers.candycraftce.registry.CBlocks.white_chocolate_furnace
import cn.jawbreakers.candycraftce.registry.CItems.asItem
import cn.jawbreakers.candycraftce.registry.CRecipeTypes.sugar_factory_type
import cn.jawbreakers.candycraftce.registry.CRecipeTypes.sugar_furnace_type
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.translate
import cn.jawbreakers.candycraftce.utils.CUtils.guiTex
import cn.jawbreakers.candycraftce.utils.CUtils.key
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.registration.IGuiHandlerRegistration
import mezz.jei.api.registration.IRecipeCatalystRegistration
import mezz.jei.api.registration.IRecipeCategoryRegistration
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.client.Minecraft
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient


/**
 * Created in 2023/9/30 15:44
 * Project: candycraftce
 *
 * Author [Bread_NiceCat](https://github.com/Bread-Nicecat)
 */
@JeiPlugin
class CJeiPlugin : IModPlugin {
    companion object {
        val uid = "jei-plugin".modLoc()
        val jeiTex = "jei".modLoc().guiTex()

        init {
            onPostWork {
                SugarFurnaceCategory.TITLE.translate("Sugar Factory", "糖熔炉")
            }
        }
    }

    override fun getPluginUid() = uid

    override fun registerRecipes(registration: IRecipeRegistration) {
        val manager = Minecraft.getInstance().level!!.recipeManager
        registration.addRecipes(sugar_furnace_jei, manager.getAllRecipesFor(sugar_furnace_type.type))

        val factoryRecipes = manager.getAllRecipesFor(sugar_factory_type.type)
        registration.addRecipes(sugar_factory_jei, factoryRecipes)

        val factoryResults = factoryRecipes.asSequence().map { it.result }.toHashSet()
        registration.addRecipes(
            sugar_factory_sugar_jei, buildList {
                SugarFactoryBE.sugaryRecipe.ingredient.items.asSequence()
                    .map { it.item }
                    .filter { it !in factoryResults }
                    .map {
                        SugarFactoryRecipe(
                            it.key.withSuffix("__sugary"),
                            Ingredient.of(it),
                            Items.SUGAR, 1, false
                        )
                    }
                    .forEach { add(it) }
            }
        )
    }

    override fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
        listOf(sugar_factory, advanced_sugar_factory).forEach {
            registration.addRecipeCatalyst(
                it.asItem().defaultInstance,
                sugar_factory_jei, sugar_factory_sugar_jei
            )
        }
        listOf(chocolate_furnace, white_chocolate_furnace, licorice_furnace).forEach {
            registration.addRecipeCatalyst(
                it.asItem().defaultInstance,
                sugar_furnace_jei
            )
        }
    }

    override fun registerCategories(registration: IRecipeCategoryRegistration) {
        val helper = registration.jeiHelpers.guiHelper
        registration.addRecipeCategories(
            SugarFurnaceCategory(helper),
            SugarFactoryCategory(helper),
            SugarFactorySugarCategory(helper)
        )
    }

    override fun registerGuiHandlers(registration: IGuiHandlerRegistration) {
        registration.addRecipeClickArea(
            SugarFactoryScreen::class.java,
            27, 9, 120, 12,
            sugar_factory_jei
        )
        registration.addRecipeClickArea(
            LicoriceFurnaceScreen::class.java,
            80, 35, 22, 16,
            sugar_furnace_jei
        )
    }
}
