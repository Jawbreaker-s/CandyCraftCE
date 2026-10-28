package cn.jawbreakers.candycraftce.recipe.recipes

import cn.breadnicecat.candycraftce.utils.CodecUtils.getIdAsItem
import cn.breadnicecat.candycraftce.utils.CodecUtils.readItemId
import cn.breadnicecat.candycraftce.utils.CodecUtils.writeItemId
import cn.jawbreakers.candycraftce.blockentity.LicoriceFurnaceBE
import cn.jawbreakers.candycraftce.recipe.RecipeSerializable
import cn.jawbreakers.candycraftce.registry.CRecipeTypes.sugar_furnace_type
import cn.jawbreakers.candycraftce.utils.CUtils.key
import com.google.gson.JsonObject
import net.minecraft.core.NonNullList
import net.minecraft.core.RegistryAccess
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.Level

class SugarFurnaceRecipe(
    @JvmField val id: ResourceLocation,
    val ingredient: Ingredient,
    val result: Item,
    val count: Int,
    val exp: Float,
) : Recipe<LicoriceFurnaceBE> {
    init {
        require(count > 0) { "count <= 0 in $id" }
    }

    override fun matches(container: LicoriceFurnaceBE, level: Level): Boolean {
        return ingredient.test(container.getItem(LicoriceFurnaceBE.INPUT_SLOT))
    }

    override fun assemble(container: LicoriceFurnaceBE, registryAccess: RegistryAccess): ItemStack {
        val stack = result.defaultInstance
        stack.count = count
        return stack
    }

    override fun getIngredients(): NonNullList<Ingredient> {
        return NonNullList.createWithCapacity<Ingredient>(1).apply { add(ingredient) }
    }

    override fun getResultItem(registryAccess: RegistryAccess): ItemStack {
        val stack = result.defaultInstance
        stack.count = count
        return stack
    }

    override fun getId() = id
    override fun getSerializer() = sugar_furnace_type.get()
    override fun getType() = sugar_furnace_type.type

    override fun canCraftInDimensions(width: Int, height: Int): Boolean = true

    class Serializer : RecipeSerializable<SugarFurnaceRecipe> {
        override fun toJson(json: JsonObject, recipe: SugarFurnaceRecipe) {
            json.add("result", JsonObject().apply {
                addProperty("item", recipe.result.key.toString())
                if (recipe.count != 1) addProperty("count", recipe.count)
            })
            if (recipe.exp > 0f) json.addProperty("exp", recipe.exp)
            json.add("ingredient", recipe.ingredient.toJson())
        }

        override fun fromJson(recipeId: ResourceLocation, json: JsonObject): SugarFurnaceRecipe {
            val result = json.get("result").getAsJsonObject()
            return SugarFurnaceRecipe(
                recipeId,
                Ingredient.fromJson(json.get("ingredient")),
                result.get("item").getIdAsItem(),
                if (result.has("count")) result.get("count").asInt else 1,
                if (json.has("exp")) json.get("exp").asFloat else 0f
            )
        }

        override fun fromNetwork(recipeId: ResourceLocation, buffer: FriendlyByteBuf): SugarFurnaceRecipe {
            return SugarFurnaceRecipe(
                recipeId,
                Ingredient.fromNetwork(buffer),
                buffer.readItemId(),
                buffer.readVarInt(),
                buffer.readFloat()
            )
        }

        override fun toNetwork(buffer: FriendlyByteBuf, recipe: SugarFurnaceRecipe) {
            recipe.ingredient.toNetwork(buffer)
            buffer.writeItemId(recipe.result)
            buffer.writeVarInt(recipe.count)
            buffer.writeFloat(recipe.exp)
        }
    }
}
