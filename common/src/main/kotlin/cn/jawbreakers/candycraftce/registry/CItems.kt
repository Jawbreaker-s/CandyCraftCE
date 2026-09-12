package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.fluid.CFluidReferences
import cn.jawbreakers.candycraftce.item.*
import cn.jawbreakers.candycraftce.registry.CTabs.addItem
import cn.jawbreakers.candycraftce.registry.CTabs.blocks
import cn.jawbreakers.candycraftce.registry.CTabs.misc
import cn.jawbreakers.candycraftce.registry.CTabs.toolsArmors
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CUtils.instance
import cn.jawbreakers.candycraftce.utils.IEntrySet
import cn.jawbreakers.candycraftce.utils.MCTimeUnit.Companion.tick
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.*
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import java.util.function.Supplier

object CItems {
    init {
        CLogUtils.sign()
    }

    private val items: MutableMap<String, Entry<out Item>> = mutableMapOf()

    private var contextTab: Entry<CreativeModeTab>? = misc

    //TODO 模型override，详见贴图
    val dynamite = register("dynamite") { DynamiteItem(Properties(), false) }
    val glue_dynamite = register("glue_dynamite") { DynamiteItem(Properties(), true) }

    val cranberry_emblem = registerEmblem("cranberry_emblem")
    val gingerbread_emblem = registerEmblem("gingerbread_emblem")
    val honey_emblem = registerEmblem("honey_emblem")
    val sky_emblem = registerEmblem("sky_emblem")
    val nessie_emblem = registerEmblem("nessie_emblem")
    val chewing_gum_emblem = registerEmblem("chewing_gum_emblem")
    val jelly_emblem = registerEmblem("jelly_emblem")
    val suguard_emblem = registerEmblem("suguard_emblem")

    //todo SoundEvent需要测试
    val record_1 = register("record_1") //,CCSoundEvents.RECORD_CD_1, 6330);
    val record_2 = register("record_2") //,CCSoundEvents.RECORD_CD_2, 1961);
    val record_3 = register("record_3") //,CCSoundEvents.RECORD_CD_3, 2259);
    val record_4 = register("record_4") //,CCSoundEvents.RECORD_CD_4, 5911);

    val jelly_dungeon_key = register("jelly_dungeon_key", Properties().stacksTo(1).rarity(Rarity.EPIC))
    val beetle_dungeon_key = register("beetle_dungeon_key", Properties().stacksTo(1).rarity(Rarity.EPIC))
    val sky_dungeon_key = register("sky_dungeon_key", Properties().stacksTo(1).rarity(Rarity.EPIC))
    val suguard_dungeon_key = register("suguard_dungeon_key", Properties().stacksTo(1).rarity(Rarity.EPIC))

    val jelly_sentry_key = register("jelly_sentry_key", Properties().rarity(Rarity.RARE))
    val jelly_boss_key = register("jelly_boss_key", Properties().rarity(Rarity.RARE))
    val suguard_sentry_key = register("suguard_sentry_key", Properties().rarity(Rarity.RARE))
    val suguard_boss_key = register("suguard_boss_key", Properties().rarity(Rarity.RARE))

    val candy_cane = registerFood("candy_cane", 4, 0.6F)
    val white_green_candy_cane = registerFood("white_green_candy_cane", 4, 0.6F)
    val red_green_candy_cane = registerFood("red_green_candy_cane", 4, 0.6F)

    val caramel_bucket = registerBucketItem("caramel_bucket", CFluids.caramel)
    val grenadine_bucket = registerBucketItem("grenadine_bucket", CFluids.grenadine)
    val liquid_chocolate_bucket = registerBucketItem("liquid_chocolate_bucket", CFluids.liquid_chocolate)
    val liquid_candy_bucket = registerBucketItem("liquid_candy_bucket", CFluids.liquid_candy)

