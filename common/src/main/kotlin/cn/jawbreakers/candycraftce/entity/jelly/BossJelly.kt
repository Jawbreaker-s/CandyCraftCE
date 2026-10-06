//package cn.jawbreakers.candycraftce.entity.slime
//
//import cn.jawbreakers.candycraftce.entity.ICandyBoss
//import cn.jawbreakers.candycraftce.utils.CUtils.defineId
//import cn.jawbreakers.candycraftce.utils.CUtils.synched
//import cn.jawbreakers.candycraftce.utils.TickUnit.tick
//import net.minecraft.network.syncher.EntityDataSerializers
//import net.minecraft.world.entity.EntityType
//import net.minecraft.world.level.Level
//
//
///**
// * Created in 2026/10/6 21:20 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
// * Project: CandyCraftCE
// */
//abstract class BossJelly(type: EntityType<out BossJelly>, level: Level) : BasicJelly(type, level), ICandyBoss {
//    companion object {
//        val BOSS_LOST_TARGET_TICKS: Int = 200.tick
//        const val BOSS_TARGET_RANGE: Double = 64.0
//        const val BOSS_JELLY_BALL_MIN_DROP: Int = 16
//        const val BOSS_JELLY_BALL_MAX_DROP: Int = 32
//
//        val BOSS_AWAKE = defineId(EntityDataSerializers.BOOLEAN)
//        val BOSS_SLAM_TICKS = defineId(EntityDataSerializers.INT)
//
//    }
//
//    var awake: Boolean by synched(BOSS_AWAKE)
//    var slamTicks: Int by synched(BOSS_SLAM_TICKS)
//}