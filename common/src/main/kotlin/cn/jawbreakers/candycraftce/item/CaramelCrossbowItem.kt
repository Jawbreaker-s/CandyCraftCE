package cn.jawbreakers.candycraftce.item

import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats.ITEM_USED
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import java.util.function.Predicate
import kotlin.math.min

//todo
class CaramelCrossbowItem(properties: Properties) :
    CrossbowItem(properties) {
    override fun getAllSupportedProjectiles() = HONEY_BOLTS

    override fun getSupportedHeldProjectiles() = HONEY_BOLTS

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {

        val crossbow = player.getItemInHand(hand)
        if (isCharged(crossbow)) {
            shootHoneyBolts(level, player, hand, crossbow, 3.15f, 1.0f)
            setCharged(crossbow, false)
            clearChargedProjectiles(crossbow)
            return InteractionResultHolder.consume(crossbow)
        }

        if (!player.getProjectile(crossbow).isEmpty || player.abilities.instabuild) {
            player.startUsingItem(hand)
            return InteractionResultHolder.consume(crossbow)
        }

        return InteractionResultHolder.fail(crossbow)
    }

    override fun releaseUsing(stack: ItemStack, level: Level, entity: LivingEntity, timeLeft: Int) {
        val usedTicks = getUseDuration(stack) - timeLeft
        if (getPowerForTime(usedTicks) >= 1.0f && !isCharged(
                stack
            ) && tryLoadHoneyBolts(entity, stack)
        ) {
            setCharged(stack, true)
            val source =
                if (entity is Player) SoundSource.PLAYERS else SoundSource.HOSTILE
            level.playSound(
                null,
                entity.x,
                entity.y,
                entity.z,
                SoundEvents.CROSSBOW_LOADING_END,
                source,
                1.0f,
                1.0f
            )
        }
    }

    override fun getUseDuration(stack: ItemStack): Int {
        return HONEY_CHARGE_DURATION + 3
    }

    companion object {
        const val HONEY_CHARGE_DURATION: Int = 25
        private val HONEY_BOLTS: Predicate<ItemStack> = Predicate { x -> false }
        //todo
//            Predicate { stack: ItemStack? -> stack.`is`(CCItems.HONEY_BOLT.get()) }

        private fun getPowerForTime(useTicks: Int): Float {
            return min(
                useTicks.toFloat() / HONEY_CHARGE_DURATION.toFloat(),
                1.0f
            )
        }

        private fun tryLoadHoneyBolts(
            entity: LivingEntity,
            crossbow: ItemStack,
        ): Boolean {
//            val multishot = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
//                net.minecraft.world.item.enchantment.Enchantments.MULTISHOT,
//                crossbow
//            )
//            val projectileCount = if (multishot == 0) 1 else 3
//            val creative = entity is Player && entity.abilities.instabuild
//            val ammo = if (creative)
//                ItemStack(CCItems.HONEY_BOLT.get())
//            else
//                entity.getProjectile(crossbow)
//
//            if (ammo.isEmpty || !ammo.`is`(CCItems.HONEY_BOLT.get())) {
//                return false
//            }
//
//            val template = ammo.copy()
//            for (i in 0..<projectileCount) {
//                val loaded = template.copy()
//                loaded.count = 1
//                addChargedProjectile(crossbow, loaded)
//            }
//
//            if (!creative) {
//                ammo.shrink(1)
//                if (ammo.isEmpty && entity is Player) {
//                    entity.inventory.removeItem(ammo)
//                }
//            }

            return true
        }

        private fun shootHoneyBolts(
            level: Level,
            shooter: LivingEntity,
            hand: InteractionHand,
            crossbow: ItemStack,
            velocity: Float,
            inaccuracy: Float,
        ) {
//            val projectiles: MutableList<ItemStack?> =
//                getChargedProjectiles(crossbow)
//            if (projectiles.isEmpty()) {
//                if (shooter is Player && shooter.abilities.instabuild) {
//                    projectiles.add(ItemStack(CCItems.HONEY_BOLT.get()))
//                } else {
//                    return
//                }
//            }
//
//            val angles =
//                if (projectiles.size == 3) floatArrayOf(0.0f, -10.0f, 10.0f) else floatArrayOf(0.0f)
//            for (i in projectiles.indices) {
//                shootHoneyBolt(
//                    level,
//                    shooter,
//                    crossbow,
//                    projectiles[i],
//                    velocity,
//                    inaccuracy,
//                    angles[min(i, angles.size - 1)]
//                )
//            }
//
//            crossbow.hurtAndBreak(projectiles.size, shooter) { living -> living!!.broadcastBreakEvent(hand) }
//
//            level.playSound(
//                null,
//                shooter.x,
//                shooter.y,
//                shooter.z,
//                SoundEvents.CROSSBOW_SHOOT,
//                SoundSource.PLAYERS,
//                1.0f,
//                1.0f
//            )
            if (shooter is ServerPlayer) {
                shooter.awardStat(ITEM_USED.get(crossbow.item))
            }
        }

        private fun shootHoneyBolt(
            level: Level,
            shooter: LivingEntity,
            crossbow: ItemStack,
            projectileStack: ItemStack?,
            velocity: Float,
            inaccuracy: Float,
            yawOffset: Float,
        ) {
            if (level.isClientSide) {
                return
            }

//            val bolt: HoneyBoltEntity = HoneyBoltEntity(level, shooter)
//            bolt.setShotFromCrossbow(true)
//            bolt.setCritArrow(true)
//            val piercing = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
//                net.minecraft.world.item.enchantment.Enchantments.PIERCING,
//                crossbow
//            )
//            if (piercing > 0) {
//                bolt.setPierceLevel(piercing.toByte())
//            }
//            bolt.shootFromRotation(
//                shooter,
//                shooter.xRot,
//                shooter.yRot + yawOffset,
//                0.0f,
//                velocity,
//                inaccuracy
//            )
//            level.addFreshEntity(bolt)
        }

        private fun addChargedProjectile(
            crossbow: ItemStack,
            projectile: ItemStack,
        ) {
            val tag = crossbow.getOrCreateTag()
            val projectiles = tag.getList("ChargedProjectiles", 10)
            val projectileTag = net.minecraft.nbt.CompoundTag()
            projectile.save(projectileTag)
            projectiles.add(projectileTag)
            tag.put("ChargedProjectiles", projectiles)
        }

        private fun getChargedProjectiles(crossbow: ItemStack): MutableList<ItemStack?> {
            val projectiles: MutableList<ItemStack?> =
                java.util.ArrayList<ItemStack?>()
            val tag = crossbow.tag
            if (tag != null && tag.contains("ChargedProjectiles", 9)) {
                val projectileTags = tag.getList("ChargedProjectiles", 10)
                for (i in projectileTags.indices) {
                    projectiles.add(ItemStack.of(projectileTags.getCompound(i)))
                }
            }
            return projectiles
        }

        private fun clearChargedProjectiles(crossbow: ItemStack) {
            val tag = crossbow.tag
            tag?.remove("ChargedProjectiles")
        }
    }
}
