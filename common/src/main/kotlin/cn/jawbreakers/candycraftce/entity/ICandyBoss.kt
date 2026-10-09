package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.client.hud.IBossBar
import cn.jawbreakers.candycraftce.client.hud.TexturedBar
import net.minecraft.network.chat.Component

/**
 * Created in 2026/10/6 21:19 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
interface ICandyBoss {
    fun isBarActive(): Boolean
    fun getHealthBarProgress(): Float
    fun getBar(): IBossBar = TexturedBar.RED
    val bossType: CandyBossType

}

enum class CandyBossType {
    Sentry, MiniBoss, Boss;

    val description: Component = Component.translatable("boss.$MOD_ID.${name.lowercase()}")
}


