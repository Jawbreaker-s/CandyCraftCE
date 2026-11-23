//package cn.jawbreakers.candycraftce.entity.slime
//
//import cn.jawbreakers.candycraftce.utils.CUtils.defineId
//import cn.jawbreakers.candycraftce.utils.TickUnit.tick
//import net.minecraft.network.syncher.EntityDataSerializers
//import net.minecraft.world.entity.EntityType
//import net.minecraft.world.level.Level
//
///**
// * Created in 2026/10/6 21:21 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
// * Project: CandyCraftCE
// */
//class JellyKing(type: EntityType<out JellyKing>, level: Level) : BossJelly(type, level) {
//    companion object {
//        val KING_EXPAND_POSE_TICKS = 36.tick
//        val KING_DASH_POSE_TICKS = 54.tick
//        val KING_DASH_CHARGE_TICKS = 22.tick
//
//        val KING_EXPAND_TICKS = defineId(EntityDataSerializers.INT)
//        val KING_DASH_TICKS = defineId(EntityDataSerializers.INT)
//    }
//}