package cn.jawbreakers.candycraftce.misc

import net.minecraft.world.entity.player.Player

/**
 * Created in 2026/9/20 13:04
 * Project: CandyCraftCE
 * Author: [Bread_NiceCat](https://github.com/Bread-NiceCat)
 */
object EmblemHelper {
    fun isChewingGumImmune(entity: Player): Boolean {
        return entity.isCreative || entity.isSpectator
    }
}