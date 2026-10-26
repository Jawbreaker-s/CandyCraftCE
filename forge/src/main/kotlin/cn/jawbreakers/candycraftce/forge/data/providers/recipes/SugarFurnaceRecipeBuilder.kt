package cn.breadnicecat.candycraftce.data.recipes

import cn.jawbreakers.candycraftce.forge.data.providers.recipes.SerializableFinishedRecipe
import cn.jawbreakers.candycraftce.recipe.recipes.SugarFurnaceRecipe
import cn.jawbreakers.candycraftce.registry.CRecipeTypes
import net.minecraft.advancements.CriterionTriggerInstance
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike
import java.util.function.Consumer


/**
 * Created by NiceCat on 2026/3/23.
 * Project: candycraftce
 * @author <a href="https://github.com/BreadNiceCat">Bread_NiceCat</a>
 *
 */
class SugarFurnaceRecipeBuilder : RecipeBuilder {
    companion object {
        inline fun builder(action: SugarFurnaceRecipeBuilder.() -> Unit): SugarFurnaceRecipeBuilder {
            return SugarFurnaceRecipeBuilder().apply(action)
        }

        fun direct(
            result: ItemLike,
            ingredient: Ingredient,
            exp: Float = 0f,
            count: Int = 1,
        ): SugarFurnaceRecipeBuilder = builder {
            this.ingredient = ingredient
            this.result = result
            this.exp = exp
            this.count = count
        }
    }

    @get:JvmName("getResultLike")
    lateinit var result: ItemLike
    lateinit var ingredient: Ingredient
    var exp: Float = 0f
    var count: Int = 1

    override fun unlockedBy(
        criterionName: String,
        criterionTrigger: CriterionTriggerInstance,
    ) = this

    override fun group(groupName: String?) = this

    override fun getResult(): Item = result.asItem()

    override fun save(
        writer: Consumer<FinishedRecipe?>,
        recipeId: ResourceLocation,
    ) {
        writer.accept(Result(recipeId))
    }

    inner class Result(id: ResourceLocation) : SerializableFinishedRecipe<SugarFurnaceRecipe>(
        id, SugarFurnaceRecipe(id, ingredient, result.asItem(), count, exp)
    ) {
        override fun getType() = CRecipeTypes.sugar_furnace_type.get()
    }
}