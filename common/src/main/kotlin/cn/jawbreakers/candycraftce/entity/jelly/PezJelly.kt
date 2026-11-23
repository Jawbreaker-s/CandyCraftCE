//package cn.jawbreakers.candycraftce.entity.slime
//
//import cn.jawbreakers.candycraftce.utils.CUtils.defineId
//import cn.jawbreakers.candycraftce.utils.TickUnit.tick
//import net.minecraft.network.syncher.EntityDataSerializers
//import net.minecraft.world.entity.EntityType
//import net.minecraft.world.level.Level
//
///**
// * Created in 2026/10/6 21:20 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
// * Project: CandyCraftCE
// */
//class PezJelly(type: EntityType<out PezJelly>, level: Level) : BossJelly(type, level) {
//    companion object {
//        val PEZ_ROLL_ATTACH_WAIT_TICKS = 70.tick
//        val PEZ_ROLL_ATTACK_TICKS = 30.tick
//        val PEZ_ROLL_REST_TICKS = 60.tick
//        val PEZ_ROLLING_TICKS = 160.tick
//        val PEZ_ROLL_TOTAL_TICKS =
//            PEZ_ROLLING_TICKS + PEZ_ROLL_ATTACH_WAIT_TICKS + PEZ_ROLL_ATTACK_TICKS + PEZ_ROLL_REST_TICKS
//        val PEZ_OPEN_ROLL_DIRECTION_TICKS = 40.tick
//        const val PEZ_ENCLOSURE_PROBE_DISTANCE = 32.0
//        val PEZ_ROLL_ACCELERATION_TICKS = 20.tick
//        val PEZ_ROLL_DECELERATION_TICKS = 30.tick
//
//        const val PEZ_ROLL_ANIMATION_SECONDS: Double = 0.9
//        const val PEZ_ATTACH_TRANSITION_DURATION: Int = 5
//
//
//        val PEZ_ROLL_TICKS = defineId(EntityDataSerializers.INT)
//        val PEZ_ATTACH_FACE = defineId(EntityDataSerializers.INT)
//        val PEZ_PREVIOUS_ATTACH_FACE = defineId(EntityDataSerializers.INT)
//        val PEZ_ATTACH_TRANSITION_TICKS = defineId(EntityDataSerializers.INT)
//        val PEZ_ROLL_DIRECTION = defineId(EntityDataSerializers.INT)
//        val PEZ_ROLL_STEPS = defineId(EntityDataSerializers.INT)
//        val PEZ_ROLL_DISTANCE = defineId(EntityDataSerializers.FLOAT)
//    }
//}