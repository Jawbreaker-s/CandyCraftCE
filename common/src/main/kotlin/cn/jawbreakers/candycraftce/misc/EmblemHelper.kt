package cn.jawbreakers.candycraftce.misc

import cn.jawbreakers.candycraftce.item.EmblemItem
import cn.jawbreakers.candycraftce.mixin_stub.IEmblemDataHolder
import cn.jawbreakers.candycraftce.utils.CUtils.use
import net.minecraft.world.entity.player.Player

/**
 * Created in 2026/9/20 13:04
 * Project: CandyCraftCE
 * Author: [Bread_NiceCat](https://github.com/Bread-NiceCat)
 */
object EmblemHelper {

    fun isChewingGumImmune(entity: Player): Boolean {
        return entity.isSpectator
    }

    fun shouldCandyMobAvoid(entity: Player): Boolean {
        return !entity.isSpectator
    }

    fun shouldGingerbreadManAvoid(entity: Player): Boolean {
        return !entity.isSpectator
    }

    const val IN_INVENTORY_TAG = "InInventory"
    fun hasEmblem(player: Player, item: EmblemItem): Boolean {
        val data = (player as IEmblemDataHolder).candycraftce_emblemData
        val flag = data.getLong(IN_INVENTORY_TAG)
        return (flag ushr item.ordinal and 0xFL) != 0L
    }

    fun onServerTick(player: Player) {
        player.level().profiler.use("candycraftce_emblem_update") {
            player as IEmblemDataHolder
            val tag = player.candycraftce_emblemData
            val inventory = player.inventory
            if (player.tickCount % 10 == 0) {
                var flag = 0L
                sequenceOf(inventory.items, inventory.offhand)
                    .flatten()
                    .map { it.item }
                    .filterIsInstance<EmblemItem>()
                    .forEach { flag = flag or (1L shl it.ordinal) }
                tag.putLong(IN_INVENTORY_TAG, flag)
            }
        }
    }
}