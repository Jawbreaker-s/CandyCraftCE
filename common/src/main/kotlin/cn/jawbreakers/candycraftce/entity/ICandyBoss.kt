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
    companion object {
        const val BOSS_FOLLOW_RANGE = 32.0
    }

    fun isBarActive(): Boolean
    fun getHealthBarProgress(): Float

    fun getBar(): IBossBar = TexturedBar.RED

    //0f全开，0.5f全闭，1f全开
    fun getFaintProgress(): Float = 0f
    val bossType: CandyBossType
}

enum class CandyBossType {
    Sentry, MiniBoss, Boss;

    val description: Component = Component.translatable("boss.$MOD_ID.${name.lowercase()}")
}


