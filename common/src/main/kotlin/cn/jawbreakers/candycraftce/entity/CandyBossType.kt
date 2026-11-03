package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.CandyCraftCE
import net.minecraft.network.chat.Component

enum class CandyBossType {
    Sentry, MiniBoss, Boss;

    val description: Component = Component.translatable("boss.${CandyCraftCE.MOD_ID}.${name.lowercase()}")
}