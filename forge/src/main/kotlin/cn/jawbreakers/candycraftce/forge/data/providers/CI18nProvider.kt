package cn.jawbreakers.candycraftce.forge.data.providers

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.item.EmblemItem
import cn.jawbreakers.candycraftce.item.JumpWandItem
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CTabs
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraftforge.common.data.LanguageProvider
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class CI18nProvider(output: PackOutput) : DataProvider {
    override fun getName(): String = "CandyCraftCE I18n"
    private val enUS: SubLanguageProvider = SubLanguageProvider(output, "en_us")
    private val zhCN: SubLanguageProvider = SubLanguageProvider(output, "zh_cn")
    private val subs = setOf(enUS, zhCN)


    private fun addTranslations() {
        add("mod.$MOD_ID", "CandyCraft Community Edition", "糖果世界社区版")
        CTabs.apply {
            addTab(blocks, "CandyCraftCE: Blocks", "糖果世界:方块")
            addTab(toolsArmors, "CandyCraftCE: Tools and Armors", "糖果世界:工具和盔甲")
            addTab(misc, "CandyCraftCE: Misc", "糖果世界:杂项")
        }
        CItems.apply {
            addItem(dynamite, "Nougat Dynamite", "牛轧糖炸药")
            addItem(glue_dynamite, "Chewing Gum Emblem", "口香糖炸药")
            addItem(chewing_gum_emblem, "Chewing Gum Emblem", "口香糖徽章")
            add(
                chewing_gum_emblem.get().tooltipKey,
                "Lets you walk normally on chewing gum.",
                "让你能在口香糖上正常行走。"
            )
            addItem(cranberry_emblem, "Cranberry Emblem", "蔓越莓徽章")
            add(
                cranberry_emblem.get().tooltipKey,
                "The dawn heals you.",
                "黎明会治愈你。"
            )
            addItem(gingerbread_emblem, "Gingerbread Emblem", "姜饼徽章")
            add(
                gingerbread_emblem.get().tooltipKey,
                "Gingerbread villagers and candy cane pigs are no longer afraid.",
                "姜饼村民和拐杖糖猪不再害怕你。"
            )
            addItem(honey_emblem, "Honey Emblem", "蜜蜡徽章")
            add(
                honey_emblem.get().tooltipKey,
                "Lets you hear voices from underground villages.",
                "让你能偶尔听见地下结构的声音。"
            )
            addItem(jelly_emblem, "Jelly Emblem", "果冻徽章")
            add(
                jelly_emblem.get().tooltipKey,
                "Reduces fall damage by 30%.",
                "减少 30% 摔落伤害。"
            )
            addItem(sky_emblem, "Sky Emblem", "天空徽章")
            add(
                sky_emblem.get().tooltipKey,
                "Protects you from falling out of sky dungeons.",
                "防止你在天空地牢掉出世界。"
            )
            addItem(suguard_emblem, "Suguard Emblem", "糖卫徽章")
            add(
                suguard_emblem.get().tooltipKey,
                "Reduces arrow damage by 20%.",
                "减少 20% 箭矢伤害。"
            )
            addItem(nessie_emblem, "Nessie Emblem", "尼斯徽章")
            add(
                nessie_emblem.get().tooltipKey,
                "Water slowly heals you.",
                "在水中时你会被缓慢的治疗。"
            )
            add(
                EmblemItem.emblemTooltip,
                "Collect different emblems to awaken CandyCraft's endgame.",
                "收集不同的徽章来唤醒糖果工艺的后期内容。"
            )
            //todo RecordItem
            addItem(record_1, "果冻女王的秘密唱片", "Jelly queen's secret record")
//            add(record_1.get().getDisplayName(), "CandyCraft - Jelly queen's secret record", "糖果世界- 果冻女王的秘密唱片")
            addItem(record_2, "糖卫图腾的秘密唱片", "Suguard's secret record")
//            add(record_2.get().getDisplayName(), "CandyCraft - Suguard's secret record", "糖果世界 - 糖卫图腾的秘密唱片")
            addItem(record_3, "彩虹唱片", "Rainbow record")
//            add(record_3.get().getDisplayName(), "CandyCraft - Rainbow record", "糖果世界 - 彩虹唱片")
            addItem(record_4, "甘草甲虫的秘密唱片", "Licorice beetle secret record")
//            add(record_4.get().getDisplayName(), "CandyCraft - Licorice beetle secret record", "糖果世界 - 甘草甲虫的秘密唱片")

            addItem(jelly_dungeon_key, "Jelly Dungeon Key", "果冻地牢钥匙")
            addItem(beetle_dungeon_key, "Beetle Dungeon Key", "甲虫地牢钥匙")
            addItem(sky_dungeon_key, "Sky Dungeon Key", "天空地牢钥匙")
            addItem(suguard_dungeon_key, "Suguard Dungeon Key", "糖卫地牢钥匙")

            addItem(jelly_sentry_key, "Jelly Sentry Key", "果冻哨兵钥匙")
            addItem(jelly_boss_key, "Jelly Boss Key", "果冻国王钥匙")
            addItem(suguard_sentry_key, "Suguard Sentry Key", "糖卫哨兵钥匙")
            addItem(suguard_boss_key, "Suguard Boss Key", "糖卫国王钥匙")

            addItem(caramel_bucket, "Caramel Bucket", "焦糖桶")
            addItem(grenadine_bucket, "Grenadine Bucket", "番石榴糖浆桶")
            addItem(liquid_chocolate_bucket, "Liquid Chocolate Bucket", "液体巧克力桶")
            addItem(liquid_candy_bucket, "Liquid Candy Bucket", "液体糖浆桶")

            addItem(white_hard_candy, "White Hard Candy", "白色硬糖")
            addItem(red_hard_candy, "Red Hard Candy", "红色硬糖")
            addItem(green_hard_candy, "Green Hard Candy", "绿色硬糖")
            addItem(yellow_hard_candy, "Yellow Hard Candy", "黄色硬糖")
            addItem(orange_hard_candy, "Orange Hard Candy", "橙色硬糖")
            addItem(light_blue_hard_candy, "Light Blue Hard Candy", "浅蓝色硬糖")
            addItem(pink_hard_candy, "Pink Hard Candy", "粉色硬糖")
            addItem(purple_hard_candy, "Purple Hard Candy", "紫色硬糖")
            addItem(white_red_hard_candy, "White-Red Hard Candy", "红白硬糖")
            addItem(white_green_hard_candy, "White-Green Hard Candy", "绿白硬糖")
            addItem(white_yellow_hard_candy, "White-Yellow Hard Candy", "黄白硬糖")
            addItem(white_orange_hard_candy, "White-Orange Hard Candy", "橙白硬糖")
            addItem(white_light_blue_hard_candy, "White-Light Blue Hard Candy", "浅蓝白硬糖")
            addItem(white_pink_hard_candy, "White-Pink Hard Candy", "粉白硬糖")
            addItem(white_purple_hard_candy, "White-Purple Hard Candy", "紫白硬糖")
            addItem(red_green_hard_candy, "Red-Green Hard Candy", "红绿硬糖")

            addItem(milk_brownie, "Milk Brownie", "牛奶布朗尼")
            addItem(white_brownie, "White Brownie", "白色布朗尼")
            addItem(dark_brownie, "Dark Brownie", "深色布朗尼")
            addItem(milk_chocolate_bar, "Milk Chocolate Bar", "牛奶巧克力条")
            addItem(white_chocolate_bar, "White Chocolate Bar", "白色巧克力条")
            addItem(dark_chocolate_bar, "Dark Chocolate Bar", "深色巧克力条")
            addItem(ruby_chocolate_bar, "Ruby Chocolate Bar", "红宝石巧克力条")
            addItem(milk_chocolate_egg, "Milk Chocolate Egg", "牛奶巧克力蛋")
            addItem(white_chocolate_egg, "White Chocolate Egg", "白色巧克力蛋")
            addItem(dark_chocolate_egg, "Dark Chocolate Egg", "深色巧克力蛋")
            addItem(magic_candy, "Magic Candy", "魔法糖果")
            addItem(licorice, "Licorice", "盐甘草糖")
            addItem(butter, "Butter", "黄油")
            addItem(wafer_stick, "Wafer Stick", "饼干棒")
            addItem(rock_sugar, "Rock Sugar", "冰糖")
            addItem(honey_shard, "Honey Shard", "蜜蜡碎片")
            addItem(honeycomb, "Honeycomb", "蜜蜡")
            addItem(chocolate_coin, "Chocolate Coin", "巧克力币")
            addItem(pez, "PEZ", "皮礼士糖")
            addItem(cranberry_scale, "Cranberry Scale", "蔓越莓鱼鳞")
            addItem(sugar_crystal, "Sugar Crystal", "超纯糖晶")
            addItem(waffle_nugget, "Waffle Nugget", "华夫饼碎屑")
            addItem(marshmallow_stick, "Marshmallow Stick", "棉花软糖棒")
            addItem(lollipop_seeds, "Lollipop Seeds", "棒棒糖种子")
            addItem(dragibus, "Dragibus", "Dragibus软糖")
            addItem(lollipop, "Lollipop", "棒棒糖")
            addItem(marshmallow_flower, "Marshmallow Flower", "棉花软糖花")
            addItem(candied_cherry, "Candied Cherry", " 蜜饯樱桃")
            addItem(candy_cane, "Candy Cane", "拐杖糖")
            addItem(white_green_candy_cane, "White-Green Candy Cane", "白绿拐杖糖")
            addItem(red_green_candy_cane, "Red-Green Candy Cane", "红绿拐杖糖")
            addItem(chewing_gum, "Chewing Gum", "口香糖")
            addItem(cotton_candy, "Cotton Candy", "棉花糖")
            addItem(raspberry_cotton_candy, "Raspberry Cotton Candy", "树莓棉花糖")
            addItem(cranberry_fish, "Cranberry Fish", "蔓越莓鱼")
            addItem(cranberry_fish_cooked, "Cooked Cranberry Fish", "熟蔓越莓鱼")
            addItem(dragibus_stick, "Dragibus Stick", "Dragibus钓竿")
            addItem(gummy, "Gummy", "软糖")
            addItem(hot_gummy, "Hot Gummy", "熟软糖")
            addItem(orange_gummy, "Orange Gummy", "橙色软糖")
            addItem(yellow_gummy, "Yellow Gummy", "黄色软糖")
            addItem(white_gummy, "White Gummy", "白色软糖")
            addItem(green_gummy, "Green Gummy", "绿色软糖")
            addItem(red_gummy_worm, "Red Gummy Worm", "红色软糖虫")
            addItem(orange_gummy_worm, "Orange Gummy Worm", "橙色软糖虫")
            addItem(yellow_gummy_worm, "Yellow Gummy Worm", "黄色软糖虫")
            addItem(white_gummy_worm, "White Gummy Worm", "白色软糖虫")
            addItem(green_gummy_worm, "Green Gummy Worm", "绿色软糖虫")
            addItem(hot_gummy_worm, "Hot Gummy Worm", "熟软糖虫")

            addItem(sugar_pill, "Sugar Pill", "糖丸")
            addItem(waffle, "Waffle", "华夫饼")
            addItem(jump_wand, "Jump Wand", "跳跃权杖")
            addItem(jelly_wand, "Jelly King's Wand", "果冻国王的权杖")
            addItem(honey_arrow, "Honey Arrow", "蜜蜡箭")
            addItem(honey_bolt, "Honey Bolt", "蜜蜡弩箭")
            addItem(caramel_bow, "Caramel Bow", "焦糖弓")
            addItem(caramel_crossbow, "Caramel Crossbow", "焦糖弩")
            addItem(fork, "Fork", "叉子")
            addItem(licorice_short_sword, "Licorice Short Sword", "盐甘草糖短剑")
            addItem(gummy_ball, "Gummy Ball", "软糖球")
            addItem(lemon_jelly_ball, "Lemon Jelly Ball", "柠檬果冻球")
            addItem(raspberry_jelly_ball, "Raspberry Jelly Ball", "树莓果冻球")
            addItem(mint_jelly_ball, "Mint Jelly Ball", "薄荷果冻球")
            addItem(pez_jelly_ball, "PEZ Jelly Ball", "皮礼士糖果冻球")
            addItem(caramel_king_jelly_ball, "Caramel King Jelly Ball", "焦糖国王果冻球")
            addItem(strawberry_queen_jelly_ball, "Strawberry Queen Jelly Ball", "草莓女王果冻球")
            addItem(lemon_jelly, "Lemon Jelly", "柠檬果冻")
            addItem(raspberry_jelly, "Raspberry Jelly", "树莓果冻")
            addItem(mint_jelly, "Mint Jelly", "薄荷果冻")
            addItem(caramel_jelly, "Caramel Jelly", "焦糖果冻")
            addItem(strawberry_jelly, "Strawberry Jelly", "草莓果冻")
            addItem(royal_rations, "Royal Rations", "皇家口粮")
            addItem(caramel_jelly_slice, "Caramel Jelly Slice", "焦糖果冻切片")
            addItem(strawberry_jelly_slice, "Strawberry Jelly Slice", "草莓果冻切片")
            addItem(royal_rations_slice, "Royal Rations Slice", "皇家口粮切片")
            addToolSet(marshmallow_tools, "Marshmallow", "棉花软糖")
            addToolSet(milk_chocolate_tools, "Milk Chocolate", "牛奶巧克力")
            addToolSet(white_chocolate_tools, "White Chocolate", "白色巧克力")
            addToolSet(dark_chocolate_tools, "Dark Chocolate", "深色巧克力")
            addToolSet(cotton_candy_tools, "Cotton Candy", "棉花糖")
            addToolSet(honey_tools, "Honey", "蜜蜡")
            addToolSet(licorice_tools, "Licorice", "盐甘草糖")
            addToolSet(pez_tools, "PEZ", "皮礼士糖")
            addArmorSet(honey_armors, "Honey", "蜜蜡")
            addArmorSet(licorice_armors, "Licorice", "盐甘草糖")
            addArmorSet(pez_armors, "PEZ", "皮礼士糖")
            addItem(water_mask, "Water Mask", "水下面具")
            addItem(jelly_crown, "Jelly Crown", "果冻王冠")
            addItem(jelly_boots, "Jelly Boots", "果冻靴子")
        }
        add(JumpWandItem.TOOLTIP_WAND_USED, "Uses: %s/%s", "剩余次数：%s/%s")
        add(JumpWandItem.TOOLTIP_WAND_RESTORE, "Sneak use %s to restore %d durability.", "潜行使用%s回复%d点耐久")
    }

    fun add(key: String, en: String, zh: String) {
        enUS.add { it.add(key, en) }
        zhCN.add { it.add(key, zh) }
    }

    fun add(key: Component, en: String, zh: String) {
        val key = (key.contents as? TranslatableContents)?.key
            ?: throw IllegalArgumentException("Component is not translatable")
        add(key, en, zh)
    }

    fun addTab(key: Entry<CreativeModeTab>, en: String, zh: String) {
        add(key.get().displayName, en, zh)
    }

    fun addToolSet(key: CItems.ToolSet, en: String, zh: String) {
        addItem(key.sword, "$en Sword", "${zh}剑")
        addItem(key.shovel, "$en Shovel", "${zh}锹")
        addItem(key.pickaxe, "$en Pickaxe", "${zh}镐")
        addItem(key.axe, "$en Axe", "${zh}斧")
        addItem(key.hoe, "$en Hoe", "${zh}锄")
    }

    fun addArmorSet(key: CItems.ArmorSet, en: String, zh: String) {
        addItem(key.helmet, "$en Helmet", "${zh}头盔")
        addItem(key.plate, "$en Plate", "${zh}胸甲")
        addItem(key.leggings, "$en Leggings", "${zh}护腿")
        addItem(key.boots, "$en Boots", "${zh}靴子")
    }

    fun addItem(key: Entry<out Item>, en: String, zh: String) {
        enUS.add { it.addItem(key, en) }
        zhCN.add { it.addItem(key, zh) }
    }

    class SubLanguageProvider(output: PackOutput, locate: String) : LanguageProvider(output, MOD_ID, locate) {
        private val entries = mutableListOf<Consumer<LanguageProvider>>()
        override fun addTranslations() {
            entries.forEach { it.accept(this) }
        }

        fun add(consumer: Consumer<LanguageProvider>) = entries.add(consumer)
    }

    override fun run(output: CachedOutput): CompletableFuture<*> {
        addTranslations()
        return CompletableFuture.allOf(*subs.map { it.run(output) }.toTypedArray())
    }
}