    val white_hard_candy = registerFood("white_hard_candy", 2, 0.6F)
    val red_hard_candy = registerFood("red_hard_candy", 2, 0.6F)
    val green_hard_candy = registerFood("green_hard_candy", 2, 0.6F)
    val yellow_hard_candy = registerFood("yellow_hard_candy", 2, 0.6F)
    val orange_hard_candy = registerFood("orange_hard_candy", 2, 0.6F)
    val light_blue_hard_candy = registerFood("light_blue_hard_candy", 2, 0.6F)
    val pink_hard_candy = registerFood("pink_hard_candy", 2, 0.6F)
    val purple_hard_candy = registerFood("purple_hard_candy", 2, 0.6F)
    val white_red_hard_candy = registerFood("white_red_hard_candy", 2, 0.6F)
    val white_green_hard_candy = registerFood("white_green_hard_candy", 2, 0.6F)
    val white_yellow_hard_candy = registerFood("white_yellow_hard_candy", 2, 0.6F)
    val white_orange_hard_candy = registerFood("white_orange_hard_candy", 2, 0.6F)
    val white_light_blue_hard_candy = registerFood("white_light_blue_hard_candy", 2, 0.6F)
    val white_pink_hard_candy = registerFood("white_pink_hard_candy", 2, 0.6F)
    val white_purple_hard_candy = registerFood("white_purple_hard_candy", 2, 0.6F)
    val red_green_hard_candy = registerFood("red_green_hard_candy", 2, 0.6F)
    val waffle = registerFood("waffle", 10, 0.6F)
    val milk_brownie = registerFood("milk_brownie", 4, 0.5F)
    val white_brownie = registerFood("white_brownie", 4, 0.5F)
    val dark_brownie = registerFood("dark_brownie", 4, 0.5F)
    val milk_chocolate_bar = registerFood("milk_chocolate_bar", 5, 0.6F)
    val white_chocolate_bar = registerFood("white_chocolate_bar", 5, 0.6F)
    val dark_chocolate_bar = registerFood("dark_chocolate_bar", 5, 0.6F)
    val ruby_chocolate_bar = registerFood("ruby_chocolate_bar", 5, 0.6F)
    val milk_chocolate_egg = registerFood("milk_chocolate_egg", 7, 0.8F)
    val white_chocolate_egg = registerFood("white_chocolate_egg", 7, 0.8F)
    val dark_chocolate_egg = registerFood("dark_chocolate_egg", 7, 0.8F)
    val cotton_candy = registerFood("cotton_candy", 4, 0.5F)
    val raspberry_cotton_candy = register(
        "raspberry_cotton_candy",
        Properties().food(3, 0.6f, true, MobEffects.DIG_SPEED.instance(30.tick) to 0.9f)
    )
    val lollipop = register("lollipop") { LollipopItem(Properties().food(1, 0.6F)) }

    val cranberry_fish = register(
        "cranberry_fish",
        Properties().food(2, 0.6F, true, MobEffects.WATER_BREATHING.instance(30.tick) to 0.9F)
    )
    val cranberry_fish_cooked = register(
        "cranberry_fish_cooked",
        Properties().food(6, 0.6F, true, MobEffects.WATER_BREATHING.instance(60.tick) to 0.9F)
    )

    val gummy = register("gummy", Properties().food(4, 0.6F, true, MobEffects.CONFUSION.instance(30.tick) to 0.9f))

    val hot_gummy = register(
        "hot_gummy",
        Properties().food(7, 0.6f, true, MobEffects.JUMP.instance(60.tick, 1) to 0.9f)
    )
    val orange_gummy = registerFood("orange_gummy", 4, 0.6F)
    val yellow_gummy = registerFood("yellow_gummy", 4, 0.6F)
    val white_gummy = registerFood("white_gummy", 4, 0.6F)
    val green_gummy = registerFood("green_gummy", 4, 0.6F)
    val red_gummy_worm = registerFood("red_gummy_worm", 4, 0.6F)
    val orange_gummy_worm = registerFood("orange_gummy_worm", 6, 1.0F)
    val yellow_gummy_worm = registerFood("yellow_gummy_worm", 6, 1.0F)
    val white_gummy_worm = registerFood("white_gummy_worm", 6, 1.0F)
    val green_gummy_worm = registerFood("green_gummy_worm", 6, 1.0F)
    val hot_gummy_worm = registerFood("hot_gummy_worm", 6, 1.0F)
    val gummy_ball = register("gummy_ball") { GummyBallItem(Properties().stacksTo(16)) }
    val lemon_jelly_ball = register("lemon_jelly_ball")
    val raspberry_jelly_ball = register("raspberry_jelly_ball")
    val mint_jelly_ball = register("mint_jelly_ball")
    val pez_jelly_ball = register("pez_jelly_ball")
    val caramel_king_jelly_ball = register("caramel_king_jelly_ball")
    val strawberry_queen_jelly_ball = register("strawberry_queen_jelly_ball")

