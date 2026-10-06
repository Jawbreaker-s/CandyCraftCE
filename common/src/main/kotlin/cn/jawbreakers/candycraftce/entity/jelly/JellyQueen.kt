//package cn.jawbreakers.candycraftce.entity.slime
//
//import cn.jawbreakers.candycraftce.utils.CUtils.defineId
//import cn.jawbreakers.candycraftce.utils.CUtils.synched
//import net.minecraft.network.syncher.EntityDataSerializers
//import net.minecraft.world.entity.EntityType
//import net.minecraft.world.level.Level
//
///**
// * Created in 2026/10/6 21:18 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
// * Project: CandyCraftCE
// */
//class JellyQueen(type: EntityType<out JellyQueen>, level: Level) : BossJelly(type, level) {
//    companion object {
//        const val JELLY_QUEEN_SLEEP_MODE: Int = 0
//        const val JELLY_QUEEN_PINK_MODE: Int = 1
//        const val JELLY_QUEEN_BLUE_MODE: Int = 2
//        const val JELLY_QUEEN_BROWN_MODE: Int = 3
//
//        val JELLY_QUEEN_MODE = defineId(EntityDataSerializers.INT)
//    }
//
//    var mode: Int by synched(JELLY_QUEEN_MODE)
//}
