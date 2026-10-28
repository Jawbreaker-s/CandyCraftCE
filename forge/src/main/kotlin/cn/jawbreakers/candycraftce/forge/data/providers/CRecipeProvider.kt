package cn.jawbreakers.candycraftce.forge.data.providers

import cn.breadnicecat.candycraftce.data.recipes.SugarFurnaceRecipeBuilder
import cn.jawbreakers.candycraftce.forge.data.providers.recipes.SugarFactoryRecipeBuilder
import cn.jawbreakers.candycraftce.registry.*
import cn.jawbreakers.candycraftce.registry.CBlocks.BlockFamily
import cn.jawbreakers.candycraftce.registry.CItems.asItem
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CUtils.key
import com.mojang.datafixers.util.Either
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.*
import net.minecraft.data.recipes.RecipeCategory.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Created in 2026/10/10 21:19 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
typealias RawIngredient = Either<ItemLike, TagKey<Item>>

class CRecipeProvider(output: PackOutput) : RecipeProvider(output) {
    override fun buildRecipes(exporter: Consumer<FinishedRecipe>) {
        val helper = Helper(exporter)
        helper.apply {
            CItems.apply {
                CBlocks.apply {
                    sugar_sand.madeWith {
                        cube(BUILDING_BLOCKS, 1, 2, Items.SUGAR.setUnlockBy())
                            .save()
                    }
                    listOf(
                        marshmallow_planks to listOf(marshmallow_log, stripped_marshmallow_log),
                        light_marshmallow_planks to listOf(light_marshmallow_log, stripped_light_marshmallow_log),
                        dark_marshmallow_planks to listOf(dark_marshmallow_log, stripped_dark_marshmallow_log),
                    ).forEach { (planks, logList) ->
                        planks.madeWith {
                            logList.forEach { log ->
                                cube(BUILDING_BLOCKS, 4, 1, log.setUnlockBy())
                                    .save()
                            }
                        }
                    }
                    families.forEach {
                        if (it === candy_cane_family) {
                            family(it, candy_cane.get())
                        } else {
                            family(it)
                        }
                    }
                    marshmallow_ladder.madeWith {
                        shaped(DECORATIONS, 3)
                            .pattern("# #")
                            .pattern("###")
                            .pattern("# #")
                            .define('#', marshmallow_stick.setUnlockBy())
                            .save()
                    }
                    candy_cane_block.madeWith {
                        cube(BUILDING_BLOCKS, 2, 2, candy_cane.setUnlockBy())
                            .save()
                    }
                    cotton_candy_block.madeWith {
                        cube(BUILDING_BLOCKS, 1, 3, cotton_candy.setUnlockBy())
                            .save()
                    }
                    caramel_bricks.madeWith {
                        cube(BUILDING_BLOCKS, 4, 2, caramel_block.setUnlockBy())
                            .save()
                    }
                    licorice_block.madeWith {
                        cube(BUILDING_BLOCKS, 1, 3, licorice.setUnlockBy())
                            .save()
                    }
                    licorice_bricks.madeWith {
                        cube(BUILDING_BLOCKS, 4, 2, licorice_block.setUnlockBy())
                            .save()
                    }
                    nougat_block.madeWith {
                        cube(BUILDING_BLOCKS, 1, 3, nougat_powder.setUnlockBy())
                            .save()
                    }
                    sugar_spikes.madeWith {
                        shaped(COMBAT, 8)
                            .pattern(" # ")
                            .pattern("# #")
                            .define('#', CItems.sugar_crystal.setUnlockBy())
                            .save()
                    }
                    cranberry_spikes.madeWith {
                        shaped(COMBAT, 8)
                            .pattern(" # ")
                            .pattern("#*#")
                            .define('#', CItems.cranberry_scale.get())
                            .define('*', CItems.sugar_crystal.setUnlockBy())
                            .save()
                    }
                    marshmallow_rope.madeWith {
                        shaped(DECORATIONS, 2)
                            .pattern(" #")
                            .pattern(" #")
                            .pattern(" #")
                            .define('#', CBlockTags.marshmallow_planks.item.setUnlockBy())
                            .save()
                    }
                    sugar_factory.madeWith {
                        shaped(DECORATIONS)
                            .pattern("*/*")
                            .pattern("/o/")
                            .pattern("*/*")
                            .define('*', licorice.setUnlockBy())
                            .define('/', marshmallow_stick.get())
                            .define('o', candy_cane_family.wall!!.get())
                            .save()
                    }
                    advanced_sugar_factory.madeWith {
                        shaped(DECORATIONS)
                            .pattern("*/*")
                            .pattern("/o/")
                            .pattern("*/*")
                            .define('*', red_trampojelly.get())
                            .define('/', candied_cherry.get())
                            .define('o', sugar_factory.setUnlockBy())
                            .save()
                    }

                    banana_block.madeWith {
                        cube(BUILDING_BLOCKS, 1, 2, banana_seaweed.setUnlockBy())
                            .save()
                    }
                    chewing_gum_block.madeWith {
                        cube(BUILDING_BLOCKS, 1, 2, CItems.chewing_gum.setUnlockBy())
                            .save()
                    }
                    mint_block.madeWith {
                        cube(BUILDING_BLOCKS, 1, 2, mint.setUnlockBy())
                            .save()
                    }
                    raspberry_block.madeWith {
                        cube(BUILDING_BLOCKS, 1, 2, rope_raspberry.setUnlockBy())
                            .save()
                    }
                    honeycomb_block.madeWith {
                        cube(BUILDING_BLOCKS, 1, 3, CItems.honeycomb.setUnlockBy())
                            .save()
                    }
                    pez_block.madeWith {
                        cube(BUILDING_BLOCKS, 1, 3, CItems.pez.setUnlockBy())
                            .save()
                    }
                    //Items
                    dragibus_stick.madeWith {
                        shaped(TOOLS, 1)
                            .pattern("/ ")
                            .pattern(" D")
                            .define('D', dragibus.setUnlockBy())
                            .define('/', Items.FISHING_ROD)
                            .save()
                    }
                    honey_arrow.madeWith {
                        shaped(COMBAT, 1)
                            .pattern("H")
                            .pattern("/")
                            .pattern("O")
                            .define('H', honeycomb.setUnlockBy())
                            .define('/', marshmallow_stick.get())
                            .define('O', CItemTags.candy_leaf)
                            .save()
                    }
                    caramel_crossbow.madeWith {
                        shaped(COMBAT, 1)
                            .pattern("B  ")
                            .pattern(" C ")
                            .pattern("  C")
                            .define('B', caramel_bow.setUnlockBy())
                            .define('C', caramel_block.get())
                            .save()
                    }
                    toolSet(marshmallow_tools, CBlockTags.marshmallow_planks.item.raw())
                    toolSet(pez_tools, pez.raw())
                    toolSet(honey_tools, honeycomb.raw())
                    toolSet(licorice_tools, licorice.raw())
                    armorSet(licorice_armors, licorice.raw())
                    armorSet(honey_armors, honeycomb_block.raw())
                    armorSet(pez_armors, pez.raw())
                    jelly_boots.madeWith {
                        shaped(COMBAT)
                            .pattern("# #")
                            .pattern("#*#")
                            .define('#', jelly_shock_absorber.setUnlockBy())
                            .define('*', sugar_crystal.get())
                            .save()
                    }
                    water_mask.madeWith {
                        shaped(COMBAT)
                            .pattern("###")
                            .pattern("#*#")
                            .define('*', cranberry_scale.get())
                            .define('#', sugar_crystal.setUnlockBy())
                            .save()
                    }
                    dynamite.madeWith {
                        shaped(COMBAT)
                            .pattern("#*#")
                            .pattern("#*#")
                            .pattern("#*#")
                            .define('*', nougat_powder.setUnlockBy())
                            .define('#', waffle_nugget.get())
                            .save()
                    }
                    glue_dynamite.madeWith {
                        shapeless(COMBAT, 1)
                            .requires(chewing_gum.get())
                            .requires(glue_dynamite.setUnlockBy())
                            .save()
                    }
                    waffle.madeWith {
                        cube(FOOD, 1, 3, waffle_nugget.setUnlockBy())
                            .save()
                    }
                    pez.madeWith {

                    }
                    listOf(
                        royal_rations_block to royal_rations,
                        caramel_jelly_block to caramel_jelly_block,
                        strawberry_jelly_block to strawberry_queen_jelly_ball,
                        lemon_jelly_block to lemon_jelly_ball,
                        raspberry_jelly_block to raspberry_jelly_ball,
                        mint_jelly_block to mint_jelly_ball,
                    ).forEach { (placed, ball) ->
                        placed.madeWith {
                            shaped(FOOD)
                                .pattern("###")
                                .pattern("###")
                                .define('#', ball.setUnlockBy())
                                .save()
                        }
                    }
                    custard_pudding_block.madeWith {
                        sugarSmelting(
                            pudding_block.ingredient(),
                            0.7f
                        ).save()
                    }
                    licorice.madeWith {
                        sugarSmelting(CBlockTags.licorice_ores.ingredient(), 1f).save()
                    }
                    pez.madeWith {
                        sugarSmelting(CBlockTags.pez_ores.ingredient(), 1f).save()
                    }
                    trampojelly.madeWith {
                        sugarSmelting(CBlockTags.jelly_ores.ingredient(), 1f).save()
                    }
                    red_trampojelly.madeWith {
                        sugarSmelting(trampojelly.ingredient(), 0.7f).save()
                    }
                    jelly_shock_absorber.madeWith {
                        sugarSmelting(red_trampojelly.ingredient(), 0.7f).save()
                    }
                    purple_trampojelly.madeWith {
                        sugarFactory(red_trampojelly.ingredient()).save()
                    }
                    yellow_trampojelly.madeWith {
                        sugarFactory(red_trampojelly.ingredient()).save()
                    }
                    caramel_block.madeWith {
                        sugarSmelting(sugar_sand.ingredient(), 0.5f).save()
                        sugarSmelting(sugar_block.ingredient(), 0.5f).save()
                    }
                    chocolate_stone.madeWith {
                        sugarSmelting(chocolate_cobblestone.ingredient(), 0.5f).save()
                    }
                    white_chocolate_stone.madeWith {
                        sugarSmelting(white_chocolate_cobblestone.ingredient(), 0.5f).save()
                    }
                    honey_lamp.madeWith {
                        sugarSmelting(honeycomb_block.ingredient(), 0.7f).save()
                    }
                    hot_gummy.madeWith {
                        sugarSmelting(gummy.ingredient(), 0.7f).save()
                    }
                    cranberry_fish_cooked.madeWith {
                        sugarSmelting(cranberry_fish.ingredient(), 1f).save()
                    }
                    glassTypes.flatten().forEach { (glass, pane) ->
                        pane.madeWith {
                            shaped(DECORATIONS, 16)
                                .pattern("###")
                                .pattern("###")
                                .define('#', glass.setUnlockBy())
                                .save()
                        }
                    }
                    glassTypes.forEach { type ->
                        val glasses = type.map { it.first }
                        for (i in glasses.indices) {
                            val g1 = glasses[i]
                            val g2 = glasses[(i + 1) % glasses.size]
                            g2.madeWith {
                                sugarFactory(g1.ingredient()).save()
                            }
                        }
                    }
                    caramel_glass.madeWith {
                        sugarSmelting(caramel_block.ingredient(), 0.7f).save()
                    }
                    dark_caramel_glass.madeWith {
                        shaped(DECORATIONS, 8)
                            .pattern("###")
                            .pattern("#*#")
                            .pattern("###")
                            .define('#', caramel_glass.setUnlockBy())
                            .define('*', licorice.get())
                            .save()
                    }
                    honey_glass.madeWith {
                        shaped(DECORATIONS, 8)
                            .pattern("###")
                            .pattern("#*#")
                            .pattern("###")
                            .define('#', caramel_glass.setUnlockBy())
                            .define('*', honeycomb.get())
                            .save()
                    }
                    marshmallow_stick.madeWith {
                        shaped(MISC, 4)
                            .pattern("#")
                            .pattern("#")
                            .define('#', CBlockTags.marshmallow_planks.setUnlockBy())
                            .save()
                        sugarFactory(Items.STICK.ingredient())
                            .save()
                    }
                    honey_shard.madeWith {
                        sugarFactory(fraise_tagada_flower.ingredient())
                            .save()
                    }
                    nougat_head.madeWith {
                        sugarFactory(nougat_block.ingredient(), true)
                            .save()
                    }
                    honey_torch.madeWith {
                        shaped(DECORATIONS, 4)
                            .pattern("*")
                            .pattern("/")
                            .define('*', honey_shard.setUnlockBy())
                            .define('/', marshmallow_stick.get())
                            .save()
                    }
                    resultOf({ Items.GOLD_NUGGET }) {
                        sugarFactory(sugar_essence_flower.ingredient(), true, 2)
                            .save()
                    }

                }
            }

        }
    }

