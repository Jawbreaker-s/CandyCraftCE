package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level


/**
 * Created in 2026/11/7 13:16 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
open class Suguard(entityType: EntityType<out Suguard>, level: Level) : Monster(entityType, level) {
    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
        }
    }

    constructor(level: Level) : this(CEntityTypes.suguard.get(), level)

    var isAngry: Boolean = false
    override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(hand)
        setItemInHand(hand, stack)
        return super.mobInteract(player, hand)
    }

    override fun registerGoals() {
        super.registerGoals()
        goalSelector.addGoal(1, FloatGoal(this))
        goalSelector.addGoal(4, SuguardAttackGoal())
        goalSelector.addGoal(6, LookAtPlayerGoal(this, Player::class.java, 8.0f))
        goalSelector.addGoal(6, RandomLookAroundGoal(this))

        targetSelector.addGoal(1, SuguardTargetGoal())
        targetSelector.addGoal(2, HurtByTargetGoal(this))
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.putBoolean("Angry", isAngry)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        isAngry = compound.getBoolean("Angry")
    }

    inner class SuguardTargetGoal : NearestAttackableTargetGoal<Player>(this, Player::class.java, true) {
        override fun canUse(): Boolean {
            return (isAngry || level().getBiome(blockPosition()).`is`(CBiomes.caramel_forest))
                    && super.canUse()
        }
    }

    inner class SuguardAttackGoal : MeleeAttackGoal(this, 1.0, true) {
        override fun canContinueToUse(): Boolean {
            //追击
            val canUse = super.canContinueToUse()
            if (canUse) {
                if (random.nextBoolean()) {
                    setJumping(true)
                }
            }
            return canUse
        }

        override fun getAttackReachSqr(attackTarget: LivingEntity): Double = 4.0 + attackTarget.bbWidth
    }
}