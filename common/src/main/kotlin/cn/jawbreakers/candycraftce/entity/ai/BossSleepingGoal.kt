package cn.jawbreakers.candycraftce.entity.ai

import cn.jawbreakers.candycraftce.entity.ICandyBoss
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.goal.Goal
import java.util.*
import kotlin.math.min

/**
 * Created in 2026/11/3 13:22 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 *
 * 优先级应当最高，位于goalSelector中；
 * 通过占用所有GoalFlag，抑制其他Goal的运行
 */
class BossSleepingGoal<T>(val boss: T) : Goal() where T : ICandyBoss, T : Mob {
    init {
        flags = EnumSet.allOf(Flag::class.java)
    }

    override fun canUse(): Boolean {
        return !boss.awake
    }

    override fun requiresUpdateEveryTick(): Boolean = true
    override fun stop() {
        boss.faint
    }

    override fun tick() {
        boss.target = null
        boss.navigation.stop()
        boss.setJumping(false)
        val movement = boss.deltaMovement
        //禁止移动，但是允许坠落
        if (movement.x != 0.0 || movement.z != 0.0 || movement.y > 0.0) {
            boss.setDeltaMovement(0.0, min(0.0, movement.y), 0.0)
        }
    }

}