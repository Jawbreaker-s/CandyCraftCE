package cn.jawbreakers.candycraftce.forge.data.providers.recipes

import cn.jawbreakers.candycraftce.recipe.RecipeSerializable
import com.google.gson.JsonObject
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe

/**
 * Created by NiceCat on 2026/5/1.
 * Project: candycraftce
 * @author <a href="https://github.com/BreadNiceCat">Bread_NiceCat</a>
 *
 */
abstract class SerializableFinishedRecipe<R : Recipe<*>>(
    @JvmField val id: ResourceLocation,
    val recipe: R,
) : FinishedRecipe {
    override fun serializeRecipeData(json: JsonObject) {
        type.toJson(json, recipe)
    }

    abstract override fun getType(): RecipeSerializable<R>
    final override fun getId(): ResourceLocation = id
    override fun getAdvancementId(): ResourceLocation? = null
    override fun serializeAdvancement(): JsonObject? = null

}