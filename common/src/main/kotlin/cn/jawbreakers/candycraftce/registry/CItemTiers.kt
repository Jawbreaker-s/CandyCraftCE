package cn.jawbreakers.candycraftce.registry

import net.minecraft.world.item.Tier
import net.minecraft.world.item.crafting.Ingredient

enum class CItemTiers(
    @JvmField val level: Int,
    @JvmField val uses: Int,
    @JvmField val speed: Float,
    @JvmField val attackDamage: Float,
    @JvmField val enchantmentValue: Int,
    repairIngredientFactory: () -> Ingredient,
) : Tier {
    MARSHMALLOW(1, 131, 4.0f, 1.0f, 8, {
        Ingredient.of()//() -> Ingredient.of(CCBlocks.MARSHMALLOW_PLANKS.get()));
    }),
    COTTON_CANDY(1, 5, 15.0f, 5.0f, 65, {
        Ingredient.of(CItems.raspberry_cotton_candy.value)
    }),
    CHOCOLATE(
        2, 750, 7.0f, 2.5f, 25, {
            Ingredient.of()
            //todo
//            Ingredient.of(
//                BuiltInRegistries.ITEM.get(ResourceLocation(CandyCraft.MODID, "milk_chocolate_bar")),
//                BuiltInRegistries.ITEM.get(ResourceLocation(CandyCraft.MODID, "white_chocolate_bar")),
//                BuiltInRegistries.ITEM.get(ResourceLocation(CandyCraft.MODID, "dark_chocolate_bar")),
//                BuiltInRegistries.ITEM.get(ResourceLocation(CandyCraft.MODID, "ruby_chocolate_bar"))
//            )
        }),
    LICORICE(2, 250, 6.0f, 2.0f, 12, {
        Ingredient.of(CItems.licorice.value)
    }),
    HONEY(2, 220, 6.0f, 2.0f, 18, {
        Ingredient.of(CItems.honey_shard.value)
    }),
    PEZ(3, 561, 8.0f, 3.0f, 14, {
        Ingredient.of(CItems.pez.value)
    }),
    ;

    override fun getUses() = uses

    override fun getSpeed() = speed

    override fun getAttackDamageBonus() = attackDamage

    override fun getLevel(): Int = level

    override fun getEnchantmentValue() = enchantmentValue

    val ingredient: Ingredient by lazy(repairIngredientFactory)
    override fun getRepairIngredient() = ingredient
}