    //TODO BLOCK CAKE!!!
    val lemon_jelly =
        register("lemon_jelly")//, () ->new PlaceableJellyFoodItem(CCBlocks.LEMON_JELLY_FOOD.get(), foodProperties(7, 0.4F)));
    val raspberry_jelly =
        register("raspberry_jelly")//, () ->new PlaceableJellyFoodItem(CCBlocks.RASPBERRY_JELLY_FOOD.get(), foodProperties(7, 0.4F)));
    val mint_jelly =
        register("mint_jelly")//, () ->new PlaceableJellyFoodItem(CCBlocks.MINT_JELLY_FOOD.get(), foodProperties(7, 0.4F)));

    val caramel_jelly =
        register("caramel_jelly")//, () ->new StrawberryJellyItem(CCBlocks.CARAMEL_JELLY_BLOCK.get(), new CItemTags.Properties()));
    val strawberry_jelly =
        register("strawberry_jelly")//, () ->new StrawberryJellyItem(CCBlocks.STRAWBERRY_JELLY_BLOCK.get(), new CItemTags.Properties()));
    val royal_rations =
        register("royal_rations")//, () ->new StrawberryJellyItem(CCBlocks.ROYAL_RATIONS_BLOCK.get(), new CItemTags.Properties()));

    val caramel_jelly_slice = registerFood("caramel_jelly_slice", 12, 0.6F)
    val strawberry_jelly_slice = registerFood("strawberry_jelly_slice", 12, 0.6F)
    val royal_rations_slice = registerFood("royal_rations_slice", 20, 0.5F)

    val nougat_powder = register("nougat_powder") {
        NougatPowderItem(Properties().food(3, 0.6f, true))
    }
    val pez_dust = register("pez_dust")
    val chocolate_coin = registerFood("chocolate_coin", 2, 0.6F)
    val pez = register(
        "pez",
        Properties().food(10, 0.6f, true, MobEffects.DAMAGE_RESISTANCE.instance(60.tick) to 0.9f)
    )
    val magic_candy = register("magic_candy") { MagicCandyItem(Properties().food(1, 1.0F, true)) }
    val licorice = registerFood("licorice", 6, 0.6F)
    val honey_shard = register("honey_shard")
    val chewing_gum = registerFood("chewing_gum", 1, 0.1F)
    val sugar_pill = register("sugar_pill") { SugarPillItem(Properties().food(0, 0f, true)) }
    val marshmallow_stick = register("marshmallow_stick")
    val wafer_stick = registerFood("wafer_stick", 5, 0.6F)
    val waffle_nugget = registerFood("waffle_nugget", 1, 0.6F)
    val butter = registerFood("butter", 1, 1.0F)
    val honeycomb = register("honeycomb")
    val cranberry_scale = register("cranberry_scale")
    val candied_cherry = register("candied_cherry") { CandiedCherryItem(Properties().food(3, 0.6F)) }
    val rock_sugar = registerFood("rock_sugar", 4, 0.2F)
    val sugar_crystal = register("sugar_crystal", Properties().rarity(Rarity.RARE))

    val white_chocolate_leaf = register("white_chocolate_leaf")
    val magical_leaf = register("magical_leaf")
    val chocolate_leaf = register("chocolate_leaf")
    val caramel_leaf = register("caramel_leaf")
    val candied_cherry_leaf = register("candied_cherry_leaf")

