package cn.jawbreakers.candycraftce.recipe.recipes

import cn.breadnicecat.candycraftce.utils.CodecUtils.getIdAsItem
import cn.breadnicecat.candycraftce.utils.CodecUtils.readItemId
import cn.breadnicecat.candycraftce.utils.CodecUtils.writeItemId
import cn.jawbreakers.candycraftce.blockentity.AdvancedFactoryBE
import cn.jawbreakers.candycraftce.blockentity.SugarFactoryBE
import cn.jawbreakers.candycraftce.recipe.RecipeSerializable
import cn.jawbreakers.candycraftce.registry.CRecipeTypes
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

/**
 * Created in 2024/2/4
 * Project: candycraftce
 * 
 * Author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * 
 * Kotlinize in 2026/10/23
 */
class SugarFactoryRecipe(
    @JvmField val id: ResourceLocation,
    val ingredient: Ingredient,
    val result: Item,
    val count: Int,
    val advanced: Boolean,
) : Recipe<SugarFactoryBE> {
    companion object {
        fun isAdvanced(container: SugarFactoryBE): Boolean {
            return container is AdvancedFactoryBE
        }
    }

    init {
        require(count > 0) { "count <= 0 in recipe $id" }
    }

    override fun matches(container: SugarFactoryBE, level: Level): Boolean {
        return (!advanced || isAdvanced(container)) && ingredient.test(container.getItem(SugarFactoryBE.INPUT_SLOT))
    }

    override fun assemble(container: SugarFactoryBE, registryAccess: RegistryAccess): ItemStack {
        val stack = result.defaultInstance
        stack.count = count
        return stack
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean = true

    override fun getIngredients(): NonNullList<Ingredient> {
        return NonNullList.create<Ingredient>().apply { add(ingredient) }
    }

    override fun getResultItem(registryAccess: RegistryAccess?): ItemStack {
        val stack = result.defaultInstance
        stack.count = count
        return stack
    }

    override fun getId() = id

    override fun getSerializer() = CRecipeTypes.sugar_factory_type.get()

    override fun getType() = CRecipeTypes.sugar_factory_type.type

    class Serializer : RecipeSerializable<SugarFactoryRecipe> {
        override fun fromJson(recipeId: ResourceLocation, json: JsonObject): SugarFactoryRecipe {
            val result = json.get("result").getAsJsonObject()
            return SugarFactoryRecipe(
                recipeId,
                Ingredient.fromJson(json.get("ingredient")),
                result.get("item").getIdAsItem(),
                if (result.has("count")) result.get("count").asInt else 1,
                json.has("advanced") && json.get("advanced").asBoolean
            )
        }

        override fun toJson(json: JsonObject, recipe: SugarFactoryRecipe) {
            json.add("result", JsonObject().apply {
                addProperty("item", recipe.result.key.toString())
                if (recipe.count != 1) addProperty("count", recipe.count)
            })
            json.add("ingredient", recipe.ingredient.toJson())
            if (recipe.advanced) json.addProperty("advanced", true)
        }

        override fun fromNetwork(recipeId: ResourceLocation, buffer: FriendlyByteBuf): SugarFactoryRecipe {
            return SugarFactoryRecipe(
                recipeId,
                Ingredient.fromNetwork(buffer),
                buffer.readItemId(),
                buffer.readVarInt(),
                buffer.readBoolean()
            )
        }

        override fun toNetwork(buffer: FriendlyByteBuf, recipe: SugarFactoryRecipe) {
            recipe.ingredient.toNetwork(buffer)
            buffer.writeItemId(recipe.result)
            buffer.writeVarInt(recipe.count)
            buffer.writeBoolean(recipe.advanced)
        }
    }

}
