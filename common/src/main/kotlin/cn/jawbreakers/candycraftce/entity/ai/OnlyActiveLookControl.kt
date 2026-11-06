package cn.jawbreakers.candycraftce.entity.ai

import cn.jawbreakers.candycraftce.entity.ICandyBoss
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.control.LookControl

class OnlyActiveLookControl<B>(val boss: B) : LookControl(boss) where B : Mob, B : ICandyBoss {
    override fun tick() {
        if (boss.isActive) super.tick()
    }
}