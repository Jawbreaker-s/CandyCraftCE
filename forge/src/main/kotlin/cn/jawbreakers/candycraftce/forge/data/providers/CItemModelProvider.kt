package cn.jawbreakers.candycraftce.forge.data.providers

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.utils.CUtils.key
import cn.jawbreakers.candycraftce.utils.CUtils.mcLoc
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
                chewing_gum,
                cranberry_fish,
                cranberry_fish_cooked,
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
                lemon_jelly,
                raspberry_jelly,
                mint_jelly,
                caramel_jelly,
                strawberry_jelly,
                royal_rations,
                caramel_jelly_slice,
                strawberry_jelly_slice,
                royal_rations_slice,
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
                chocolate_brick,
                white_chocolate_brick,
                caramel_chocolate_brick,
                lollipop_seeds,
                cotton_candy,
            ).forEach {
                when (it) {
                    is Entry<*> -> basicItem(it.get() as Item)
                    is IEntrySet<*> -> it.entries().forEach { entry -> basicItem(entry.get() as Item) }
                    else -> throw IllegalStateException("Unsupported type: $it")
                }
            }
            lollipop_stem.also {
                generated(it, it.id.withPath { path -> "block/${path}_2" })
            }
            dragibus_stick.also {
                withExistingParent(it.id.toString(), "item/handheld_rod".mcLoc())
                    .texture("layer0", it.getTextureLocation())
            }
            listOf(
                licorice_short_sword,
                marshmallow_tools,
                licorice_tools,
                honey_tools,
                pez_tools,
                jump_wand,
                jelly_wand,
                fork,
                marshmallow_debugger
            ).forEach {
                when (it) {
                    is Entry<*> -> handheld(it as Entry<Item>)
                    is IEntrySet<*> -> it.entries().forEach { entry -> handheld(entry as Entry<Item>) }
                    else -> throw IllegalStateException("Unsupported type: $it")
                }
            }
            CItems.spawn_eggs.values.forEach {
                withExistingParent(it.id.toString(), "item/template_spawn_egg".mcLoc())
            }
        }
    }

    companion object {
        private fun Entry<out Item>.getTextureLocation(suffix: String = ""): ResourceLocation {
            return if (suffix.isEmpty()) get().key.withPrefix("item/")
            else get().key.withPath { "item/$it$suffix" }
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