package cn.jawbreakers.candycraftce.recipe

import com.google.gson.JsonObject
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer

interface RecipeSerializable<R : Recipe<*>> : RecipeSerializer<R> {
    fun toJson(json: JsonObject, recipe: R)
}