    private fun Helper.family(
        family: BlockFamily,
        material: Item = family.original.asItem(),
        stick: Item = CItems.marshmallow_stick.asItem(),
    ) {
        family.stairs?.madeWith {
            stairBuilder(result, material.setUnlockBy())
                .save()
        }
        family.slab?.madeWith {
            slabBuilder(BUILDING_BLOCKS, result, material.setUnlockBy())
                .save()
        }
        family.wall?.madeWith {
            wallBuilder(BUILDING_BLOCKS, result, material.setUnlockBy())
                .save()
        }
        family.fence?.madeWith {
            shaped(DECORATIONS, 3)
                .pattern("W#W")
                .pattern("W#W")
                .define('W', material.setUnlockBy())
                .define('#', stick)
                .save()
        }
        family.fenceGate?.madeWith {
            shaped(REDSTONE)
                .pattern("#W#")
                .pattern("#W#")
                .define('#', stick)
                .define('W', material.setUnlockBy())
                .save()
        }
        family.door?.madeWith {
            doorBuilder(result, material.setUnlockBy())
                .save()
        }
        family.trapdoor?.madeWith {
            trapdoorBuilder(result, material.setUnlockBy())
                .save()
        }
        family.sign?.madeWith {
            shaped(DECORATIONS, 3)
                .pattern("###").pattern("###").pattern(" X ")
                .define('#', material.setUnlockBy())
                .define('X', stick)
                .save()
        }
    }

