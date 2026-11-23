package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.client.hud.ColorBar
import cn.jawbreakers.candycraftce.entity.ICandyBoss.Companion.HALF_FAINT_TICKS
import cn.jawbreakers.candycraftce.entity.ai.OnlyActiveLookControl
import cn.jawbreakers.candycraftce.entity.gummy.BeetleGummyBall
import cn.jawbreakers.candycraftce.entity.gummy.FireGummyBall
import cn.jawbreakers.candycraftce.utils.CLevelUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CLevelUtils.ifServer
import cn.jawbreakers.candycraftce.utils.CUtils.defineId
import cn.jawbreakers.candycraftce.utils.CUtils.synched
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.animal.AbstractGolem
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.min

/**
 * Created in 2026/11/2 00:48 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class LicoriceBeetle(entityType: EntityType<out LicoriceBeetle>, level: Level) : AbstractGolem(entityType, level),
    ICandyBoss, Enemy {
    companion object {
        const val INIT_COOLDOWN = 100
        const val ATTACKING_COOLDOWN = 40
        fun createAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
        }

        val AWAKE = defineId(EntityDataSerializers.BOOLEAN)
        val FAINT = defineId(EntityDataSerializers.INT)
        val FIRING = defineId(EntityDataSerializers.BOOLEAN)
    }

    override fun self(): Mob = this
    override var awake: Boolean by synched(AWAKE)
    override var faint: Int by synched(FAINT)
    override var lostTargetTicks: Int = 0
    override val bossType: CandyBossType = CandyBossType.MiniBoss
    var cooldown = INIT_COOLDOWN
    var firing: Boolean by synched(FIRING)
    var turn: Int = 0
    var mode: Int = 0
    var shoot: Int = 0

    init {
        lookControl = OnlyActiveLookControl(this)
        xpReward = 500
    }

    override fun isInvulnerableTo(source: DamageSource): Boolean {
        return super.isInvulnerableTo(source) && source.directEntity !is BeetleGummyBall
    }

    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(AWAKE, false)
        entityData.define(FAINT, HALF_FAINT_TICKS)
        entityData.define(FIRING, false)
    }

    override fun setTarget(target: LivingEntity?) {
        level().ifServer {
            if (!awake && target != null) {
                awaken(target, false)
                applyBossSource(this.target, target)
            }
            super.setTarget(target)
            if (target != null) lookControl.setLookAt(target)
        }
    }

    override fun registerGoals() {
        super.registerGoals()
        targetSelector.addGoal(1, NearestAttackableTargetGoal(this, Player::class.java, false))
        registerBossGoals(goalSelector)
        goalSelector.addGoal(1, BossBeetleAttackGoal())
    }

    override fun customServerAiStep() {
        bossServerAiStep()
    }

    override fun onServerAwakeTick() {
        super.onServerAwakeTick()
        if (cooldown > 0) cooldown--
    }

    override fun awaken(entity: Entity?, setTarget: Boolean) {
        super.awaken(entity, setTarget)
        resetCooldown()
        resetAttackState()
    }


    override fun tick() {
        super.tick()
        isInvulnerable = true
        level().ifClient { level ->
            if (isActive && firing) {
                repeat(16) { i ->
                    val x = -Mth.sin(
                        (((i * 11.25f) + tickCount) / 90.0f) * Mth.PI
                    ) * Mth.cos(tickCount * 0.05f) * 2.5f + x

                    val z = Mth.cos(
                        (((i * 11.25f) + tickCount) / 90.0f) * Mth.PI
                    ) * Mth.cos(tickCount * 0.05f) * 2.5f + z

                    val y = if (i % 2 == 0) {
                        Mth.cos(tickCount * 0.05f)
                    } else {
                        Mth.cos((tickCount + 10) * 0.05f)
                    } + 3.0f + y
                    level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0)
                }
            }
        }
    }

    override fun onServerSleepingTick() {
        super.onServerSleepingTick()
        if (tickCount % 20 == 0) heal(5f)
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        val hurt = super.hurt(source, amount)
        if (hurt && random.nextInt(10) == 0) {
            setFainting()
        }
        return hurt
    }

    fun resetAttackState() {
        mode = 0
        firing = false
        shoot = 0
        turn = 0
    }

    fun resetCooldown() {
        cooldown = if (target != null) (ATTACKING_COOLDOWN - 40f * (1 - health / maxHealth)).toInt() else INIT_COOLDOWN
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.putInt("Cooldown", cooldown)
        compound.putBoolean("Awake", awake)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        cooldown = compound.getInt("Cooldown")
        awake = compound.getBoolean("Awake")
    }

    private val bars by lazy { ColorBar(0.65f, 0.30f, 0.35f) }
    override fun getBar() = bars
    override fun knockback(strength: Double, x: Double, z: Double) {
    }

    override fun setDeltaMovement(deltaMovement: Vec3) {
        if (deltaMovement.y != 0.0) {
            super.setDeltaMovement(Vec3(0.0, deltaMovement.y, 0.0))
        }
    }

    override fun isPushable(): Boolean = false
    override fun push(x: Double, y: Double, z: Double) {
    }

    override fun move(type: MoverType, pos: Vec3) {
    }

    inner class BossBeetleAttackGoal : Goal() {
        override fun canUse(): Boolean = isActive && target != null && cooldown <= 0
        override fun isInterruptable(): Boolean = false//不可被其他goal替换
        override fun requiresUpdateEveryTick(): Boolean = true
        override fun tick() {
            if (!canUse()) return
            if (mode == 0) {
                if (health < 150 && random.nextInt(6) == 0) {
                    mode = 1
                    shoot = 50
                } else if (health < 250 && random.nextInt(10) == 0) {
                    mode = 2
                    turn = 200
                } else {
                    mode = 0
                }
                //不是普攻，则休眠一下
                if (mode != 0) {
                    setFainting()
                    return
                }
            }
            val beetle = this@LicoriceBeetle
            val level = level()
            val target = target ?: run { end(); return }
            // 以“甲虫中心 -> 玩家眼睛”这条真实的瞄准线为基准，
            // 保证发射点偏移方向与 shootAt 的瞄准方向一致，避免近距离时弹道退化成竖直向上
            val aim = target.eyePosition.subtract(beetle.boundingBox.center)
            val aimDist = aim.length()
            val vec = if (aimDist > 1.0E-3) aim.normalize() else Vec3(0.0, 1.0, 0.0)
            // 偏移量既贴着甲虫身体往外走、又不会越过玩家(至少留 0.5 格余量)
            val offset = (aimDist - 0.5).coerceIn(0.0, 1.0)
            val ballpos = beetle.boundingBox.center.add(vec.scale(offset))

            when (mode) {
                0 -> {
                    level.addFreshEntity(BeetleGummyBall(beetle, level()).apply {
                        isNoGravity = true
                        setPos(ballpos)
                        shootAt(target.eyePosition)
                    })
                    level.playSound(
                        beetle,
                        beetle.blockPosition(),
                        SoundEvents.SNOWBALL_THROW,
                        SoundSource.HOSTILE,
                        1.0f,
                        1.0f
                    )
                    end()
                }

                2 -> {
                    // 火焰
                    val firing = turn > 100
                    beetle.firing = firing
                    if (!firing) {
                        if (tickCount % 2 == 0) {
                            level.addFreshEntity(FireGummyBall(beetle, level()).apply {
                                isNoGravity = true
                                setPos(ballpos)
                                shootAt(target.eyePosition)
                            })
                            level.playSound(
                                beetle,
                                beetle.blockPosition(),
                                SoundEvents.FIRECHARGE_USE,
                                SoundSource.HOSTILE,
                                1.0f,
                                1.0f
                            )
                        }
                    }
                    if (--turn <= 0) end()
                }

                1 -> {
                    if (tickCount % 2 == 0) {
                        // 向上连射
                        level.addFreshEntity(BeetleGummyBall(beetle, level).apply {
                            setPos(ballpos)
                            val mx = (if (random.nextBoolean()) 1 else -1) * (3 + random.nextDouble() * 6) / 40
                            val mz = (if (random.nextBoolean()) 1 else -1) * (3 + random.nextDouble() * 6) / 40
                            setDeltaMovement(mx, 1.5, mz)
                            hasImpulse = true
                        })
                        level.playSound(
                            beetle,
                            beetle.blockPosition(),
                            SoundEvents.SNOWBALL_THROW,
                            SoundSource.HOSTILE,
                            1.0f,
                            1.0f
                        )
                        if (--shoot <= 0) end()
                    }
                }

            }

        }

        fun end() {
            resetCooldown()
            resetAttackState()
        }
    }


}