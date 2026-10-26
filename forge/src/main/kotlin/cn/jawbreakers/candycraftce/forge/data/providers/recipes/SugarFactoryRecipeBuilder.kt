package cn.jawbreakers.candycraftce.forge.data.providers.recipes

import cn.jawbreakers.candycraftce.recipe.recipes.SugarFactoryRecipe
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
 * Created in 2024/2/5
 * Project: candycraftce
 * 
 * @author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * 
 * 
 */
class SugarFactoryRecipeBuilder : RecipeBuilder {
    companion object {
        fun builder(action: SugarFactoryRecipeBuilder.() -> Unit): SugarFactoryRecipeBuilder {
            return SugarFactoryRecipeBuilder().apply(action)
        }

        fun direct(
            result: ItemLike,
            ingredient: Ingredient,
            count: Int = 1,
            advanced: Boolean = false,
        ) = builder {
            this.result = result
            this.ingredient = ingredient
            this.count = count
            this.advanced = advanced
        }
    }

    @get:JvmName("getResultLike")//解决冲突
    lateinit var result: ItemLike
    lateinit var ingredient: Ingredient
    var count = 1
    var advanced = false
    fun setAdvanced() {
        advanced = true
    }

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

    inner class Result(id: ResourceLocation) :
        SerializableFinishedRecipe<SugarFactoryRecipe>(
            id, SugarFactoryRecipe(
                id, ingredient, result.asItem(), count, advanced
            )
        ) {
        override fun getType() = CRecipeTypes.sugar_factory_type.get()
    }
}
