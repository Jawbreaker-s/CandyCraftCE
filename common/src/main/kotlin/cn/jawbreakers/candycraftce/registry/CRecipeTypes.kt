package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.recipe.recipes.SugarFactoryRecipe
import cn.jawbreakers.candycraftce.recipe.recipes.SugarFurnaceRecipe
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.registry.DelegateEntry
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import java.util.function.Supplier

object CRecipeTypes {
    init {
        CLogUtils.sign()
    }

    val sugar_factory_type = register("sugar_factory", SugarFactoryRecipe::Serializer)

    val sugar_furnace_type = register("sugar_furnace", SugarFurnaceRecipe::Serializer)

    fun <R : Recipe<*>, S : RecipeSerializer<R>> register(
        name: String,
        serializer: Supplier<S>,
    ): RecipeTypeEntry<R, S> {
        val type = object : RecipeType<R> {
            val id = name.modLoc().toString()
            override fun toString() = id
        }
        return RecipeTypeEntry(CPlatformUtils.registerRecipe(name, type, serializer))
    }

    class RecipeTypeEntry<R : Recipe<*>, S : RecipeSerializer<R>>(
        val type: RecipeType<R>,
        serializer: Entry<S>,
    ) : DelegateEntry<S>(serializer) {
        constructor(entryPair: Pair<RecipeType<R>, Entry<S>>) : this(entryPair.first, entryPair.second)
    }
}