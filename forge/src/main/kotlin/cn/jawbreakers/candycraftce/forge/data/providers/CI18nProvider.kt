package cn.jawbreakers.candycraftce.forge.data.providers

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.item.EmblemItem
import cn.jawbreakers.candycraftce.registry.*
import cn.jawbreakers.candycraftce.utils.ILanguageProvider
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraftforge.common.data.LanguageProvider
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class CI18nProvider(output: PackOutput) : DataProvider, ILanguageProvider {
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
            addItem(glue_dynamite, "Chewing Gum Dynamite", "口香糖炸药")
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
            addItem(record_1, "Jelly queen's secret record", "果冻女王的秘密唱片")
//            add(record_1.get().getDisplayName(), "CandyCraft - Jelly queen's secret record", "糖果世界- 果冻女王的秘密唱片")
            addItem(record_2, "Suguard's secret record", "糖卫图腾的秘密唱片")
//            add(record_2.get().getDisplayName(), "CandyCraft - Suguard's secret record", "糖果世界 - 糖卫图腾的秘密唱片")
            addItem(record_3, "Rainbow record", "彩虹唱片")
//            add(record_3.get().getDisplayName(), "CandyCraft - Rainbow record", "糖果世界 - 彩虹唱片")
            addItem(record_4, "Licorice beetle secret record", "甘草甲虫的秘密唱片")
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
            addItem(grenadine_bucket, "Grenadine Bucket", "红石榴糖浆桶")


            addItem(licorice, "Licorice", "盐甘草糖")
            addItem(honey_shard, "Honey Shard", "蜜蜡碎片")
            addItem(honeycomb, "Honeycomb", "蜜蜡")
            addItem(chocolate_coin, "Chocolate Coin", "巧克力币")
            addItem(pez, "PEZ", "皮礼士糖")
            addItem(cranberry_scale, "Cranberry Scale", "蔓越莓鱼鳞")
            addItem(sugar_crystal, "Sugar Crystal", "冰糖")
            addItem(waffle_nugget, "Waffle Nugget", "华夫饼碎屑")
            addItem(marshmallow_stick, "Marshmallow Stick", "棉花软糖棒")
            addItem(lollipop_seeds, "Lollipop Seeds", "棒棒糖种子")
            addItem(dragibus, "Dragibus", "多味珍珠糖")
            addItem(lollipop, "Lollipop", "棒棒糖")
            addItem(marshmallow_flower, "Marshmallow Flower", "棉花软糖花")
            addItem(candied_cherry, "Candied Cherry", "蜜饯樱桃")
            addItem(candy_cane, "Candy Cane", "拐杖糖")
            addItem(chewing_gum, "Chewing Gum", "口香糖")
            addItem(cotton_candy, "Cotton Candy", "棉花糖")
            addItem(cranberry_fish, "Cranberry Fish", "蔓越莓鱼")
            addItem(cranberry_fish_cooked, "Cooked Cranberry Fish", "熟蔓越莓鱼")
            addItem(dragibus_stick, "Dragibus Stick", "多味珍珠糖钓竿")
            addItem(gummy, "Gummy", "软糖")
            addItem(hot_gummy, "Hot Gummy", "熟软糖")
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
            addToolSet(honey_tools, "Honey", "蜜蜡")
            addToolSet(licorice_tools, "Licorice", "盐甘草糖")
            addToolSet(pez_tools, "PEZ", "皮礼士糖")
            addArmorSet(honey_armors, "Honey", "蜜蜡")
            addArmorSet(licorice_armors, "Licorice", "盐甘草糖")
            addArmorSet(pez_armors, "PEZ", "皮礼士糖")
            addItem(water_mask, "Water Mask", "水下面具")
            addItem(jelly_crown, "Jelly Crown", "果冻王冠")
            addItem(jelly_boots, "Jelly Boots", "果冻靴子")
            addItem(white_chocolate_leaf, "White Chocolate Leaf", "白巧克力叶片")
            addItem(magical_leaf, "Magical Leaf", "魔法叶片")
            addItem(chocolate_leaf, "Chocolate Leaf", "巧克力叶片")
            addItem(caramel_leaf, "Caramel Leaf", "焦糖叶片")
            addItem(candied_cherry_leaf, "Candied Cherry Leaf", "蜜饯樱桃叶片")
            addItem(chocolate_brick, "Chocolate Brick", "巧克力砖")
            addItem(white_chocolate_brick, "White Chocolate Brick", "白巧克力砖")
            addItem(caramel_chocolate_brick, "Caramel Chocolate Brick", "焦糖巧克力砖")
            addItem(pez_dust, "PEZ Dust", "皮礼士糖粉")
            addItem(nougat_powder, "Nougat Powder", "牛轧糖粉")
            addItem(marshmallow_debugger, "Marshmallow Debugger", "棉花软糖调试器")
        }
        CBlocks.apply {
            addBlock(custard_pudding_block, "Custard Pudding", "奶皮布丁块")
            addBlock(pudding_block, "Pudding Block", "布丁块")
            addBlock(pudding_farmland, "Pudding Farmland", "布丁耕地")
            addBlock(sugar_sand, "Sugar Sand", "糖砂")
            addBlock(sugar_block, "Sugar Block", "糖块")
            addBlock(marshmallow_log, "Marshmallow Log", "棉花软糖原木")
            addBlock(dark_marshmallow_log, "Dark Marshmallow Log", "深色棉花软糖原木")
            addBlock(light_marshmallow_log, "Light Marshmallow Log", "浅色棉花软糖原木")
            addBlock(stripped_marshmallow_log, "Stripped Marshmallow Log", "去皮棉花软糖原木")
            addBlock(stripped_dark_marshmallow_log, "Stripped Dark Marshmallow Log", "去皮深色棉花软糖原木")
            addBlock(stripped_light_marshmallow_log, "Stripped Light Marshmallow Log", "去皮浅色棉花软糖原木")
            addBlock(marshmallow_planks, "Marshmallow Planks", "棉花软糖木板")
            addBlock(dark_marshmallow_planks, "Dark Marshmallow Planks", "深色棉花软糖木板")
            addBlock(light_marshmallow_planks, "Light Marshmallow Planks", "浅色棉花软糖木板")
            addBlockFamily(marshmallow_family, "Marshmallow", "棉花软糖")
            addBlockFamily(dark_marshmallow_family, "Dark Marshmallow", "深色棉花软糖")
            addBlockFamily(light_marshmallow_family, "Light Marshmallow", "浅色棉花软糖")
            addBlock(candy_cane_block, "Candy Cane Block", "拐杖糖块")
            addBlockFamily(candy_cane_family, "Candy Cane", "拐杖糖")
            addBlock(sugar_brick, "Sugar Brick", "糖块砖")
            addBlockFamily(sugar_family, "Sugar", "糖")
            addBlockFamily(sugar_brick_family, "Sugar Brick", "糖块砖")
            addBlock(cotton_candy_block, "Cotton Sugar Block", "棉花糖块")
            addBlock(raspberry_cotton_candy_block, "Raspberry Cotton Candy Block", "树莓棉花糖块")
            addBlockFamily(cotton_candy_family, "Cotton Candy", "棉花糖")
            addBlockFamily(raspberry_cotton_candy_family, "Cotton Candy", "树莓棉花糖")
            addBlock(marshmallow_ladder, "Marshmallow Ladder", "棉花软糖梯子")
            addBlock(sweet_grass_pink, "Pink Sweet Grass", "粉色糖草")
            addBlock(sweet_grass_pale, "Pale Sweet Grass", "浅色糖草")
            addBlock(sweet_grass_yellow, "Yellow Sweet Grass", "黄色糖草")
            addBlock(sweet_grass_red, "Red Sweet Grass", "红色糖草")
            addBlock(caramel_block, "Caramel Block", "焦糖块")
            addBlockFamily(caramel_family, "Caramel", "焦糖")
            addBlock(caramel_bricks, "Caramel Brick", "焦糖砖")
            addBlockFamily(caramel_bricks_family, "Caramel Brick", "焦糖砖")
            addBlockFamily(cookie_family, "Cookie", "曲奇")
            addBlock(cookie_block, "Cookie Block", "曲奇块")
            addBlock(licorice_block, "Licorice Block", "盐甘草糖块")
            addBlockFamily(licorice_family, "Licorice Block", "盐甘草糖")
            addBlock(licorice_brick, "Licorice Brick", "盐甘草糖砖")
            addBlockFamily(licorice_brick_family, "Licorice Brick", "盐甘草糖砖")
            addBlock(nougat_block, "Nougat Block", "牛轧糖块")
            addBlock(chiseled_nougat_block, "Chiseled Nougat Block", "錾制牛轧糖块")
            addBlock(square_pattern_nougat_block, "Square Pattern Nougat Block", "方纹牛轧糖块")
            addBlock(nougat_head, "Nougat Head", "牛轧糖头")
            addBlockFamily(nougat_family, "Nougat", "牛轧糖")
            //glass
            addBlock(caramel_glass, "Caramel Glass", "焦糖玻璃")
            addBlock(caramel_glass_round, "Round Caramel Glass", "圆形焦糖玻璃")
            addBlock(caramel_glass_diamond, "Diamond Caramel Glass", "钻石形焦糖玻璃")
            addBlock(caramel_pane, "Caramel Glass Pane", "焦糖玻璃板")
            addBlock(caramel_pane_round, "Round Caramel Glass Pane", "圆形焦糖玻璃板")
            addBlock(caramel_pane_diamond, "Diamond Caramel Glass Pane", "钻石形焦糖玻璃板")
            addBlock(dark_caramel_glass, "Dark Caramel Glass", "暗色焦糖玻璃")
            addBlock(dark_caramel_glass_round, "Round Dark Caramel Glass", "圆形暗色焦糖玻璃")
            addBlock(dark_caramel_glass_diamond, "Diamond Dark Caramel Glass", "钻石形暗色焦糖玻璃")
            addBlock(dark_caramel_pane, "Dark Caramel Glass Pane", "暗色焦糖玻璃板")
            addBlock(dark_caramel_pane_round, "Round Dark Caramel Glass Pane", "圆形暗色焦糖玻璃板")
            addBlock(dark_caramel_pane_diamond, "Diamond Dark Caramel Glass Pane", "钻石形暗色焦糖玻璃板")
            addBlock(honey_glass, "Honey Glass", "蜂蜜玻璃")
            addBlock(honey_glass_round, "Round Honey Glass", "圆形蜂蜜玻璃")
            addBlock(honey_glass_diamond, "Diamond Honey Glass", "钻石形蜂蜜玻璃")
            addBlock(honey_pane, "Honey Glass Pane", "蜂蜜玻璃板")
            addBlock(honey_pane_round, "Round Honey Glass Pane", "圆形蜂蜜玻璃板")
            addBlock(honey_pane_diamond, "Diamond Honey Glass Pane", "钻石形蜂蜜玻璃板")
            addBlock(sugar_glass, "Sugar Glass", "白糖玻璃")
            addBlock(sugar_glass_round, "Round Sugar Glass", "圆形白糖玻璃")
            addBlock(sugar_glass_diamond, "Diamond Sugar Glass", "钻石形白糖玻璃")
            addBlock(sugar_pane, "Sugar Glass Pane", "白糖玻璃板")
            addBlock(sugar_pane_round, "Round Sugar Glass Pane", "圆形白糖玻璃板")
            addBlock(sugar_pane_diamond, "Diamond Sugar Glass Pane", "钻石形白糖玻璃板")
            addBlock(grenadine_glass, "Grenadine Glass", "番石榴糖浆玻璃")
            addBlock(grenadine_glass_grid, "Grid Grenadine Glass", "方格番石榴糖浆玻璃")
            addBlock(grenadine_pane, "Grenadine Glass Pane", "番石榴糖浆玻璃板")
            addBlock(grenadine_pane_grid, "Grid Grenadine Glass Pane", "方格番石榴糖浆玻璃板")
            //
            addBlock(ice_cream, "Ice Cream", "冰淇淋")
            addBlockFamily(ice_cream_family, "Ice Cream", "冰淇淋")
            addBlock(strawberry_ice_cream, "Strawberry Ice Cream", "草莓冰淇淋")
            addBlockFamily(strawberry_ice_cream_family, "Strawberry Ice Cream", "草莓冰淇淋")
            addBlock(mint_ice_cream, "Mint Ice Cream", "薄荷冰淇淋")
            addBlockFamily(mint_ice_cream_family, "Mint Ice Cream", "薄荷冰淇淋")
            addBlock(blueberry_ice_cream, "Blueberry Ice Cream", "蓝莓冰淇淋")
            addBlockFamily(blueberry_ice_cream_family, "Blueberry Ice Cream", "蓝莓冰淇淋")
            addBlock(chocolate_ice_cream, "Chocolate Ice Cream", "巧克力冰淇淋")
            addBlockFamily(chocolate_ice_cream_family, "Chocolate Ice Cream", "巧克力冰淇淋")
            addBlock(banana_ice_cream, "Banana Ice Cream", "香蕉冰淇淋")
            addBlockFamily(banana_ice_cream_family, "Banana Ice Cream", "香蕉冰淇淋")
            addBlock(honey_lamp, "Honey Lamp", "蜜蜡灯")
            addBlock(sugar_spikes, "Sugar Spikes", "糖刺")
            addBlock(cranberry_spikes, "Cranberry Spikes", "蔓越莓刺")
            addBlock(candied_cherry_sack, "Candied Cherry Sack", "蜜饯樱桃袋")
            addBlock(purple_trampojelly, "Purple TrampoJelly", "紫色弹跳果冻")
            addBlock(trampojelly, "TrampoJelly", "弹跳果冻")
            addBlock(red_trampojelly, "Red TrampoJelly", "红色弹跳果冻")
            addBlock(yellow_trampojelly, "Yellow TrampoJelly", "黄色弹跳果冻")
            addBlock(jelly_shock_absorber, "Jelly Shock Absorber", "减震果冻块")
            addBlock(grenadine_ice, "Grenadine Ice", "番石榴糖浆冰")
            addBlock(fragile_grenadine_ice, "Fragile Grenadine Ice", "易碎的番石榴糖浆冰")
            addBlock(banana_block, "Banana Block", "香蕉块")
            addBlock(chewing_gum_block, "Chewing Gum Block", "口香糖块")
            addBlock(mint_block, "Mint Block", "薄荷块")
            addBlock(raspberry_block, "Raspberry Block", "树莓块")
            addBlock(honeycomb_block, "Honeycomb Block", "蜜蜡块")
            addBlock(pez_block, "PEZ Block", "皮礼士糖块")
            addBlock(jawbreaker_block, "Jawbreaker Block", "基岩硬糖块")
            addBlock(jawbreaker_light, "Jawbreaker Light", "基岩硬糖灯")
            addBlock(chocolate_stone, "Chocolate Stone", "巧克力石头")
            addBlock(chocolate_bricks, "Chocolate Bricks", "巧克力砖")
            addBlock(chocolate_cobblestone, "Chocolate Cobblestone", "巧克力圆石")
            addBlockFamily(chocolate_stone_family, "Chocolate Stone", "巧克力石头")
            addBlockFamily(chocolate_cobblestone_family, "Chocolate Cobblestone", "巧克力圆石")
            addBlock(white_chocolate_stone, "White Chocolate Stone", "白巧克力石头")
            addBlock(white_chocolate_bricks, "White Chocolate Bricks", "白巧克力砖")
            addBlock(white_chocolate_cobblestone, "White Chocolate Cobblestone", "白巧克力圆石")
            addBlockFamily(white_chocolate_stone_family, "White Chocolate Stone", "白巧克力石头")
            addBlockFamily(white_chocolate_cobblestone_family, "White Chocolate Cobblestone", "白巧克力圆石")


            addBlockFamily(mint_family, "Mint", "薄荷")

            addBlock(licorice_ore, "Licorice Ore", "盐甘草矿石")
            addBlock(jelly_ore, "Jelly Ore", "果冻矿石")
            addBlock(pez_ore, "PEZ Ore", "皮礼士糖矿石")
            addBlock(nougat_ore, "Nougat Ore", "牛轧糖矿石")
            addBlock(honey_ore, "Honey Ore", "蜜蜡矿石")
            addBlock(white_licorice_ore, "Licorice Ore", "盐甘草矿石")
            addBlock(white_jelly_ore, "Jelly Ore", "果冻矿石")
            addBlock(white_pez_ore, "PEZ Ore", "皮礼士糖矿石")
            addBlock(white_nougat_ore, "Nougat Ore", "牛轧糖矿石")
            addBlock(white_honey_ore, "Honey Ore", "蜜蜡矿石")

            addBlock(chocolate_leaves, "Chocolate Leaves", "巧克力树叶")
            addBlock(ice_cream_leaves, "Ice Cream Leaves", "冰淇淋树叶")
            addBlock(candied_cherry_leaves, "Candied Cherry Leaves", "蜜饯樱桃树叶")
            addBlock(caramel_leaves, "Caramel Leaves", "焦糖树叶")
            addBlock(enchant_candy_leaves, "Enchant Candy Leaves", "附魔树叶")
            addBlock(chocolate_sapling, "Chocolate Sapling", "巧克力树苗")
            addBlock(caramel_sapling, "Caramel Sapling", "焦糖树苗")
            addBlock(ice_cream_sapling, "Ice Cream Sapling", "冰淇淋树苗")
            addBlock(candied_cherry_sapling, "Candied Cherry Sapling", "蜜饯樱桃树苗")
            addBlock(fraise_tagada_flower, "Fraise Tagada Flower", "果蜜花")
            addBlock(acid_mint_flower, "Acid Mint Flower", "酸薄荷花")
            addBlock(sugar_essence_flower, "Sugar Essence Flower", "金糖花")
            addBlock(chewing_gum_puddle, "Chewing Gum Puddle", "口香糖片")
            addBlock(marshmallow_slice, "Marshmallow Slice", "棉花糖片")
            addBlock(marshmallow_slice_flower, "Marshmallow Slice", "棉花糖片")
            addBlock(rope_licorice, "Rope Licorice", "绳状盐甘草糖")
            addBlock(mint, "Mint", "水生薄荷")
            addBlock(banana_seaweed, "Banana Seaweed", "香蕉海草")
            addBlock(dragibus_crops, "Dragibus Crops", "多味珍珠糖作物")
            addBlock(lollipop_stem, "Lollipop Stem", "棒棒糖茎")
            addBlock(lollipop_fruit, "Lollipop Fruit", "棒棒糖果实")
            addBlock(caramel_portal, "Caramel Portal", "焦糖传送门")
            //Fluids
            addBlock(caramel, "Caramel", "焦糖")
            addBlock(grenadine, "Grenadine", "红石榴糖浆")
        }

        CEntityTypes.apply {
            addEntity(honey_arrow, "Honey Arrow", "蜜蜡箭")
            addEntity(dynamite, "Nougat Dynamite", "牛轧糖炸药")
            addEntity(glue_dynamite, "Chewing Gum Dynamite", "口香糖炸药")
            addEntity(candy_pig, "Candy Cane Pig", "拐杖糖猪")
            addEntity(waffle_sheep, "Waffle Sheep", "华夫饼羊")
            addEntity(gummy_bunny, "Gummy Bunny", "果冻兔")
            addEntity(cranfish, "Cranfish", "蔓越莓鱼")
            addEntity(cookie_creeper, "Cookie Creeper", "饼干苦力怕")
            addEntity(caramel_bee, "Caramel Bee", "焦糖蜂")

        }
        CMobEffects.apply {
            addMobEffects(cloying, "Cloying", "腻滞")
            addMobEffects(propolis, "Propolis", "蜂胶禁锢")
        }
    }

    override fun add(key: String, en: String, zh: String) {
        zhCN.add { add(key, zh) }
        enUS.add { add(key, en) }
    }

    override fun add(key: Component, en: String, zh: String) {
        val key = (key.contents as? TranslatableContents)?.key
            ?: throw IllegalArgumentException("Component is not translatable")
        add(key, en, zh)
    }

    fun addEntity(key: Entry<out EntityType<*>>, en: String, zh: String) {
        zhCN.add(key.get(), zh)
        enUS.add(key.get(), en)
    }

    fun addMobEffects(key: Entry<out MobEffect>, en: String, zh: String) {
        zhCN.add(key.get(), zh)
        enUS.add(key.get(), en)
    }

    fun addTab(key: Entry<CreativeModeTab>, en: String, zh: String) {
        add(key.get().displayName, en, zh)
    }

    fun addBlockFamily(family: CBlocks.BlockFamily, en: String, zh: String) {
        if (family.stairs != null) {
            addBlock(family.stairs!!, "$en Stairs", "${zh}楼梯")
        }
        if (family.slab != null) {
            addBlock(family.slab!!, "$en Slab", "${zh}台阶")
        }
        if (family.fence != null) {
            addBlock(family.fence!!, "$en Fence", "${zh}栅栏")
        }
        if (family.fenceGate != null) {
            addBlock(family.fenceGate!!, "$en Fence Gate", "${zh}栅栏门")
        }
        if (family.door != null) {
            addBlock(family.door!!, "$en Door", "${zh}门")
        }
        if (family.trapdoor != null) {
            addBlock(family.trapdoor!!, "$en Trapdoor", "${zh}陷阱门")
        }
        if (family.sign != null) {
            addBlock(family.sign!!, "$en Sign", "${zh}牌子")
        }
        if (family.wall != null) {
            addBlock(family.wall!!, "$en Wall", "${zh}墙")
        }
    }

    fun addBlock(key: Entry<out Block>, en: String, zh: String) {
        enUS.add { addBlock(key, en) }
        zhCN.add { addBlock(key, zh) }
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
        enUS.add { addItem(key, en) }
        zhCN.add { addItem(key, zh) }
    }

    class SubLanguageProvider(output: PackOutput, locate: String) : LanguageProvider(output, MOD_ID, locate) {
        private val entries = mutableListOf<Consumer<LanguageProvider>>()
        override fun addTranslations() {
            entries.forEach { it.accept(this) }
        }

        fun add(consumer: LanguageProvider.() -> Unit) = entries.add(consumer)
    }

    override fun run(output: CachedOutput): CompletableFuture<*> {
        addTranslations()
        return CompletableFuture.allOf(*subs.map { it.run(output) }.toTypedArray())
    }
}