    //todo BLOCK
    val lollipop_seeds = register("lollipop_seeds")//, () -> CCBlocks.LOLLIPOP_PLANT.get());
    val dragibus =
        register("dragibus")//, { DragibusSeedFoodItem(CCBlocks.DRAGIBUS_CROPS.get(), Properties().food(1,0.3f)) });

    //=====================
    //=====================
    //=====================
    init {
        contextTab = null
    }

    val marshmallow_flower = register("marshmallow_flower")

    init {
        contextTab = blocks
        val signs = CBlocks.families.stream()
            .filter { it.sign != null }
            .toList()
            .associate { it.sign!! to it.wallSign!! }

        val skips = buildSet {
            addAll(signs.values)
        }
        CBlocks.withItem().forEach {
            when (it) {
                in signs -> registerBlock(it) { _ -> SignItem(Properties(), it.get(), signs[it]!!.get()) }
                in skips -> {}
                else -> registerBlock(it)
            }
            if (it == CBlocks.marshmallow_slice_flower) {
                blocks.addItem { marshmallow_flower.defaultInstance }
            }
        }
    }


    init {
        contextTab = toolsArmors
    }

    val dragibus_stick = register("dragibus_stick") { DragibusStickItem(Properties().stacksTo(1).durability(25)) }
    val honey_arrow = register("honey_arrow") { HoneyArrowItem(Properties()) }
    val honey_bolt = register("honey_bolt") { HoneyBoltItem(Properties()) }
    val caramel_bow = register("caramel_bow") { CaramelBowItem(Properties().durability(384)) }
    val caramel_crossbow = register("caramel_crossbow") { CaramelCrossbowItem(Properties().durability(465)) }

    //todo wand entity
    val jelly_wand = register("jelly_wand")// { JellyWandItem(Properties().stacksTo(1)) }
    val jump_wand = register("jump_wand") { JumpWandItem(Properties().stacksTo(1)) }

    //TODO FUCK THE FORK!
    val fork = register("fork") {
        ForkItem(CItemTiers.MARSHMALLOW, 4.5f, -2.9f, Properties().durability(326))
    }
    val marshmallow_tools = registerToolSet("marshmallow", CItemTiers.MARSHMALLOW)
    val milk_chocolate_tools = registerToolSet("milk_chocolate", CItemTiers.CHOCOLATE)
    val white_chocolate_tools = registerToolSet("white_chocolate", CItemTiers.CHOCOLATE)
    val dark_chocolate_tools = registerToolSet("dark_chocolate", CItemTiers.CHOCOLATE)
    val cotton_candy_tools = registerToolSet("cotton_candy", CItemTiers.COTTON_CANDY)
    val licorice_short_sword = register("licorice_short_sword") {
        SwordItem(CItemTiers.LICORICE, 2, -2.2F, Properties())
    }
    val licorice_tools = registerToolSet("licorice", CItemTiers.LICORICE)
    val honey_tools = registerToolSet("honey", CItemTiers.HONEY)
    val pez_tools = registerToolSet("pez", CItemTiers.PEZ)

    val honey_armors = registerArmorSet("honey", CArmorMaterials.HONEY)
    val licorice_armors = registerArmorSet("licorice", CArmorMaterials.LICORICE)
    val pez_armors = registerArmorSet("pez", CArmorMaterials.PEZ)
    val jelly_boots = register("jelly_boots") {
        ArmorItem(CArmorMaterials.JELLY_BOOTS, ArmorItem.Type.BOOTS, Properties())
    }
    val jelly_crown = register("jelly_crown") {
        ArmorItem(CArmorMaterials.JELLY_CROWN, ArmorItem.Type.HELMET, Properties().rarity(Rarity.RARE))
    }
    val water_mask = register("water_mask") {
        ArmorItem(CArmorMaterials.WATER_MASK, ArmorItem.Type.HELMET, Properties())
    }

    init {
        contextTab = null
    }

    private fun register(name: String, properties: Properties = Properties()): Entry<Item> {
        return register(name) { Item(properties) }
    }

