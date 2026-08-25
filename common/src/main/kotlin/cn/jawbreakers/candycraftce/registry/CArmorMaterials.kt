package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterial
import net.minecraft.world.item.crafting.Ingredient
import java.util.*

private fun durability(
    boots: Int,
    leggings: Int,
    chestplate: Int,
    helmet: Int,
): EnumMap<ArmorItem.Type, Int> {
    val values = EnumMap<ArmorItem.Type, Int>(ArmorItem.Type::class.java)
    values[ArmorItem.Type.BOOTS] = boots * 13
    values[ArmorItem.Type.LEGGINGS] = leggings * 13
    values[ArmorItem.Type.CHESTPLATE] = chestplate * 13
    values[ArmorItem.Type.HELMET] = helmet * 13
    return values
}

private fun defense(boots: Int, leggings: Int, chestplate: Int, helmet: Int): EnumMap<ArmorItem.Type, Int> {
    val values = EnumMap<ArmorItem.Type, Int>(ArmorItem.Type::class.java)
    values[ArmorItem.Type.BOOTS] = boots
    values[ArmorItem.Type.LEGGINGS] = leggings
    values[ArmorItem.Type.CHESTPLATE] = chestplate
    values[ArmorItem.Type.HELMET] = helmet
    return values
}

enum class CArmorMaterials(
    id: String,
    @JvmField val durability: EnumMap<ArmorItem.Type, Int>,
    @JvmField val defense: EnumMap<ArmorItem.Type, Int>,
    @JvmField val enchantmentValue: Int,
    repairIngredientFactory: () -> Ingredient,
) : ArmorMaterial {
    HONEY(
        "honey",
        durability(12, 16, 15, 11),
        defense(2, 5, 6, 2),
        18,
        { Ingredient.of(CItems.honey_shard.get()) }),
    LICORICE(
        "licorice",
        durability(15, 18, 16, 13),
        defense(2, 5, 6, 2),
        12,
        { Ingredient.of(CItems.licorice.get()) }),
    PEZ(
        "pez",
        durability(18, 22, 20, 16),
        defense(3, 6, 8, 3),
        14,
        { Ingredient.of(CItems.pez.get()) }),
    JELLY_BOOTS(
        "jelly_boots",
        durability(11, 0, 0, 0),
        defense(2, 0, 0, 0),
        15,
        {
            Ingredient.of()
            //todo
//             Ingredient.of(CItems.JELLY_ORE.get())
        }),
    JELLY_CROWN(
        "jelly_crown",
        durability(0, 0, 0, 11),
        defense(0, 0, 0, 2),
        25,
        { Ingredient.EMPTY }),
    WATER_MASK(
        "water_mask",
        durability(0, 0, 0, 13),
        defense(0, 0, 0, 1),
        15,
        { Ingredient.of(CItems.cranberry_scale.get()) });

    private val repairIngredientInternal by lazy(repairIngredientFactory)
    val id = id.modLoc()


    override fun getDurabilityForType(type: ArmorItem.Type): Int = durability[type]!!
    override fun getDefenseForType(type: ArmorItem.Type): Int = defense[type]!!
    override fun getEnchantmentValue(): Int = enchantmentValue
    override fun getEquipSound(): SoundEvent = SoundEvents.ARMOR_EQUIP_GENERIC
    override fun getRepairIngredient() = repairIngredientInternal
    override fun getName() = id.toString()
    override fun getToughness() = if (this == PEZ) 1.0f else 0.0f
    override fun getKnockbackResistance() = 0.0f
}