    private fun Helper.armorSet(set: CItems.ArmorSet, material: RawIngredient) {
        resultOf(set.helmet) {
            shaped(COMBAT)
                .pattern("MMM")
                .pattern("M M")
                .define('M', material.setUnlockBy())
                .save()
        }
        resultOf(set.plate) {
            shaped(COMBAT)
                .pattern("M M")
                .pattern("MMM")
                .pattern("MMM")
                .define('M', material.setUnlockBy())
                .save()
        }
        resultOf(set.leggings) {
            shaped(COMBAT)
                .pattern("MMM")
                .pattern("M M")
                .pattern("M M")
                .define('M', material.setUnlockBy())
                .save()
        }
        resultOf(set.boots) {
            shaped(COMBAT)
                .pattern("M M")
                .pattern("M M")
                .define('M', material.setUnlockBy())
                .save()
        }
    }

    private fun Helper.toolSet(
        set: CItems.ToolSet,
        material: RawIngredient,
        stick: ItemLike = CItems.marshmallow_stick.get(),
    ) {

        resultOf(set.sword) {
            shaped(COMBAT)
                .pattern("M")
                .pattern("M")
                .pattern("S")
                .define('M', material.setUnlockBy())
                .define('S', stick)
                .save()
        }
        resultOf(set.shovel) {
            shaped(COMBAT)
                .pattern("M")
                .pattern("S")
                .pattern("S")
                .define('M', material.setUnlockBy())
                .define('S', stick)
                .save()
        }
        resultOf(set.pickaxe) {
            shaped(COMBAT)
                .pattern("MMM")
                .pattern(" S ")
                .pattern(" S ")
                .define('M', material.setUnlockBy())
                .define('S', stick)
                .save()
        }
        resultOf(set.axe) {
            shaped(COMBAT)
                .pattern("MM")
                .pattern("MS")
                .pattern(" S")
                .define('M', material.setUnlockBy())
                .define('S', stick)
                .save()
        }
        resultOf(set.hoe) {
            shaped(COMBAT)
                .pattern("MM")
                .pattern(" S")
                .pattern(" S")
                .define('M', material.setUnlockBy())
                .define('S', stick)
                .save()
        }
    }