    private fun registerBlock(entry: Entry<out Block>, properties: Properties = Properties()): Entry<BlockItem> {
        return registerBlock(entry) { b -> BlockItem(b.get(), properties) }
    }

    private fun <B : Block, I : BlockItem> registerBlock(entry: Entry<B>, factory: (Entry<B>) -> I): Entry<I> {
        return register(entry.id.path) { factory(entry) }
    }

    private fun registerBucketItem(
        name: String,
        fluid: CFluidReferences,
        properties: Properties = Properties().craftRemainder(Items.BUCKET).stacksTo(1),
    ): Entry<BucketItem> = register(name) {
        CPlatformUtils.fluids.createBucketItem(fluid, properties)
    }

    private fun registerFood(name: String, nutrition: Int, saturation: Float) =
        register(name, Properties().food(nutrition, saturation))


    private fun <I : Item> register(name: String, factory: Supplier<I>): Entry<I> {
        val item = CPlatformUtils.registerItem(name, factory)
        contextTab?.addItem { item.defaultInstance }

        items[name] = item
        return item
    }

    private fun registerEmblem(name: String) = register(name) { EmblemItem(Properties().stacksTo(1)) }
//    private fun registerRecords(name: String, analog: Int, sound: SoundEvent, time: Int) =
//        register(name) { RecordItem(analog, sound, Properties().stacksTo(1).rarity(Rarity.RARE), time) }

    private fun Properties.food(
        nutrition: Int, saturation: Float, alwaysEat: Boolean = false,
        vararg effects: Pair<MobEffectInstance, Float>,
    ): Properties {
        val builder = FoodProperties.Builder()
            .nutrition(nutrition)
            .saturationMod(saturation)
            .apply {
                effects.forEach { (effect, chance) -> effect(effect, chance) }
            }
        if (alwaysEat) {
            builder.alwaysEat()
        }
        return this.food(builder.build())
    }

    val Entry<out Item>.defaultInstance: ItemStack get() = value.defaultInstance
    fun Entry<out ItemLike>.asItem(): Item = get().asItem()

    private fun registerToolSet(name: String, tier: Tier): ToolSet {
        return ToolSet(
            register(name + "_sword") { SwordItem(tier, 3, -2.4F, Properties()) },
            register(name + "_shovel") { ShovelItem(tier, 1.5F, -3.0F, Properties()) },
            register(name + "_pickaxe") { PickaxeItem(tier, 1, -2.8F, Properties()) },
            register(name + "_axe") { AxeItem(tier, 5.0F, -3.1F, Properties()) },
            register(name + "_hoe") { HoeItem(tier, -2, -1.0F, Properties()) },
        )
    }

    private fun registerArmorSet(name: String, material: ArmorMaterial): ArmorSet {
        return ArmorSet(
            register(name + "_helmet") { ArmorItem(material, ArmorItem.Type.HELMET, Properties()) },
            register(name + "_chestplate") { ArmorItem(material, ArmorItem.Type.CHESTPLATE, Properties()) },
            register(name + "_leggings") { ArmorItem(material, ArmorItem.Type.LEGGINGS, Properties()) },
            register(name + "_boots") { ArmorItem(material, ArmorItem.Type.BOOTS, Properties()) },
        )
    }

    data class ToolSet(
        val sword: Entry<out SwordItem>,
        val shovel: Entry<out ShovelItem>,
        val pickaxe: Entry<out PickaxeItem>,
        val axe: Entry<out AxeItem>,
        val hoe: Entry<out HoeItem>,
    ) : IEntrySet<Item> {
        override fun entries(): List<Entry<out Item>> = listOf(sword, shovel, pickaxe, axe, hoe)
    }

    data class ArmorSet(
        val helmet: Entry<out ArmorItem>,
        val plate: Entry<out ArmorItem>,
        val leggings: Entry<out ArmorItem>,
        val boots: Entry<out ArmorItem>,
    ) : IEntrySet<Item> {
        override fun entries(): List<Entry<out Item>> = listOf(helmet, plate, leggings, boots)
    }

}



