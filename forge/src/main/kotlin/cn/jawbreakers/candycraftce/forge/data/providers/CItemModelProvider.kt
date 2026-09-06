package cn.jawbreakers.candycraftce.forge.data.providers

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.utils.CUtils.key
import cn.jawbreakers.candycraftce.utils.IEntrySet
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.common.data.ExistingFileHelper

class CItemModelProvider(output: PackOutput, efHelper: ExistingFileHelper) :
    ItemModelProvider(output, CandyCraftCE.MOD_ID, efHelper) {

    @Suppress("UNCHECKED_CAST")
    override fun registerModels() {
        CItems.apply {
            listOf(
                honey_shard,
                nougat_powder,
                pez,
                pez_dust,
                licorice,
                honeycomb,
                chocolate_coin,
                cranberry_scale,
                chocolate_coin,
                cranberry_scale,
                sugar_crystal,
                waffle_nugget,
                marshmallow_stick,
                lollipop_seeds,
                dragibus,
                lollipop,
                marshmallow_flower,
                candied_cherry,
                candy_cane,
                white_green_candy_cane,
                red_green_candy_cane,
                chewing_gum,
                cotton_candy,
                raspberry_cotton_candy,
                cranberry_fish,
                cranberry_fish_cooked,
                dragibus_stick,
                gummy,
                hot_gummy,
                waffle,
                record_1,
                record_2,
                record_3,
                record_4,
                honey_arrow,
                honey_bolt,
                gummy_ball,
                lemon_jelly_ball,
                raspberry_jelly_ball,
                mint_jelly_ball,
                pez_jelly_ball,
                caramel_king_jelly_ball,
                strawberry_queen_jelly_ball,
                dynamite,
                glue_dynamite,
                cranberry_emblem,
                gingerbread_emblem,
                honey_emblem,
                sky_emblem,
                nessie_emblem,
                chewing_gum_emblem,
                jelly_emblem,
                suguard_emblem,
                beetle_dungeon_key,
                jelly_dungeon_key,
                sky_dungeon_key,
                suguard_dungeon_key,
                jelly_sentry_key,
                jelly_boss_key,
                suguard_sentry_key,
                suguard_boss_key,
                caramel_bucket,
                grenadine_bucket,
                liquid_chocolate_bucket,
                liquid_candy_bucket,
                white_hard_candy,
                red_hard_candy,
                green_hard_candy,
                yellow_hard_candy,
                orange_hard_candy,
                light_blue_hard_candy,
                pink_hard_candy,
                purple_hard_candy,
                white_red_hard_candy,
                white_green_hard_candy,
                white_yellow_hard_candy,
                white_orange_hard_candy,
                white_light_blue_hard_candy,
                white_pink_hard_candy,
                white_purple_hard_candy,
                red_green_hard_candy,
                milk_brownie,
                white_brownie,
                dark_brownie,
                milk_chocolate_bar,
                white_chocolate_bar,
                dark_chocolate_bar,
                ruby_chocolate_bar,
                milk_chocolate_egg,
                white_chocolate_egg,
                dark_chocolate_egg,
                orange_gummy,
                yellow_gummy,
                white_gummy,
                green_gummy,
                red_gummy_worm,
                orange_gummy_worm,
                yellow_gummy_worm,
                white_gummy_worm,
                green_gummy_worm,
                hot_gummy_worm,
                lemon_jelly,
                raspberry_jelly,
                mint_jelly,
                caramel_jelly,
                strawberry_jelly,
                royal_rations,
                caramel_jelly_slice,
                strawberry_jelly_slice,
                royal_rations_slice,
                magic_candy,
                butter,
                wafer_stick,
                rock_sugar,
                honey_armors,
                licorice_armors,
                pez_armors,
                water_mask,
                jelly_crown,
                jelly_boots,
                white_chocolate_leaf,
                magical_leaf,
                chocolate_leaf,
                caramel_leaf,
                candied_cherry_leaf,
            ).forEach {
                when (it) {
                    is Entry<*> -> basicItem(it.get() as Item)
                    is IEntrySet<*> -> it.entries().forEach { entry -> basicItem(entry.get() as Item) }
                    else -> throw IllegalStateException("Unsupported type: $it")
                }
            }
            listOf(
                licorice_short_sword,
                marshmallow_tools,
                licorice_tools,
                honey_tools,
                pez_tools,
                milk_chocolate_tools,
                white_chocolate_tools,
                dark_chocolate_tools,
                cotton_candy_tools,
                jump_wand,
                jelly_wand,
                fork
            ).forEach {
                when (it) {
                    is Entry<*> -> handheld(it as Entry<Item>)
                    is IEntrySet<*> -> it.entries().forEach { entry -> handheld(entry as Entry<Item>) }
                    else -> throw IllegalStateException("Unsupported type: $it")
                }
            }
        }
    }

    companion object {
        private fun Entry<out Item>.getTextureLocation(suffix: String = ""): ResourceLocation {
            return if (suffix.isEmpty()) get().key.withPrefix("item/")
            else get().key.withPrefix("item/$suffix/")
        }

        fun ItemModelProvider.generated(item: Entry<out Item>, texture: ResourceLocation = item.getTextureLocation()) {
            withExistingParent(item.id.toString(), "item/generated")
                .texture("layer0", texture)
        }

        fun ItemModelProvider.handheld(item: Entry<out Item>, texture: ResourceLocation = item.getTextureLocation()) {
            withExistingParent(item.id.toString(), "item/handheld")
                .texture("layer0", texture)
        }
    }
}