    //用来自动生成recipe id等
    class Helper(val exporter: Consumer<FinishedRecipe>) {
        //记录所有生成过的路径
        private val paths = mutableSetOf<ResourceLocation>()

        //用来记忆上次该路径的postfix
        private val memory: HashMap<ResourceLocation, Int> = HashMap()

        private val results = mutableSetOf<Item>()
        private var lastScope: HelperScope? = null
        fun Supplier<out ItemLike>.madeWith(scope: HelperScope.(ItemLike) -> Unit) = resultOf(this, scope)
        fun <I : ItemLike> resultOf(result: Supplier<I>, scope: HelperScope.(I) -> Unit) {
            val get = result.get()
            val result = get.asItem()
            require((lastScope?.context === null)) { "Last Builder may not be saved yet" }
            if (!results.add(result)) {
                clog.warn("Redundant result scope is being created for key: ${result.key}")
            }
            scope(HelperScope(result.key, result).also { lastScope = it }, get)
        }

        inner class HelperScope internal constructor(
            val key: ResourceLocation,
            val result: Item,
        ) {
            private var postfix: Int = memory.getOrDefault(key, 0)

            private var unlockBy: RawIngredient? = null
            internal var context: RecipeBuilder? = null
            fun id(prefix: String = ""): ResourceLocation {
                val current = key.withPrefix(prefix)
                var cur: ResourceLocation
                var flag = 0
                do {
                    flag++
                    cur = current.withSuffix("_${++postfix}")
                } while (cur in paths)
                if (flag > 1) memory[key] = postfix

                paths.add(cur)
                return cur
            }

            private fun <R : RecipeBuilder> R.setContext() = apply {
                if (context != null && context !== this) {
                    throw IllegalStateException("Last Builder may not be saved yet")
                } else {
                    context = this
                }
            }

            fun <I : ItemLike> Supplier<I>.setUnlockBy() = raw().setUnlockBy()

            fun <I : ItemLike> I.setUnlockBy() = raw().setUnlockBy()

            fun TagKey<Item>.setUnlockBy() = raw().setUnlockBy()
            fun ItemBlockTagkey.setUnlockBy() = item.setUnlockBy()


            fun RawIngredient.setUnlockBy(): Ingredient = apply {
                require(unlockBy === null) { "Multi unlockBy not supported" }
                unlockBy = this
            }.build()

            fun RawIngredient.build(): Ingredient = map(Ingredient::of, Ingredient::of)


            fun RecipeBuilder.save() {
                unlockBy?.ifLeft {
                    unlockedBy("has_${it.asItem().key.path}", has(it))
                }?.ifRight {
                    unlockedBy("has_tag_${it.location.path}", has(it))
                }
                unlockBy = null
                context = null
                save(exporter, id())
            }


            fun shaped(category: RecipeCategory, count: Int = 1): ShapedRecipeBuilder =
                ShapedRecipeBuilder.shaped(category, result, count).setContext()

            fun cube(category: RecipeCategory, count: Int, dim: Int, ingredient: Ingredient): RecipeBuilder {
                require(dim in 1..3) { "dim must be in 1..3" }
                if (dim == 1) {
                    return shapeless(category, count).requires(ingredient)
                } else {
                    val unit = "M".repeat(dim) + " ".repeat(3 - dim)
                    return shaped(category, count)
                        .apply {
                            repeat(dim) {
                                pattern(unit)
                            }
                        }
                        .define('M', ingredient)
                }
            }

            fun shapeless(category: RecipeCategory, count: Int = 1): ShapelessRecipeBuilder =
                ShapelessRecipeBuilder.shapeless(category, result, count).setContext()

            fun smelting(
                category: RecipeCategory,
                ingredient: ItemLike,
                xp: Float = 0f,
                time: Int = 200,
            ): SimpleCookingRecipeBuilder = SimpleCookingRecipeBuilder.smelting(
                Ingredient.of(ingredient), category, result, xp, time
            ).setContext()

            fun sugarFactory(
                ingredient: Ingredient,
                advanced: Boolean = false,
                count: Int = 1,
            ): SugarFactoryRecipeBuilder = SugarFactoryRecipeBuilder.direct(
                result, ingredient, count, advanced
            ).setContext()

            fun sugarSmelting(
                ingredient: Ingredient,
                exp: Float = 0f,
                count: Int = 1,
            ): SugarFurnaceRecipeBuilder = SugarFurnaceRecipeBuilder.direct(
                result, ingredient, exp, count
            ).setContext()
        }
    }
}

fun <I : ItemLike> Supplier<I>.raw() = get().raw()

fun <I : ItemLike> I.raw(): RawIngredient = Either.left(this)

fun TagKey<Item>.raw(): RawIngredient = Either.right(this)

fun <I : ItemLike> Supplier<I>.ingredient() = get().ingredient()

fun <I : ItemLike> I.ingredient(): Ingredient = Ingredient.of(this)

fun ItemBlockTagkey.ingredient() = item.ingredient()
fun TagKey<Item>.ingredient(): Ingredient = Ingredient.of(this)

