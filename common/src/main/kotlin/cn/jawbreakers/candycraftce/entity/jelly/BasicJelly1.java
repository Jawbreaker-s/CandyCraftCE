package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCFluids;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 糖果史莱姆统一实体类。
 *
 * <p>这个类同时承担了 6 种实体的行为实现：
 * <ul>
 *     <li>普通果冻：{@code YELLOW_JELLY}、{@code RED_JELLY}、{@code TORNADO_JELLY}</li>
 *     <li>Boss：{@code PEZ_JELLY}、{@code KING_SLIME}、{@code JELLY_QUEEN}</li>
 * </ul>
 *
 * <p>它没有使用独立子类，而是通过 {@code getType() == CCEntityTypes.XXX} 判断当前实例属于哪一种，
 * 然后在方法中走不同的分支。这样做的好处是共享史莱姆基础逻辑（跳跃、挤压、尺寸），
 * 坏处是单类非常庞大、状态很多，需要小心维护。
 *
 * <p>Boss 相关功能包括：
 * <ul>
 *     <li>休眠/唤醒状态机，未唤醒时冻结并回血</li>
 *     <li>服务端 Boss 血条（{@link ServerBossEvent}）</li>
 *     <li>砸地、膨胀、冲刺、弹跳、Pez 滚轮等技能</li>
 *     <li>阶段模式（果冻女王颜色/强度阶段）</li>
 *     <li>召唤物与召唤者/目标的继承关系</li>
 * </ul>
 */
public class BasicCandySlimeEntity extends Slime {
	/**
	 * 缓存所有方向，避免每次使用时重新分配数组。
	 */
	private static final Direction[] DIRECTIONS = Direction.values();

	/**
	 * 预计算的每个方向的单位向量，与 {@link #DIRECTIONS} 索引一一对应。
	 */
	private static final Vec3[] DIRECTION_VECTORS = createDirectionVectors();

	/**
	 * 休眠 Boss 每隔多少 tick 回一次血。
	 */
	private static final int DORMANT_BOSS_HEAL_INTERVAL_TICKS = 20;

	/**
	 * 休眠 Boss 每次回血的生命值。
	 */
	private static final float DORMANT_BOSS_HEAL_AMOUNT = 5.0F;

	// ===== 果冻女王阶段模式 =====
	/**
	 * 果冻女王：休眠模式。
	 */
	public static final int JELLY_QUEEN_SLEEP_MODE = 0;
	/**
	 * 果冻女王：粉色模式（生命 > 50%）。
	 */
	public static final int JELLY_QUEEN_PINK_MODE = 1;
	/**
	 * 果冻女王：蓝色模式（生命 <= 50%，跳得更快、砸地更远）。
	 */
	public static final int JELLY_QUEEN_BLUE_MODE = 2;
	/**
	 * 果冻女王：棕色模式（生命 <= 25%，落地双重爆炸）。
	 */
	public static final int JELLY_QUEEN_BROWN_MODE = 3;

	// ===== Boss 动画/技能时长（单位：tick） =====
	/**
	 * Boss 砸地动作持续多少 tick。
	 */
	private static final int BOSS_SLAM_POSE_TICKS = 52;
	/**
	 * 国王膨胀动作持续多少 tick。
	 */
	private static final int KING_EXPAND_POSE_TICKS = 36;
	/**
	 * 国王冲刺动作持续多少 tick。
	 */
	private static final int KING_DASH_POSE_TICKS = 54;
	/**
	 * 国王冲刺前蓄力多少 tick，之后才真正释放冲刺。
	 */
	private static final int KING_DASH_CHARGE_TICKS = 22;
	/**
	 * Boss 弹跳动作持续多少 tick。
	 */
	private static final int BOSS_BOUNCE_POSE_TICKS = 64;
	/**
	 * Boss 弹跳前蓄力多少 tick。
	 */
	private static final int BOSS_BOUNCE_CHARGE_TICKS = 40;
	/**
	 * 技能释放后强制休息多少 tick。
	 */
	private static final int BOSS_REST_TICKS = 40;

	// ===== Pez 滚轮技能各阶段时长 =====
	/**
	 * Pez 滚轮：实际滚动阶段 tick 数。
	 */
	private static final int PEZ_ROLLING_TICKS = 160;
	/**
	 * Pez 滚轮：滚动结束后贴墙等待 tick 数。
	 */
	private static final int PEZ_ROLL_ATTACH_WAIT_TICKS = 70;
	/**
	 * Pez 滚轮：贴墙后的攻击阶段 tick 数。
	 */
	private static final int PEZ_ROLL_ATTACK_TICKS = 30;
	/**
	 * Pez 滚轮：结束后休息 tick 数。
	 */
	private static final int PEZ_ROLL_REST_TICKS = 60;
	/**
	 * Pez 滚轮技能总时长。
	 */
	private static final int PEZ_ROLL_TOTAL_TICKS = PEZ_ROLLING_TICKS + PEZ_ROLL_ATTACH_WAIT_TICKS + PEZ_ROLL_ATTACK_TICKS + PEZ_ROLL_REST_TICKS;
	/**
	 * Pez 开放空间滚动时，每隔多少 tick 重新瞄准一次。
	 */
	private static final int PEZ_OPEN_ROLL_DIRECTION_TICKS = 40;
	/**
	 * Pez 探测“是否在封闭空间”时，向四周检测的最大距离。
	 */
	private static final double PEZ_ENCLOSURE_PROBE_DISTANCE = 32.0D;
	/**
	 * Pez 滚轮加速阶段 tick 数。
	 */
	private static final int PEZ_ROLL_ACCELERATION_TICKS = 20;
	/**
	 * Pez 滚轮减速阶段 tick 数。
	 */
	private static final int PEZ_ROLL_DECELERATION_TICKS = 30;
	/**
	 * 继承目标解析间隔 tick 数，避免每 tick 都查实体。
	 */
	private static final int INHERITED_TARGET_RESOLVE_INTERVAL = 10;
	/**
	 * Pez 滚动动画播放时长（秒），用于推算匹配速度。
	 */
	private static final double PEZ_ROLL_ANIMATION_SECONDS = 0.9D;
	/**
	 * Pez 贴面过渡动画持续 tick 数。
	 */
	private static final int PEZ_ATTACH_TRANSITION_DURATION = 5;

	// ===== Boss 通用参数 =====
	/**
	 * Boss 丢失目标后维持追击状态的最长 tick 数，超时则休眠。
	 */
	private static final int BOSS_LOST_TARGET_TICKS = 200;
	/**
	 * Boss 死亡掉落果冻球的最小数量。
	 */
	private static final int BOSS_JELLY_BALL_MIN_DROP = 16;
	/**
	 * Boss 死亡掉落果冻球的最大数量。
	 */
	private static final int BOSS_JELLY_BALL_MAX_DROP = 32;
	/**
	 * Boss 追击目标的最远距离。
	 */
	private static final double BOSS_TARGET_RANGE = 64.0D;

	// ===== 同步数据 =====
	/**
	 * 同步到客户端的 Boss 是否已唤醒。
	 */
	private static final EntityDataAccessor<Boolean> BOSS_AWAKE = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.BOOLEAN);
	/**
	 * 同步到客户端的果冻女王阶段模式。
	 */
	private static final EntityDataAccessor<Integer> JELLY_QUEEN_MODE = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
	/**
	 * 同步到客户端的 Boss 砸地动画剩余 tick。
	 */
	private static final EntityDataAccessor<Integer> BOSS_SLAM_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
	/**
	 * 同步到客户端的国王膨胀动画剩余 tick。
	 */
	private static final EntityDataAccessor<Integer> KING_EXPAND_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
	/**
	 * 同步到客户端的国王冲刺动画剩余 tick。
	 */
	private static final EntityDataAccessor<Integer> KING_DASH_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
	/**
	 * 同步到客户端的 Pez 滚轮技能剩余 tick。
	 */
	private static final EntityDataAccessor<Integer> PEZ_ROLL_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
	/**
	 * 同步到客户端的 Pez 当前贴附面。
	 */
	private static final EntityDataAccessor<Integer> PEZ_ATTACH_FACE = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
	/**
	 * 同步到客户端的 Pez 上一次贴附面，用于过渡动画。
	 */
	private static final EntityDataAccessor<Integer> PEZ_PREVIOUS_ATTACH_FACE = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
	/**
	 * 同步到客户端的 Pez 贴面过渡剩余 tick。
	 */
	private static final EntityDataAccessor<Integer> PEZ_ATTACH_TRANSITION_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
	/**
	 * 同步到客户端的 Pez 滚动方向。
	 */
	private static final EntityDataAccessor<Integer> PEZ_ROLL_DIRECTION = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
	/**
	 * 同步到客户端的 Pez 滚动步数（调试/渲染用）。
	 */
	private static final EntityDataAccessor<Integer> PEZ_ROLL_STEPS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
	/**
	 * 同步到客户端的 Pez 累计滚动距离。
	 */
	private static final EntityDataAccessor<Float> PEZ_ROLL_DISTANCE = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.FLOAT);
	/**
	 * 同步到客户端的 Boss 弹跳动画剩余 tick。
	 */
	private static final EntityDataAccessor<Integer> BOSS_BOUNCE_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
	/**
	 * 同步到客户端的 Boss 休息剩余 tick。
	 */
	private static final EntityDataAccessor<Integer> BOSS_RESTING_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);

	// ===== 运行时状态（不需要同步，仅服务端逻辑使用） =====
	/**
	 * 特殊攻击冷却，防止短时间内重复造成接触伤害。
	 */
	private int specialAttackCooldown;
	/**
	 * 休眠时的朝向是否已初始化。
	 */
	private boolean dormantRotationInitialized;
	/**
	 * 休眠时锁定的 yaw。
	 */
	private float dormantYRot;
	/**
	 * 休眠时锁定的头部 yaw。
	 */
	private float dormantYHeadRot;
	/**
	 * Boss 跳跃冷却。
	 */
	private int bossJumpCooldown;
	/**
	 * Boss 丢失目标后的剩余容忍 tick。
	 */
	private int bossLostTargetTicks;
	/**
	 * Boss 远程攻击冷却。
	 */
	private int bossRangedCooldown;
	/**
	 * 国王膨胀技能冷却。
	 */
	private int kingExpandCooldown;
	/**
	 * 国王冲刺技能冷却。
	 */
	private int kingDashCooldown;
	/**
	 * 国王冲刺蓄力剩余 tick。
	 */
	private int kingDashChargeTicks;
	/**
	 * Boss 弹跳技能冷却。
	 */
	private int bossBounceCooldown;
	/**
	 * 继承目标解析冷却。
	 */
	private int inheritedTargetResolveCooldown;
	/**
	 * 本次弹跳是否还未结算伤害。
	 */
	private boolean bossBounceDamageReady;
	/**
	 * 本次膨胀是否还未结算伤害。
	 */
	private boolean kingExpandDamageReady;
	/**
	 * 国王冲刺锁定的目标。
	 */
	@Nullable
	private LivingEntity kingDashTarget;
	/**
	 * 国王/女王连续多次砸中多个目标时的计数，用于触发膨胀。
	 */
	private int bossMultiTargetSlamCount;
	/**
	 * Pez 砸地次数，累积到一定程度触发滚轮技能。
	 */
	private int pezSlamCount;
	/**
	 * Pez 滚轮技能冷却。
	 */
	private int pezRollCooldown;
	/**
	 * Pez 滚动时对“擦碰”目标的伤害冷却。
	 */
	private int pezRollBrushDamageCooldown;
	/**
	 * Pez 开放空间滚动时使用的切向方向。
	 */
	private Vec3 pezRollTangent = Vec3.ZERO;
	/**
	 * Pez 滚轮攻击阶段是否已经释放。
	 */
	private boolean pezRollAttackReleased;
	/**
	 * Pez 是否在开放空间滚动（false 表示在封闭空间贴墙滚动）。
	 */
	private boolean pezOpenGroundRoll;
	/**
	 * Pez 开放空间滚动中已经命中过的目标 ID，避免同一次滚动重复伤害同一目标。
	 */
	private final Set<Integer> pezOpenRollHitTargets = new HashSet<>();
	/**
	 * Pez 滚轮锁定的目标。
	 */
	@Nullable
	private LivingEntity pezRollTarget;
	/**
	 * 上一次记录时 Boss 是否在地面，用于检测砸地落地瞬间。
	 */
	private boolean bossWasOnGround = true;
	/**
	 * 上一次砸地时的下落距离，用于判断是否触发“高度加成冲刺”。
	 */
	private float bossLastFallDistance;
	/**
	 * 是否处于砸地攻击中。
	 */
	private boolean bossSlamAttackActive;
	/**
	 * 本次砸地是否还未结算伤害。
	 */
	private boolean bossSlamDamageReady;
	/**
	 * 砸地时被水平阻挡的累计 tick，用于判断是否需要绕行。
	 */
	private int bossSlamBlockedTicks;
	/**
	 * 砸地绕行时交替使用的方向侧。
	 */
	private int bossSlamStrafeSide = 1;
	/**
	 * Pez 死亡分裂是否已经执行过，避免重复分裂。
	 */
	private boolean pezDeathSplitSpawned;
	/**
	 * 服务端 Boss 血条。
	 */
	private final ServerBossEvent bossEvent = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
	/**
	 * Boss 被攻击后临时锁定的报复目标。
	 */
	@Nullable
	private LivingEntity bossRetaliationTarget;
	/**
	 * 召唤者 UUID，用于识别盟友。
	 */
	@Nullable
	private UUID jellySummonerUuid;
	/**
	 * 召唤时继承的目标 UUID。
	 */
	@Nullable
	private UUID inheritedBossTargetUuid;

	public BasicCandySlimeEntity(EntityType<? extends BasicCandySlimeEntity> type, Level level) {
		super(type, level);
		// Boss 类型在构造时初始化血条名称/颜色（颜色可能随后由阶段更新）。
		if (isCandyBoss()) {
			bossEvent.setName(type.getDescription());
			bossEvent.setColor(getBossBarColor());
		}
	}

	/**
	 * 注册 AI 目标。
	 *
	 * <p>这里清空了原版史莱姆的所有 target goal，只保留“攻击玩家”。
	 * 是否真的能攻击由 {@code CandyTargeting.canAttackPlayer} 决定，比如创造/和平模式下跳过。
	 */
	@Override
	protected void registerGoals() {
		super.registerGoals();
		targetSelector.removeAllGoals(goal -> true);
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
				entity -> entity instanceof Player player && CandyTargeting.canAttackPlayer(player)));
	}

	/**
	 * 定义需要同步到客户端的数据。
	 *
	 * <p>注意：这里按实体类型按需注册，避免普通果冻也携带一堆 Boss 字段。
	 * 所以后续所有 get/set 方法都先判断类型，不满足就直接返回默认值。
	 */
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		if (isCandyBoss()) {
			entityData.define(BOSS_AWAKE, false);
			entityData.define(BOSS_SLAM_TICKS, 0);
			entityData.define(KING_EXPAND_TICKS, 0);
			entityData.define(BOSS_RESTING_TICKS, 0);
		}
		if (isJellyQueen()) {
			entityData.define(JELLY_QUEEN_MODE, JELLY_QUEEN_SLEEP_MODE);
		}
		if (isKingSlime() || isJellyQueen()) {
			entityData.define(KING_DASH_TICKS, 0);
			entityData.define(BOSS_BOUNCE_TICKS, 0);
		}
		if (isPezJelly()) {
			entityData.define(PEZ_ROLL_TICKS, 0);
			entityData.define(PEZ_ATTACH_FACE, Direction.UP.ordinal());
			entityData.define(PEZ_PREVIOUS_ATTACH_FACE, Direction.UP.ordinal());
			entityData.define(PEZ_ATTACH_TRANSITION_TICKS, 0);
			entityData.define(PEZ_ROLL_DIRECTION, Direction.NORTH.ordinal());
			entityData.define(PEZ_ROLL_STEPS, 0);
			entityData.define(PEZ_ROLL_DISTANCE, 0.0F);
		}
	}

	/**
	 * 生成时收尾。主要目的是应用旧版（1.12 以前）的固定尺寸规则。
	 */
	@Override
	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
		SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
		applyLegacySpawnSize();
		return data;
	}

	/**
	 * 每 tick 的 AI 主循环。
	 *
	 * <p>执行顺序大致是：
	 * <ol>
	 *     <li>Pez 滚动阶段：禁用常规移动（防止重力/摩擦干扰手动位移）</li>
	 *     <li>休眠 Boss 冻结（要在 super.aiStep 之前之后各一次，防止原版逻辑改动朝向）</li>
	 *     <li>调用原版 Slime AI</li>
	 *     <li>继承目标解析、Boss 血条、Boss 服务端行为</li>
	 *     <li>报复目标检测（普通果冻）</li>
	 * </ol>
	 */
	@Override
	public void aiStep() {
		boolean candyBoss = isCandyBoss();
		boolean clientSide = level().isClientSide;
		// Pez 滚动期间完全接管位移，这里先把速度清零避免原版移动叠加。
		if (!clientSide && isPezRollingMovement()) {
			setDeltaMovement(Vec3.ZERO);
		}
		if (candyBoss && !isBossAwake()) {
			freezeSleepingBoss();
		}
		super.aiStep();
		if (!clientSide && inheritedBossTargetUuid != null) {
			tickInheritedBossTarget();
		}
		if (candyBoss && !clientSide) {
			updateBossBar();
		}
		if (specialAttackCooldown > 0) {
			specialAttackCooldown--;
		}
		// 龙卷风果冻在空中时喷云，纯客户端表现。
		if (isTornadoJelly() && !onGround() && clientSide && tickCount % 4 == 0) {
			level().addParticle(ParticleTypes.CLOUD, getRandomX(0.8D), getRandomY(), getRandomZ(0.8D), 0.0D, 0.02D, 0.0D);
		}
		if (candyBoss && !clientSide) {
			tickServerBossBehavior();
		}
		// 再次冻结，防止 super.aiStep 或 Boss 行为修改了朝向/速度。
		if (candyBoss && !isBossAwake()) {
			freezeSleepingBoss();
		}
		if (!clientSide) {
			tickRetaliationTarget();
		}
	}

	/**
	 * 移动处理。Pez 滚动期间完全接管位移，直接跳过原版 travel，避免重力/跳跃干扰。
	 */
	@Override
	public void travel(Vec3 travelVector) {
		if (!level().isClientSide && isPezRollingMovement()) {
			return;
		}
		super.travel(travelVector);
	}

	/**
	 * 是否处于 Pez 滚轮技能的“滚动移动”阶段（不含贴墙/攻击/休息）。
	 */
	private boolean isPezRollingMovement() {
		return isPezJelly() && getPezRollTicks() >= PEZ_ROLL_TOTAL_TICKS - PEZ_ROLLING_TICKS;
	}

	/**
	 * Boss 服务端行为的统一入口。按顺序推进各类技能/动画状态机。
	 */
	private void tickServerBossBehavior() {
		tickBossSlamLandingAndAnimation();
		tickKingBossSpecialAnimation();
		tickBossBounceAndRest();
		tickPezRollSkill();
		tickBossAwakeBehavior();
	}

	/**
	 * 各果冻跳跃间隔。
	 * <ul>
	 *     <li>黄色果冻：4 tick（非常频繁）</li>
	 *     <li>Pez：8 tick</li>
	 *     <li>女王：根据阶段模式，蓝色模式跳得更快</li>
	 * </ul>
	 */
	@Override
	protected int getJumpDelay() {
		if (isYellowJelly()) {
			return 4;
		}
		if (isPezJelly()) {
			return 8;
		}
		if (isJellyQueen()) {
			return nextJellyQueenJumpDelay();
		}
		return super.getJumpDelay();
	}

	/**
	 * 起跳。休眠 Boss 不允许起跳。
	 */
	@Override
	protected void jumpFromGround() {
		if (isCandyBoss() && !isBossAwake()) {
			return;
		}
		super.jumpFromGround();
	}

	/**
	 * 玩家与实体碰撞时触发。
	 *
	 * <p>原版史莱姆的接触伤害被 {@link #isDealsDamage()} 禁用了，
	 * 这里实现各果冻的自定义接触效果：
	 * <ul>
	 *     <li>黄色：单体 6 伤害</li>
	 *     <li>红色：6 伤害 + 强度 3 爆炸 + 自毁</li>
	 *     <li>龙卷风：6 伤害 + 强度 1 爆炸 + 自毁</li>
	 *     <li>Pez/国王/女王：走 Boss 接触伤害逻辑</li>
	 * </ul>
	 */
	@Override
	public void playerTouch(Player player) {
		if (!isSurvivalLike(player)) {
			return;
		}
		if (!isAlive() || specialAttackCooldown > 0) {
			return;
		}
		if (isCandyBoss() && !isBossAwake()) {
			return;
		}
		if (isYellowJelly()) {
			specialAttackCooldown = 10;
			player.hurt(damageSources().mobAttack(this), 6.0F);
			playSound(SoundEvents.SLIME_ATTACK, 1.0F, 1.0F);
		} else if (isRedJelly()) {
			specialAttackCooldown = 20;
			if (!level().isClientSide) {
				player.hurt(damageSources().mobAttack(this), 6.0F);
				level().explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.NONE);
				discard();
			}
		} else if (isTornadoJelly()) {
			specialAttackCooldown = 20;
			if (!level().isClientSide) {
				player.hurt(damageSources().mobAttack(this), 6.0F);
				level().explode(this, getX(), getY(), getZ(), 1.0F, Level.ExplosionInteraction.NONE);
				discard();
			}
		} else if (isPezJelly() || isKingSlime() || isJellyQueen()) {
			specialAttackCooldown = 15;
			hurtPlayerWithLegacyBossContact(player);
		}
	}

	/**
	 * 关闭原版 Slime 的接触伤害路径。
	 *
	 * <p>原因有二：
	 * <ol>
	 *     <li>本项目所有接触伤害都在 {@link #playerTouch} / {@link #doHurtTarget} 里手动实现</li>
	 *     <li>原版 {@code isDealsDamage()} 还会让史莱姆直接攻击铁傀儡，本项目不希望这样</li>
	 * </ol>
	 */
	@Override
	protected boolean isDealsDamage() {
		return false;
	}

	/**
	 * 是否在远离玩家时自动消失。所有糖果果冻都不消失。
	 */
	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	/**
	 * 和平模式是否消失。糖果果冻不消失（Boss 更是不能）。
	 */
	@Override
	public boolean shouldDespawnInPeaceful() {
		return false;
	}

	/**
	 * 受伤入口。
	 *
	 * <p>这里处理了很多特殊规则，顺序很重要：
	 * <ol>
	 *     <li>召唤盟友免伤</li>
	 *     <li>Grenadine 液体中的环境伤害免疫</li>
	 *     <li>Boss 对弹射物的反射/减伤</li>
	 *     <li>普通果冻之间默认不互相伤害（除非存在强制冲突）</li>
	 *     <li>被攻击时唤醒 Boss / 设置报复目标</li>
	 *     <li>Boss 免疫摔落伤害</li>
	 *     <li>女王：唤醒 + 阶段更新 + 击退攻击玩家</li>
	 *     <li>国王：受伤后按生命比例缩小</li>
	 *     <li>Boss 被玩家近战命中时击退玩家</li>
	 * </ol>
	 */
	@Override
	public boolean hurt(DamageSource source, float amount) {
		LivingEntity livingAttacker = getLivingAttacker(source);
		// 召唤盟友之间不互相伤害。
		if (livingAttacker instanceof BasicCandySlimeEntity slimeAttacker && isSummonedAlly(slimeAttacker)) {
			return false;
		}
		// 站在 Grenadine 里时，无实体来源的伤害（例如持续伤害）无效。
		if (isInGrenadine() && source.getEntity() == null && source.getDirectEntity() == null) {
			return false;
		}
		// Boss 对弹射物的特殊处理：反射 + 未唤醒时免伤 + 唤醒后大概率免伤。
		if (isCandyBoss() && source.is(DamageTypeTags.IS_PROJECTILE)) {
			boolean wasAwake = isBossAwake();
			if (!level().isClientSide && livingAttacker != null && (wasAwake || shouldDormantBossWakeFromMobAttack(livingAttacker))) {
				activateBossFromDamage(livingAttacker);
			}
			reflectProjectile(source);
			if (!wasAwake) {
				return false;
			}
			if (isBossAwake() && random.nextInt(4) != 0) {
				return false;
			}
			return super.hurt(source, amount * 0.5F);
		}
		// 普通果冻被其他糖果史莱姆攻击时默认免伤，除非双方处于强制冲突（例如互相锁定）。
		if (source.getEntity() instanceof BasicCandySlimeEntity slimeAttacker && !isCandyBoss() && !isPezJelly() && !isForcedJellyConflict(slimeAttacker, this)) {
			return false;
		}
		if (!level().isClientSide && livingAttacker != null) {
			if (isCandyBoss()) {
				activateBossFromDamage(livingAttacker);
			} else {
				setRetaliationTarget(livingAttacker);
			}
		}
		// Boss 免疫摔落伤害。
		if (isCandyBoss() && source.is(DamageTypeTags.IS_FALL)) {
			return false;
		}
		// 女王单独处理：唤醒、阶段更新、近战击退。
		if (isJellyQueen()) {
			if (!level().isClientSide && livingAttacker != null) {
				activateBossFromDamage(livingAttacker);
				if (isBossAwake()) {
					updateJellyQueenMode();
				}
				if (source.getEntity() instanceof Player player && amount > 1.0F && !player.getAbilities().instabuild) {
					knockbackAttackingPlayer(player);
				}
			}
			boolean hurt = super.hurt(source, amount);
			if (!level().isClientSide && hurt && livingAttacker != null) {
				activateBossFromDamage(livingAttacker);
			}
			return hurt;
		}
		// 国王受伤后按当前生命比例缩小体型。
		if (isKingSlime() && !level().isClientSide && getSize() > 1) {
			shrinkKingSlimeFromHealth();
		}
		// Boss 被玩家近战打中时击退玩家，防止贴脸站桩。
		if (isCandyBoss() && !level().isClientSide && source.getEntity() != null) {
			if (source.getEntity() instanceof Player player && amount > 1.0F && !player.getAbilities().instabuild) {
				knockbackAttackingPlayer(player);
			}
		}
		boolean hurt = super.hurt(source, amount);
		if (!level().isClientSide && hurt && isCandyBoss() && livingAttacker != null) {
			activateBossFromDamage(livingAttacker);
		}
		return hurt;
	}

	/**
	 * 从伤害来源中提取“活体攻击者”。优先取 {@code getEntity()}，其次取 {@code getDirectEntity()}。
	 */
	@Nullable
	private static LivingEntity getLivingAttacker(DamageSource source) {
		if (source.getEntity() instanceof LivingEntity attacker) {
			return attacker;
		}
		if (source.getDirectEntity() instanceof LivingEntity attacker) {
			return attacker;
		}
		return null;
	}

	/**
	 * 主动攻击目标（AI 调用）。
	 *
	 * <p>逻辑与 {@link #playerTouch} 类似，只是目标是任意实体，需要先经过 {@link #canAttackTarget} 过滤。
	 */
	@Override
	public boolean doHurtTarget(Entity target) {
		if (!canAttackTarget(target)) {
			setTarget(null);
			return false;
		}
		if (!isAlive() || specialAttackCooldown > 0) {
			return false;
		}
		if (isCandyBoss() && !isBossAwake()) {
			return false;
		}
		if (isYellowJelly()) {
			specialAttackCooldown = 10;
			return hurtCandyTarget(target, 6.0F);
		}
		if (isRedJelly()) {
			specialAttackCooldown = 20;
			if (!level().isClientSide) {
				hurtCandyTarget(target, 6.0F);
				level().explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.NONE);
				discard();
			}
			return true;
		}
		if (isTornadoJelly()) {
			specialAttackCooldown = 20;
			if (!level().isClientSide) {
				hurtCandyTarget(target, 6.0F);
				level().explode(this, getX(), getY(), getZ(), 1.0F, Level.ExplosionInteraction.NONE);
				discard();
			}
			return true;
		}
		if (isPezJelly() || isKingSlime() || isJellyQueen()) {
			specialAttackCooldown = 15;
			return hurtLegacyBossTarget(target);
		}
		return super.doHurtTarget(target);
	}

	/**
	 * 玩家开始追踪本实体时，Boss 加入该玩家的血条。
	 */
	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		if (isCandyBoss()) {
			bossEvent.addPlayer(player);
		}
	}

	/**
	 * 玩家不再追踪本实体时，Boss 从该玩家血条中移除。
	 */
	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		if (isCandyBoss()) {
			bossEvent.removePlayer(player);
		}
	}

	/**
	 * 实体被移除时的收尾。Pez 在死亡且尺寸 > 1 时会分裂一次。
	 */
	@Override
	public void remove(RemovalReason reason) {
		if (!level().isClientSide && reason == RemovalReason.KILLED && getSize() > 1) {
			splitPezJellyOnDeath();
			setSize(1, false);
		}
		super.remove(reason);
	}

	/**
	 * 死亡掉落。
	 * <ul>
	 *     <li>黄色：柠檬果冻球</li>
	 *     <li>红色：覆盆子果冻球</li>
	 *     <li>龙卷风：薄荷果冻球</li>
	 *     <li>女王：唱片 + 果冻钥匙 + 果冻徽章 + 草莓女王果冻球若干</li>
	 *     <li>Pez（最终尺寸）：果冻哨兵钥匙 + Pez 果冻球若干</li>
	 *     <li>国王（最终尺寸）：果冻 Boss 钥匙 + 焦糖国王果冻球若干</li>
	 * </ul>
	 */
	@Override
	protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
		if (isYellowJelly()) {
			spawnAtLocation(CCItems.LEMON_JELLY_BALL.get());
		} else if (isRedJelly()) {
			spawnAtLocation(CCItems.RASPBERRY_JELLY_BALL.get());
		} else if (isTornadoJelly()) {
			spawnAtLocation(CCItems.MINT_JELLY_BALL.get());
		} else if (isJellyQueen()) {
			spawnAtLocation(CCItems.RECORD_1.get());
			spawnAtLocation(CCItems.JELLY_KEY.get());
			spawnAtLocation(CCItems.JELLY_EMBLEM.get());
			dropBossJellyBalls(CCItems.STRAWBERRY_QUEEN_JELLY_BALL.get());
		} else if (isPezJelly() && getSize() <= 1) {
			spawnAtLocation(CCItems.JELLY_SENTRY_KEY.get());
			dropBossJellyBalls(CCItems.PEZ_JELLY_BALL.get());
		} else if (isKingSlime()) {
			if (getSize() <= 1) {
				spawnAtLocation(CCItems.JELLY_BOSS_KEY.get());
			}
			dropBossJellyBalls(CCItems.CARAMEL_KING_JELLY_BALL.get());
		}
	}

	/**
	 * 掉落 Boss 专属果冻球，数量在 [16, 32] 之间随机。
	 */
	private void dropBossJellyBalls(Item jellyBall) {
		int count = BOSS_JELLY_BALL_MIN_DROP
				+ random.nextInt(BOSS_JELLY_BALL_MAX_DROP - BOSS_JELLY_BALL_MIN_DROP + 1);
		spawnAtLocation(new ItemStack(jellyBall, count));
	}

	/**
	 * 保存数据到 NBT。使用 {@code putIntIfNonZero} 精简存档。
	 */
	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (isCandyBoss()) {
			if (isBossAwake()) {
				tag.putBoolean("BossAwake", true);
			}
			putIntIfNonZero(tag, "BossLostTargetTicks", bossLostTargetTicks);
			putIntIfNonZero(tag, "BossRangedCooldown", bossRangedCooldown);
			putIntIfNonZero(tag, "KingExpandCooldown", kingExpandCooldown);
			if (isKingSlime() || isJellyQueen()) {
				putIntIfNonZero(tag, "KingDashCooldown", kingDashCooldown);
				putIntIfNonZero(tag, "BossMultiTargetSlamCount", bossMultiTargetSlamCount);
			}
			if (isPezJelly()) {
				putIntIfNonZero(tag, "PezSlamCount", pezSlamCount);
				putIntIfNonZero(tag, "PezRollCooldown", pezRollCooldown);
			}
		}
		if (jellySummonerUuid != null) {
			tag.putUUID("JellySummoner", jellySummonerUuid);
		}
		if (inheritedBossTargetUuid != null) {
			tag.putUUID("InheritedBossTarget", inheritedBossTargetUuid);
		}
		if (isJellyQueen()) {
			tag.putInt("JellyQueenMode", getJellyQueenMode());
		}
	}

	/**
	 * 仅在值非 0 时写入 NBT，节省空间。
	 */
	private static void putIntIfNonZero(CompoundTag tag, String key, int value) {
		if (value != 0) {
			tag.putInt(key, value);
		}
	}

	/**
	 * 从 NBT 读取数据。女王的阶段模式有默认回退逻辑。
	 */
	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (isCandyBoss()) {
			setBossAwake(tag.getBoolean("BossAwake"));
			bossLostTargetTicks = tag.getInt("BossLostTargetTicks");
			bossRangedCooldown = tag.getInt("BossRangedCooldown");
			kingExpandCooldown = tag.getInt("KingExpandCooldown");
			if (isKingSlime() || isJellyQueen()) {
				kingDashCooldown = tag.getInt("KingDashCooldown");
				bossMultiTargetSlamCount = tag.getInt("BossMultiTargetSlamCount");
			}
			if (isPezJelly()) {
				pezSlamCount = tag.getInt("PezSlamCount");
				pezRollCooldown = tag.getInt("PezRollCooldown");
			}
		}
		jellySummonerUuid = tag.hasUUID("JellySummoner") ? tag.getUUID("JellySummoner") : null;
		inheritedBossTargetUuid = tag.hasUUID("InheritedBossTarget") ? tag.getUUID("InheritedBossTarget") : null;
		if (isJellyQueen()) {
			setJellyQueenMode(tag.contains("JellyQueenMode") ? tag.getInt("JellyQueenMode") : isBossAwake() ? JELLY_QUEEN_PINK_MODE : JELLY_QUEEN_SLEEP_MODE);
		}
	}

	/**
	 * 受伤音效。
	 */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.SLIME_HURT;
	}

	/**
	 * 死亡音效。
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SLIME_DEATH;
	}

	/**
	 * 挤压音效。
	 */
	@Override
	protected SoundEvent getSquishSound() {
		return SoundEvents.SLIME_SQUISH;
	}

	/**
	 * 跳跃音效。
	 */
	@Override
	protected SoundEvent getJumpSound() {
		return SoundEvents.SLIME_JUMP;
	}

	/**
	 * 落地粒子类型。
	 */
	@Override
	protected ParticleOptions getParticleType() {
		return jellyLandingParticle();
	}

	/**
	 * 自定义落地粒子。纯客户端执行，围绕自身一圈生成对应果冻球粒子。
	 */
	@Override
	protected boolean spawnCustomParticles() {
		if (!level().isClientSide) {
			return true;
		}
		int size = getSize();
		ParticleOptions particle = getParticleType();
		for (int i = 0; i < size * 8; i++) {
			float angle = random.nextFloat() * ((float) Math.PI * 2.0F);
			float radius = random.nextFloat() * 0.5F + 0.5F;
			double dx = Mth.sin(angle) * size * 0.5F * radius;
			double dz = Mth.cos(angle) * size * 0.5F * radius;
			level().addParticle(particle, getX() + dx, getY(), getZ() + dz, 0.0D, 0.0D, 0.0D);
		}
		return true;
	}

	/**
	 * 脚步声使用糖果模组的果冻音效。
	 */
	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		playSound(CCSoundEvents.STEP_JELLY.get(), 0.18F, 0.9F + random.nextFloat() * 0.2F);
	}

	/**
	 * 设置尺寸。Boss 会根据类型覆写最大生命与经验：
	 * <ul>
	 *     <li>国王：经验 800，最大生命 800</li>
	 *     <li>女王：经验 500，最大生命 300</li>
	 *     <li>Pez：最大生命 = size * 20（尺寸 10 时为 200）</li>
	 * </ul>
	 */
	@Override
	public void setSize(int size, boolean resetHealth) {
		super.setSize(size, resetHealth);
		AttributeInstance maxHealth = getAttribute(Attributes.MAX_HEALTH);
		if (maxHealth != null) {
			if (isKingSlime()) {
				xpReward = 800;
				setBaseValueIfChanged(maxHealth, 800.0D);
				if (resetHealth) {
					setHealth(getMaxHealth());
				}
			} else if (isJellyQueen()) {
				xpReward = 500;
				setBaseValueIfChanged(maxHealth, 300.0D);
				if (resetHealth) {
					setHealth(getMaxHealth());
				}
			} else if (isPezJelly()) {
				setBaseValueIfChanged(maxHealth, size * 20.0D);
				if (resetHealth) {
					setHealth(getMaxHealth());
				}
			}
		}
	}

	/**
	 * Boss 唤醒时的主行为循环（每 tick）。
	 *
	 * <p>女王的逻辑被拆到 {@link #tickJellyQueenBossBehavior()} 里，因为女王阶段更复杂。
	 */
	private void tickBossAwakeBehavior() {
		if (!isCandyBoss() || level().isClientSide) {
			return;
		}
		// 未唤醒时持续回血。
		if (!isBossAwake()) {
			tickDormantBossRegeneration();
		}
		if (isJellyQueen()) {
			tickJellyQueenBossBehavior();
			return;
		}
		LivingEntity target = findBossAttackTarget();
		AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
		if (target == null) {
			tickBossLostTarget(speed);
			return;
		}
		bossLostTargetTicks = BOSS_LOST_TARGET_TICKS;
		// 技能休息阶段速度归零。
		if (isBossResting()) {
			setBaseValueIfChanged(speed, 0.0D);
			stopHorizontalMovement(true);
			getNavigation().stop();
			return;
		}
		if (isBossAwake()) {
			setBaseValueIfChanged(speed, isJellyQueen() ? 0.7D : isKingSlime() ? 0.45D : 0.38D);
		}
		tickBossRangedAttack(target);
		if (isKingSlime() || isJellyQueen()) {
			tickKingSlimeSpecials(target);
		}
		if (isPezJelly()) {
			tickPezExpandSpecial(target);
			tickPezRollTrigger(target);
			// Pez 滚轮期间锁定速度，交给手动位移。
			if (getPezRollTicks() > 0) {
				setBaseValueIfChanged(speed, 0.0D);
				return;
			}
		}
		if (bossJumpCooldown > 0) {
			bossJumpCooldown--;
		}
		// 常规跳跃砸地：国王间隔更长。
		if (isBossAwake() && onGround() && bossJumpCooldown <= 0) {
			bossJumpCooldown = isKingSlime() ? 18 + random.nextInt(28) : 12 + random.nextInt(22);
			launchBossSlamAt(target);
		}
	}

	/**
	 * 果冻女王的独立 Boss 行为循环。与通用 Boss 行为的区别：
	 * <ul>
	 *     <li>速度固定 0.7</li>
	 *     <li>会更新阶段模式</li>
	 *     <li>跳跃间隔与阶段相关</li>
	 * </ul>
	 */
	private void tickJellyQueenBossBehavior() {
		LivingEntity target = findBossAttackTarget();
		AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
		if (target == null) {
			tickBossLostTarget(speed);
			return;
		}
		bossLostTargetTicks = BOSS_LOST_TARGET_TICKS;

		// 未唤醒时不参与战斗，只保持睡眠模式。
		if (!isBossAwake()) {
			setJellyQueenMode(JELLY_QUEEN_SLEEP_MODE);
			setBaseValueIfChanged(speed, 0.0D);
			stopHorizontalMovement(false);
			return;
		}

		setBaseValueIfChanged(speed, 0.699999988079071D);
		if (isBossResting()) {
			setBaseValueIfChanged(speed, 0.0D);
			stopHorizontalMovement(true);
			getNavigation().stop();
			return;
		}
		updateJellyQueenMode();
		tickBossRangedAttack(target);
		tickKingSlimeSpecials(target);
		if (bossJumpCooldown > 0) {
			bossJumpCooldown--;
		}
		if (onGround() && bossJumpCooldown <= 0) {
			// 女王跳跃间隔大约是普通跳跃延迟的三分之一，蓝色模式最快。
			bossJumpCooldown = Math.max(2, nextJellyQueenJumpDelay() / 3);
			launchBossSlamAt(target);
		}
	}

	/**
	 * 砸地落地检测与动画倒计时。
	 *
	 * <p>核心逻辑：
	 * <ul>
	 *     <li>从空中到落地的瞬间：结算伤害、组合计数、落地特效、可能触发高度加成冲刺</li>
	 *     <li>还在空中且正在下落：持续伤害（允许“砸中”空中目标）</li>
	 *     <li>水平被阻挡：累计 {@code bossSlamBlockedTicks}，供 {@link #chooseBossSlamVector} 绕行使用</li>
	 * </ul>
	 */
	private void tickBossSlamLandingAndAnimation() {
		if (!isCandyBoss() || level().isClientSide) {
			return;
		}
		int slamTicks = getBossSlamTicks();
		if (slamTicks > 0) {
			setBossSlamTicks(slamTicks - 1);
		}
		boolean grounded = onGround();
		if (bossSlamAttackActive && !bossWasOnGround && grounded) {
			// 落地瞬间
			bossLastFallDistance = fallDistance;
			setBossSlamTicks(BOSS_SLAM_POSE_TICKS / 2);
			int hits = damageBossSlamTargets();
			trackBossSlamCombo(hits);
			triggerBossSlamLandingEffects();
			tryStartHeightBonusDash();
			bossSlamAttackActive = false;
			bossSlamDamageReady = false;
		} else if (bossSlamAttackActive && !grounded && getDeltaMovement().y < -0.05D) {
			// 下落过程中也持续尝试造成伤害（例如从高处砸下时碰到空中目标）。
			damageBossSlamTargets();
		}
		if (bossSlamAttackActive && !grounded && horizontalCollision) {
			bossSlamBlockedTicks = Math.min(20, bossSlamBlockedTicks + 1);
		} else if (!bossSlamAttackActive) {
			bossSlamBlockedTicks = Math.max(0, bossSlamBlockedTicks - 1);
		}
		bossWasOnGround = grounded;
	}

	/**
	 * 国王/女王/Pez 的“膨胀”“冲刺”技能倒计时。
	 */
	private void tickKingBossSpecialAnimation() {
		if (!(isPezJelly() || isKingSlime() || isJellyQueen()) || level().isClientSide) {
			return;
		}
		if (kingExpandCooldown > 0) {
			kingExpandCooldown--;
		}
		if (kingDashCooldown > 0) {
			kingDashCooldown--;
		}
		// 膨胀技能：锁定移动，最后 10 tick 结算一次伤害，随后进入休息。
		int expandTicks = getKingExpandTicks();
		if (expandTicks > 0) {
			setKingExpandTicks(expandTicks - 1);
			setDeltaMovement(0.0D, Math.min(0.0D, getDeltaMovement().y), 0.0D);
			getNavigation().stop();
			if (kingExpandDamageReady && expandTicks <= 10) {
				kingExpandDamageReady = false;
				damageKingExpandTargets();
			}
			if (expandTicks - 1 <= 0) {
				startBossResting();
			}
		}
		// 冲刺技能：蓄力 -> 释放 -> 命中检测。
		int dashTicks = getKingDashTicks();
		if (dashTicks > 0) {
			setKingDashTicks(dashTicks - 1);
			spawnKingDashTrail();
			if (kingDashChargeTicks > 0) {
				kingDashChargeTicks--;
				if (kingDashChargeTicks == 0 && kingDashTarget != null && kingDashTarget.isAlive()) {
					releaseKingDash(kingDashTarget);
				}
			} else if (dashTicks < KING_DASH_POSE_TICKS - KING_DASH_CHARGE_TICKS) {
				damageKingDashTargets();
			}
		} else {
			kingDashTarget = null;
			kingDashChargeTicks = 0;
		}
	}

	/**
	 * Boss 弹跳技能倒计时与休息阶段处理。
	 */
	private void tickBossBounceAndRest() {
		if (!isCandyBoss() || level().isClientSide) {
			return;
		}
		// 休息阶段：完全静止并滴水粒子。
		int restTicks = getBossRestingTicks();
		if (restTicks > 0) {
			setBossRestingTicks(restTicks - 1);
			setDeltaMovement(0.0D, Math.min(0.0D, getDeltaMovement().y), 0.0D);
			getNavigation().stop();
			if (level() instanceof ServerLevel serverLevel && tickCount % 4 == 0) {
				serverLevel.sendParticles(ParticleTypes.FALLING_WATER, getX(), getY() + getBbHeight() * 0.7D, getZ(),
						10, getBbWidth() * 0.32D, getBbHeight() * 0.22D, getBbWidth() * 0.32D, 0.035D);
			}
		}
		if (bossBounceCooldown > 0) {
			bossBounceCooldown--;
		}
		int bounceTicks = getBossBounceTicks();
		if (bounceTicks <= 0) {
			return;
		}
		setBossBounceTicks(bounceTicks - 1);
		setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
		getNavigation().stop();
		// 蓄力完成后结算一次弹跳伤害。
		if (bossBounceDamageReady && bounceTicks <= BOSS_BOUNCE_POSE_TICKS - BOSS_BOUNCE_CHARGE_TICKS) {
			bossBounceDamageReady = false;
			damageBossBounceTargets();
		}
		if (bounceTicks - 1 <= 0) {
			startBossResting();
		}
	}

	/**
	 * 让 Boss 朝目标起跳砸地。
	 *
	 * <p>水平速度由类型决定，竖直速度在水里更大。
	 */
	private void launchBossSlamAt(LivingEntity target) {
		getLookControl().setLookAt(target);
		double horizontalPower = bossSlamHorizontalPower();
		double yPower = isInWater() ? 4.0D : 1.5D;
		Vec3 horizontal = chooseBossSlamVector(target, horizontalPower);
		setJumping(true);
		setDeltaMovement(horizontal.x, yPower, horizontal.z);
		hasImpulse = true;
		bossSlamAttackActive = true;
		bossSlamDamageReady = true;
		bossWasOnGround = false;
		setBossSlamTicks(BOSS_SLAM_POSE_TICKS);
		performBossSlamSpecials(target);
		playSound(getJumpSound(), getSoundVolume(), ((random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
	}

	/**
	 * 选择砸地方向。
	 *
	 * <p>默认朝目标直冲，若被阻挡或没有视线，则尝试左右偏转（类似“绕行”）。
	 * 偏转角度从 ±24° 到 ±62°，选一个最接近目标且路径可通行的。
	 */
	private Vec3 chooseBossSlamVector(LivingEntity target, double horizontalPower) {
		double dx = target.getX() - getX();
		double dz = target.getZ() - getZ();
		double distance = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
		Vec3 direct = new Vec3(dx / distance, 0.0D, dz / distance);
		boolean shouldStrafe = bossSlamBlockedTicks >= 3 || !hasLineOfSight(target);
		if (!shouldStrafe || isBossSlamPathOpen(direct, horizontalPower)) {
			return direct.scale(horizontalPower);
		}

		double[] angles = {24.0D, -24.0D, 42.0D, -42.0D, 62.0D, -62.0D};
		Vec3 best = direct;
		double bestScore = -999.0D;
		for (double angle : angles) {
			double signedAngle = angle * bossSlamStrafeSide;
			Vec3 candidate = rotateHorizontal(direct, signedAngle);
			if (!isBossSlamPathOpen(candidate, horizontalPower)) {
				continue;
			}
			// 评分：越接近目标越好，小角度有微弱加成。
			Vec3 projected = position().add(candidate.scale(Math.max(2.0D, distance * 0.55D)));
			double targetScore = -projected.distanceToSqr(target.position());
			double sideScore = Math.abs(angle) < 45.0D ? 0.35D : 0.0D;
			double score = targetScore + sideScore;
			if (score > bestScore) {
				best = candidate;
				bestScore = score;
			}
		}
		// 下一次尝试换另一侧，避免卡在同一个角落。
		bossSlamStrafeSide *= -1;
		return best.scale(horizontalPower);
	}

	/**
	 * 检查砸地方向是否可通行：沿方向采样几个点，看碰撞箱是否会被挡住。
	 */
	private boolean isBossSlamPathOpen(Vec3 direction, double horizontalPower) {
		if (direction.lengthSqr() < 1.0E-4D) {
			return false;
		}
		Vec3 normalized = direction.normalize();
		double step = Math.max(0.45D, horizontalPower * 1.15D);
		for (int i = 1; i <= 4; i++) {
			Vec3 offset = normalized.scale(step * i);
			if (!level().noCollision(this, getBoundingBox().move(offset.x, 0.35D + i * 0.12D, offset.z))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 绕 Y 轴把水平向量旋转指定角度，返回归一化结果。
	 */
	private static Vec3 rotateHorizontal(Vec3 direction, double degrees) {
		double radians = Math.toRadians(degrees);
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);
		return new Vec3(direction.x * cos - direction.z * sin, 0.0D, direction.x * sin + direction.z * cos).normalize();
	}

	/**
	 * 预计算所有方向的单位向量。
	 */
	private static Vec3[] createDirectionVectors() {
		Vec3[] vectors = new Vec3[DIRECTIONS.length];
		for (Direction direction : DIRECTIONS) {
			vectors[direction.ordinal()] = Vec3.atLowerCornerOf(direction.getNormal());
		}
		return vectors;
	}

	/**
	 * 通过方向取预计算的单位向量。
	 */
	private static Vec3 directionVector(Direction direction) {
		return DIRECTION_VECTORS[direction.ordinal()];
	}

	/**
	 * Boss 远程攻击：目标较高或较远且视线可见时，发射一颗 GummyBall。
	 */
	private void tickBossRangedAttack(LivingEntity target) {
		if (!isBossAwake() || !(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		if (bossRangedCooldown > 0) {
			bossRangedCooldown--;
			return;
		}
		double distanceSqr = distanceToSqr(target);
		boolean highOrFar = target.getY() - getY() > 4.0D || distanceSqr > 144.0D;
		if (!highOrFar || !hasLineOfSight(target)) {
			return;
		}
		shootBossJellyBall(serverLevel, target);
		// 女王射击最频繁，国王最慢。
		bossRangedCooldown = isJellyQueen() ? 48 + random.nextInt(28) : isKingSlime() ? 64 + random.nextInt(36) : 58 + random.nextInt(32);
	}

	/**
	 * 发射一颗 Boss 专属果冻球。
	 *
	 * <p>射击时朝目标加上一点弧度补偿（{@code dy + distance * 0.08}），避免近距离完全平飞。
	 */
	private void shootBossJellyBall(ServerLevel level, LivingEntity target) {
		GummyBallEntity ball = new GummyBallEntity(level, this, 5);
		ball.setVisualVariant(bossJellyBallVisual());
		ball.setBonusDamage(5.0F);
		ball.setPos(getX(), getEyeY() - 0.1D, getZ());
		double dx = target.getX() - getX();
		double dy = target.getEyeY() - ball.getY();
		double dz = target.getZ() - getZ();
		double distance = Math.sqrt(dx * dx + dz * dz);
		ball.shoot(dx, dy + distance * 0.08D, dz, 1.25F, 6.0F);
		level.addFreshEntity(ball);
		playSound(SoundEvents.SNOWBALL_THROW, 1.0F, 0.85F + random.nextFloat() * 0.25F);
	}

	/**
	 * 根据 Boss 类型返回果冻球视觉变体 ID，用于客户端渲染颜色/图标。
	 */
	private int bossJellyBallVisual() {
		if (isPezJelly()) {
			return GummyBallEntity.PEZ_JELLY_VISUAL;
		}
		if (isKingSlime()) {
			return GummyBallEntity.CARAMEL_KING_JELLY_VISUAL;
		}
		if (isJellyQueen()) {
			return GummyBallEntity.STRAWBERRY_QUEEN_JELLY_VISUAL;
		}
		if (isYellowJelly()) {
			return GummyBallEntity.LEMON_JELLY_VISUAL;
		}
		if (isRedJelly()) {
			return GummyBallEntity.RASPBERRY_JELLY_VISUAL;
		}
		if (isTornadoJelly()) {
			return GummyBallEntity.MINT_JELLY_VISUAL;
		}
		return 0;
	}

	/**
	 * 国王/女王的随机特殊技能判定。
	 *
	 * <p>三个技能互斥（一次只触发一个），各自有距离/随机数条件：
	 * <ol>
	 *     <li>膨胀：近距离随机触发</li>
	 *     <li>弹跳：中近距离随机触发</li>
	 *     <li>冲刺：远距离随机触发</li>
	 * </ol>
	 */
	private void tickKingSlimeSpecials(LivingEntity target) {
		if (!(isKingSlime() || isJellyQueen())) {
			return;
		}
		if (getKingExpandTicks() == 0 && kingExpandCooldown <= 0 && onGround() && Math.abs(getDeltaMovement().y) < 0.05D && distanceToSqr(target) < 196.0D && random.nextInt(28) == 0) {
			startKingExpandAttack();
			return;
		}
		if (getBossBounceTicks() == 0 && bossBounceCooldown <= 0 && onGround() && distanceToSqr(target) < 81.0D && random.nextInt(isJellyQueen() ? 48 : 36) == 0) {
			startBossBounceAttack();
			return;
		}
		if (getKingDashTicks() == 0 && kingDashCooldown <= 0 && distanceToSqr(target) > 49.0D && random.nextInt(28) == 0) {
			startKingDashAttack(target);
		}
	}

	/**
	 * 开始弹跳技能：立即锁定移动，等待 {@link #BOSS_BOUNCE_CHARGE_TICKS} 后结算伤害。
	 */
	private void startBossBounceAttack() {
		setBossBounceTicks(BOSS_BOUNCE_POSE_TICKS);
		bossBounceCooldown = isJellyQueen() ? 210 + random.nextInt(100) : 170 + random.nextInt(90);
		bossBounceDamageReady = true;
		setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
		getNavigation().stop();
		playSound(SoundEvents.SLIME_SQUISH, 1.35F, 0.5F);
	}

	/**
	 * 弹跳伤害结算：以自身为中心，半径与尺寸/类型相关，命中后带击退。
	 */
	private void damageBossBounceTargets() {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		double radius = Math.max(isJellyQueen() ? 4.25D : 5.25D, getSize() * 0.58D);
		for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius, 1.8D, radius), this::canBossTarget)) {
			if (target == this) {
				continue;
			}
			if (target.hurt(damageSources().mobAttack(this), isJellyQueen() ? 4.0F : 8.0F)) {
				double dx = target.getX() - getX();
				double dz = target.getZ() - getZ();
				double length = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
				double push = isJellyQueen() ? 1.35D : 2.0D;
				target.push(dx / length * push, isJellyQueen() ? 0.52D : 0.8D, dz / length * push);
				target.hurtMarked = true;
			}
		}
		serverLevel.sendParticles(jellyLandingParticle(), getX(), getY() + 0.2D, getZ(), 42, radius * 0.22D, 0.22D, radius * 0.22D, 0.09D);
		playSound(SoundEvents.SLIME_ATTACK, 1.55F, 0.62F);
	}

	/**
	 * 进入技能后休息状态：完全静止 {@link #BOSS_REST_TICKS} tick。
	 */
	private void startBossResting() {
		setBossRestingTicks(BOSS_REST_TICKS);
		setDeltaMovement(0.0D, Math.min(0.0D, getDeltaMovement().y), 0.0D);
		getNavigation().stop();
	}

	/**
	 * 开始膨胀技能。冷却根据类型略有差异。
	 */
	private void startKingExpandAttack() {
		setKingExpandTicks(KING_EXPAND_POSE_TICKS);
		kingExpandCooldown = isPezJelly() ? 180 + random.nextInt(80) : isJellyQueen() ? 190 + random.nextInt(90) : 150 + random.nextInt(80);
		kingExpandDamageReady = true;
		setDeltaMovement(0.0D, 0.0D, 0.0D);
		getNavigation().stop();
		playSound(SoundEvents.SLIME_SQUISH, 1.2F, 0.55F);
	}

	/**
	 * 从高处落地后，按下落距离有概率额外触发冲刺。
	 * 下落越高，触发概率越大（最低 10%，最高 50%）。
	 */
	private void tryStartHeightBonusDash() {
		LivingEntity target = findBossAttackTarget();
		if (target == null || !(isKingSlime() || isJellyQueen()) || getKingDashTicks() > 0 || kingDashCooldown > 0) {
			return;
		}
		float fall = Math.max(bossLastFallDistance, fallDistance);
		if (fall < 5.0F) {
			return;
		}
		int chance = Mth.clamp(10 - (int) (fall * 0.9F), 2, 10);
		if (random.nextInt(chance) == 0) {
			startKingDashAttack(target);
		}
	}

	/**
	 * 膨胀伤害结算。半径随类型变化：Pez 最小，国王最大。
	 */
	private void damageKingExpandTargets() {
		double radius = Math.max(isPezJelly() ? 5.25D : isJellyQueen() ? 7.5D : 9.0D, getSize() * 1.05D);
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius, 3.2D, radius), this::canBossTarget)) {
			if (target == this) {
				continue;
			}
			if (target.hurt(damageSources().mobAttack(this), isPezJelly() ? 5.5F : isJellyQueen() ? 4.5F : 9.0F)) {
				double dx = target.getX() - getX();
				double dz = target.getZ() - getZ();
				double length = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
				double push = isPezJelly() ? 1.65D : isJellyQueen() ? 1.8D : 2.7D;
				target.push(dx / length * push, isPezJelly() ? 0.62D : isJellyQueen() ? 0.7D : 1.05D, dz / length * push);
				target.hurtMarked = true;
			}
		}
		serverLevel.sendParticles(jellyLandingParticle(), getX(), getY() + 0.2D, getZ(), 76, radius * 0.33D, 0.38D, radius * 0.33D, 0.12D);
		playSound(SoundEvents.SLIME_ATTACK, 1.9F, 0.54F);
	}

	/**
	 * 开始冲刺技能：先蓄力，然后锁定目标。
	 */
	private void startKingDashAttack(LivingEntity target) {
		kingDashTarget = target;
		kingDashChargeTicks = KING_DASH_CHARGE_TICKS;
		kingDashCooldown = isJellyQueen() ? 220 + random.nextInt(100) : 170 + random.nextInt(90);
		setKingDashTicks(KING_DASH_POSE_TICKS);
		setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
		getNavigation().stop();
		getLookControl().setLookAt(target);
		playSound(SoundEvents.SLIME_SQUISH, 1.3F, 0.72F);
	}

	/**
	 * 蓄力完成，释放冲刺。速度朝目标方向，并叠加少量当前动量。
	 */
	private void releaseKingDash(LivingEntity target) {
		Vec3 desired = target.position().subtract(position());
		Vec3 current = getDeltaMovement();
		if (desired.lengthSqr() < 1.0E-4D) {
			desired = getLookAngle();
		}
		Vec3 corrected = desired.normalize().scale(isJellyQueen() ? 1.65D : 2.15D).add(current.scale(0.15D));
		setDeltaMovement(corrected.x, Math.max(0.08D, corrected.y * 0.15D + 0.28D), corrected.z);
		hasImpulse = true;
		playSound(SoundEvents.SLIME_JUMP, 1.45F, 0.82F);
	}

	/**
	 * 冲刺命中检测：命中后带击退，并把冲刺动画缩短（避免连续命中同一目标）。
	 */
	private void damageKingDashTargets() {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.8D), this::canBossTarget)) {
			if (target == this) {
				continue;
			}
			if (target.hurt(damageSources().mobAttack(this), isJellyQueen() ? 7.0F : 14.0F)) {
				Vec3 velocity = getDeltaMovement();
				double horizontal = Math.max(0.1D, velocity.horizontalDistance());
				double push = isJellyQueen() ? 0.72D : 1.1D;
				target.push(velocity.x / horizontal * push, isJellyQueen() ? 0.35D : 0.55D, velocity.z / horizontal * push);
				target.hurtMarked = true;
				setKingDashTicks(Math.min(getKingDashTicks(), 10));
			}
		}
	}

	/**
	 * 冲刺时在身后留下果冻球粒子拖尾。
	 */
	private void spawnKingDashTrail() {
		if (!(level() instanceof ServerLevel serverLevel) || kingDashChargeTicks > 0) {
			return;
		}
		serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new net.minecraft.world.item.ItemStack(isJellyQueen() ? CCItems.STRAWBERRY_QUEEN_JELLY_BALL.get() : CCItems.CARAMEL_KING_JELLY_BALL.get())),
				getX() - getDeltaMovement().x * 0.7D, getY() + getBbHeight() * 0.45D, getZ() - getDeltaMovement().z * 0.7D,
				4, getBbWidth() * 0.2D, getBbHeight() * 0.15D, getBbWidth() * 0.2D, 0.03D);
	}

	/**
	 * Pez 滚轮触发条件：连续砸地 5 次后开始有概率触发，之后概率随次数提高。
	 */
	private void tickPezRollTrigger(LivingEntity target) {
		if (!isPezJelly()) {
			return;
		}
		if (pezRollCooldown > 0) {
			pezRollCooldown--;
		}
		if (pezSlamCount >= 5 && pezRollCooldown <= 0 && getPezRollTicks() == 0) {
			int chance = Math.max(1, 11 - pezSlamCount);
			if (pezSlamCount < 10 && random.nextInt(chance) != 0) {
				return;
			}
			startPezRollSkill(target);
			pezSlamCount = 0;
		}
	}

	/**
	 * Pez 的膨胀技能触发（比国王更保守）。
	 */
	private void tickPezExpandSpecial(LivingEntity target) {
		if (!isPezJelly() || getPezRollTicks() > 0 || getKingExpandTicks() > 0 || kingExpandCooldown > 0 || !onGround() || Math.abs(getDeltaMovement().y) >= 0.05D) {
			return;
		}
		if (distanceToSqr(target) < 81.0D && random.nextInt(42) == 0) {
			startKingExpandAttack();
		}
	}

	/**
	 * Pez 滚轮技能状态机。
	 *
	 * <p>根据已经过 tick 数推进阶段：
	 * <ol>
	 *     <li>{@code elapsed < PEZ_ROLLING_TICKS}：滚动阶段（开放空间/封闭空间走不同逻辑）</li>
	 *     <li>接下来 {@code PEZ_ROLL_ATTACH_WAIT_TICKS}：贴墙等待</li>
	 *     <li>接下来 {@code PEZ_ROLL_ATTACK_TICKS}：释放攻击并结算伤害</li>
	 *     <li>最后 {@code PEZ_ROLL_REST_TICKS}：休息</li>
	 * </ol>
	 */
	private void tickPezRollSkill() {
		if (!isPezJelly() || level().isClientSide) {
			return;
		}
		int ticks = getPezRollTicks();
		if (ticks <= 0) {
			setNoGravity(false);
			return;
		}
		tickPezAttachTransition();
		setPezRollTicks(ticks - 1);
		int elapsed = PEZ_ROLL_TOTAL_TICKS - ticks;
		LivingEntity target = pezRollTarget != null && pezRollTarget.isAlive() ? pezRollTarget : findBossAttackTarget();
		if (target == null) {
			finishPezRollSkill();
			return;
		}
		pezRollTarget = target;

		// 开放空间：直接朝目标滚动。
		if (pezOpenGroundRoll) {
			if (elapsed < PEZ_ROLLING_TICKS) {
				rollPezTowardTarget(target, elapsed);
			} else {
				finishPezRollSkill();
			}
			return;
		}

		// 封闭空间：沿墙壁滚动，贴墙，再攻击。
		if (elapsed < PEZ_ROLLING_TICKS) {
			rollPezThroughRoom(target, elapsed);
		} else if (elapsed < PEZ_ROLLING_TICKS + PEZ_ROLL_ATTACH_WAIT_TICKS) {
			stickPezToFace(target);
		} else if (elapsed < PEZ_ROLLING_TICKS + PEZ_ROLL_ATTACH_WAIT_TICKS + PEZ_ROLL_ATTACK_TICKS) {
			if (!pezRollAttackReleased) {
				releasePezRollAttack(target);
			}
			damagePezRollAttackTargets();
		} else {
			restPezAfterRoll();
		}

		if (ticks - 1 <= 0) {
			finishPezRollSkill();
		}
	}

	/**
	 * 开始 Pez 滚轮技能。
	 *
	 * <p>检测是否在封闭空间，决定走“开放滚动”还是“贴墙滚动”。
	 */
	private void startPezRollSkill(LivingEntity target) {
		setPezRollTicks(PEZ_ROLL_TOTAL_TICKS);
		setPezAttachFace(Direction.UP);
		Vec3 initialDirection = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
		setPezRollDirection(initialDirection.lengthSqr() > 1.0E-4D
				? Direction.getNearest(initialDirection.x, 0.0D, initialDirection.z)
				: Direction.NORTH);
		setPezRollSteps(0);
		setPezRollDistance(0.0F);
		pezRollTangent = Vec3.ZERO;
		pezRollTarget = target;
		pezRollAttackReleased = false;
		pezOpenGroundRoll = !isPezInEnclosedRollSpace();
		pezOpenRollHitTargets.clear();
		pezRollCooldown = 240;
		bossSlamAttackActive = false;
		bossSlamDamageReady = false;
		getNavigation().stop();
		playSound(SoundEvents.SLIME_SQUISH, 1.4F, 0.62F);
	}

	/**
	 * 判断 Pez 是否处于封闭空间（三面有墙 + 有天花板）。
	 * 用于决定滚轮模式。
	 */
	private boolean isPezInEnclosedRollSpace() {
		Vec3 center = position().add(0.0D, getBbHeight() * 0.55D, 0.0D);
		int walls = 0;
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (hasPezRollBoundary(center, direction)) {
				walls++;
			}
		}
		Vec3 ceilingProbe = position().add(0.0D, getBbHeight() + 0.1D, 0.0D);
		return walls >= 3 && hasPezRollBoundary(ceilingProbe, Direction.UP);
	}

	/**
	 * 从起点向指定方向做碰撞射线检测，看是否有实体方块阻挡。
	 */
	private boolean hasPezRollBoundary(Vec3 start, Direction direction) {
		Vec3 normal = Vec3.atLowerCornerOf(direction.getNormal());
		HitResult hit = level().clip(new ClipContext(
				start,
				start.add(normal.scale(PEZ_ENCLOSURE_PROBE_DISTANCE)),
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				this
		));
		return hit.getType() != HitResult.Type.MISS;
	}

	/**
	 * 开放空间滚动：直接朝目标推进，碰到障碍可小幅跳跃。
	 */
	private void rollPezTowardTarget(LivingEntity target, int elapsed) {
		setNoGravity(false);
		getNavigation().stop();
		Vec3 previousPosition = position();
		// 每隔一段 tick 重新瞄准，或方向丢失时重新计算。
		if (elapsed % PEZ_OPEN_ROLL_DIRECTION_TICKS == 0 || pezRollTangent.lengthSqr() < 1.0E-4D) {
			Vec3 toTarget = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
			if (toTarget.lengthSqr() < 1.0E-4D) {
				toTarget = getLookAngle().multiply(1.0D, 0.0D, 1.0D);
			}
			pezRollTangent = toTarget.lengthSqr() > 1.0E-4D ? toTarget.normalize() : Vec3.ZERO;
			pezOpenRollHitTargets.clear();
		}
		Vec3 rollDirection = pezRollTangent;
		double speed = Mth.clamp(pezRollMatchedSpeed() * 0.78D, 0.62D, 1.05D)
				* pezRollSpeedEnvelope(elapsed);
		double vertical = getDeltaMovement().y;
		// 碰到水平障碍且在地面时，给一点弹跳帮助越过小台阶。
		if (horizontalCollision && onGround()) {
			vertical = 0.36D;
		}
		Vec3 movement = rollDirection.scale(speed).add(0.0D, vertical, 0.0D);
		move(MoverType.SELF, movement);
		setDeltaMovement(rollDirection.scale(speed * 0.82D).add(0.0D, vertical, 0.0D));
		hasImpulse = true;
		addPezRollDistance(previousPosition.distanceTo(position()));
		alignPezBodyToRoll(rollDirection);

		if (rollDirection.lengthSqr() > 1.0E-4D) {
			Direction visualDirection = Direction.getNearest(rollDirection.x, 0.0D, rollDirection.z);
			setPezAttachFace(Direction.UP);
			setPezRollDirection(visualDirection);
		}
		damagePezOpenRollTargets(rollDirection, previousPosition);
		if (tickCount % 3 == 0) {
			spawnPezRollBreakParticles(rollDirection.scale(-1.0D));
		}
	}

	/**
	 * 开放空间滚动时的接触伤害。同一次滚动里每个目标只伤害一次。
	 */
	private void damagePezOpenRollTargets(Vec3 rollDirection, Vec3 previousPosition) {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		// 用本帧和上一帧碰撞箱的并集做“扫掠”检测，避免高速穿模。
		AABB sweptBox = getBoundingBox().minmax(getBoundingBox().move(previousPosition.subtract(position()))).inflate(0.55D);
		for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, sweptBox, this::canBossTarget)) {
			if (target == this || !pezOpenRollHitTargets.add(target.getId())) {
				continue;
			}
			if (target.hurt(damageSources().mobAttack(this), pezSourceContactDamage())) {
				Vec3 push = rollDirection.lengthSqr() > 1.0E-4D ? rollDirection.normalize() : target.position().subtract(position()).normalize();
				target.push(push.x * 0.9D, 0.3D, push.z * 0.9D);
				target.hurtMarked = true;
				playSound(SoundEvents.SLIME_ATTACK, 1.35F, 0.58F);
			}
		}
	}

	/**
	 * 结束 Pez 滚轮技能，清理所有状态。
	 */
	private void finishPezRollSkill() {
		setPezRollTicks(0);
		setNoGravity(false);
		setPezAttachTransitionTicks(0);
		pezRollAttackReleased = false;
		pezOpenGroundRoll = false;
		pezOpenRollHitTargets.clear();
		pezRollTarget = null;
		pezRollCooldown = 120;
	}

	/**
	 * 调试用：强制 Pez 立即开始滚轮技能。
	 */
	public boolean debugStartPezRoll(LivingEntity target) {
		if (!isPezJelly() || level().isClientSide || target == null || !target.isAlive()) {
			return false;
		}
		setBossAwake(true);
		setTarget(target);
		startPezRollSkill(target);
		return true;
	}

	/**
	 * 封闭空间滚动：沿墙壁/天花板滚动，遇到转角切面继续。
	 */
	private void rollPezThroughRoom(LivingEntity target, int elapsed) {
		Direction face = getPezAttachFace();
		// 如果当前贴面不再有效，重新选一个。
		if (face == null || !hasAttachSurface(face)) {
			face = choosePezRollFace();
			setPezAttachFace(face);
			pezRollTangent = Vec3.ZERO;
		}

		Direction rollDirection = getPezRollDirection();
		if (!isPezRollDirectionValid(face, rollDirection)) {
			Direction recoveryDirection = choosePezOpenDirection(face, Vec3.ZERO);
			if (recoveryDirection == null) {
				setDeltaMovement(Vec3.ZERO);
				return;
			}
			rollDirection = recoveryDirection;
			applyPezRollDirection(rollDirection);
		} else if (pezRollTangent.lengthSqr() < 1.0E-4D) {
			applyPezRollDirection(rollDirection);
		}

		setNoGravity(face != Direction.UP);
		double speed = pezRollMatchedSpeed() * pezRollSpeedEnvelope(elapsed);
		Vec3 beforeRollPosition = position();
		Vec3 tangent = directionVector(rollDirection);
		Vec3 segmentStart = position();
		movePezAlongSurface(face, tangent, speed);
		double movedDistance = segmentStart.distanceTo(position());
		double forwardDistance = Math.max(0.0D, position().subtract(segmentStart).dot(tangent));
		double remainingDistance = Math.max(0.0D, speed - forwardDistance);
		Vec3 finalTangent = tangent;

		// 如果本 tick 位移没有被完全用掉（比如撞到墙角），尝试沿转角继续。
		if (speed > 0.05D && remainingDistance > Math.max(0.025D, speed * 0.08D)) {
			Direction nextFace = findPezCornerFace(face, rollDirection);
			if (nextFace != null) {
				Direction continuationDirection = face;
				face = nextFace;
				setPezAttachFace(face);
				applyPezRollDirection(continuationDirection);
				finalTangent = directionVector(continuationDirection);
				segmentStart = position();
				movePezAlongSurface(face, finalTangent, remainingDistance);
				movedDistance += segmentStart.distanceTo(position());
			} else {
				Direction escape = choosePezOpenDirection(face, tangent);
				if (escape != null && escape != rollDirection) {
					applyPezRollDirection(escape);
					finalTangent = directionVector(escape);
				}
			}
		}

		setNoGravity(face != Direction.UP);
		setDeltaMovement(finalTangent.scale(movedDistance));
		hasImpulse = true;
		addPezRollDistance(movedDistance);
		if (finalTangent.lengthSqr() > 1.0E-4D) {
			alignPezBodyToRoll(finalTangent);
		}
		damagePezRollBrushTargets(finalTangent, beforeRollPosition);
	}

	/**
	 * 沿指定贴面方向移动，稍微向内压（-0.045）防止漂浮离开墙面。
	 */
	private void movePezAlongSurface(Direction face, Vec3 tangent, double distance) {
		Vec3 surfaceNormal = directionVector(face);
		move(MoverType.SELF, tangent.scale(distance).add(surfaceNormal.scale(-0.045D)));
	}

	/**
	 * 检查当前滚动方向是否撞到一个面，若是，返回该面作为新的贴面。
	 */
	@Nullable
	private Direction findPezCornerFace(Direction currentFace, Direction rollDirection) {
		Direction blockingFace = rollDirection.getOpposite();
		if (blockingFace != currentFace && hasAttachSurface(blockingFace)) {
			return blockingFace;
		}
		return null;
	}

	/**
	 * 计算与滚动动画匹配的速度：让滚动周期等于动画时长。
	 */
	private double pezRollMatchedSpeed() {
		double cycleTicks = PEZ_ROLL_ANIMATION_SECONDS * 20.0D;
		double circumference = Math.max(0.25D, getBbWidth()) * Math.PI;
		return circumference / cycleTicks;
	}

	/**
	 * 滚动速度包络：起步加速，结尾减速（使用 smootherStep 平滑）。
	 */
	private static double pezRollSpeedEnvelope(int elapsed) {
		float acceleration = smootherStep((elapsed + 1.0F) / PEZ_ROLL_ACCELERATION_TICKS);
		float remaining = PEZ_ROLLING_TICKS - elapsed;
		float deceleration = smootherStep(remaining / PEZ_ROLL_DECELERATION_TICKS);
		return acceleration * deceleration;
	}

	/**
	 * 让身体朝向滚动方向，视觉上像在滚动。
	 */
	private void alignPezBodyToRoll(Vec3 direction) {
		Vec3 horizontal = direction.multiply(1.0D, 0.0D, 1.0D);
		if (horizontal.lengthSqr() < 1.0E-4D) {
			return;
		}
		float yaw = (float) (Mth.atan2(horizontal.z, horizontal.x) * Mth.RAD_TO_DEG) - 90.0F;
		setYRot(yaw);
		setYHeadRot(yaw);
		yBodyRot = yaw;
	}

	/**
	 * 检查在指定贴面上，沿切向是否可以滑动（前方无障碍，且前进后仍然贴着面）。
	 */
	private boolean canPezSlide(Direction face, Vec3 tangent) {
		if (tangent.lengthSqr() < 1.0E-4D) {
			return false;
		}
		Vec3 normalized = tangent.normalize();
		// 切向必须与贴面法线垂直。
		if (normalized.dot(directionVector(face)) != 0.0D) {
			return false;
		}
		Vec3 step = normalized.scale(0.18D);
		return level().noCollision(this, getBoundingBox().move(step))
				&& hasAttachSurfaceAt(face, position().add(step));
	}

	/**
	 * 滚动方向是否合法：不能与贴面同轴（否则等于撞向墙面）。
	 */
	private boolean isPezRollDirectionValid(Direction face, Direction direction) {
		return direction != null && direction.getAxis() != face.getAxis();
	}

	/**
	 * 应用新的滚动方向，并累计步数。
	 */
	private void applyPezRollDirection(Direction direction) {
		setPezRollDirection(direction);
		pezRollTangent = directionVector(direction);
		setPezRollSteps(getPezRollSteps() + 1);
	}

	/**
	 * 找一个可贴附的面（优先地面，其次水平面，最后天花板）。
	 */
	private Direction findPezAttachFace() {
		Direction[] preferred = {Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP};
		for (Direction direction : preferred) {
			if (hasAttachSurface(direction)) {
				return direction;
			}
		}
		return Direction.UP;
	}

	/**
	 * 选一个适合滚动的贴面（当前贴面优先，否则按偏好顺序）。
	 */
	private Direction choosePezRollFace() {
		Direction current = getPezAttachFace();
		if (current != null && hasAttachSurface(current)) {
			return current;
		}
		Direction[] preferred = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP};
		for (Direction candidate : preferred) {
			if (hasAttachSurface(candidate)) {
				return candidate;
			}
		}
		return findPezAttachFace();
	}

	/**
	 * 选择攻击阶段贴附面：优先天花板（能倒挂），其次水平面，最后地面。
	 * 加入随机分数避免每次都完全一样。
	 */
	private Direction choosePezStrikeAttachFace() {
		Direction[] preferred = {Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP};
		Direction best = Direction.UP;
		double bestScore = -Double.MAX_VALUE;
		for (Direction candidate : preferred) {
			if (!hasAttachSurface(candidate)) {
				continue;
			}
			double score = random.nextDouble();
			if (candidate == Direction.DOWN) {
				score += 9.0D;
			} else if (candidate != Direction.UP) {
				score += 6.0D;
			}
			if (candidate == getPezAttachFace()) {
				score += 1.5D;
			}
			if (score > bestScore) {
				best = candidate;
				bestScore = score;
			}
		}
		return best;
	}

	/**
	 * 是否存在可贴附的表面。地面特判：只要 onGround 就算有。
	 */
	private boolean hasAttachSurface(Direction face) {
		if (face == Direction.UP && onGround()) {
			return true;
		}
		return hasAttachSurfaceAt(face, position());
	}

	/**
	 * 在指定位置检测某个方向是否有可贴附表面（用略缩小并贴边的碰撞箱做无碰撞检测）。
	 */
	private boolean hasAttachSurfaceAt(Direction face, Vec3 center) {
		if (face == Direction.UP && onGround()) {
			return true;
		}
		Vec3 probe = Vec3.atLowerCornerOf(face.getOpposite().getNormal()).scale(0.08D);
		AABB box = getBoundingBox().move(center.subtract(position())).move(probe).deflate(0.05D);
		return !level().noCollision(this, box);
	}

	/**
	 * 在给定贴面上选一个可滑动的方向，尽量与之前的切向保持连续性。
	 */
	private Direction choosePezOpenDirection(Direction face, Vec3 previousTangent) {
		Direction best = null;
		double bestScore = -Double.MAX_VALUE;
		Vec3 previous = previousTangent.lengthSqr() > 1.0E-4D ? previousTangent.normalize() : Vec3.ZERO;
		for (Direction direction : DIRECTIONS) {
			if (direction.getAxis() == face.getAxis()) {
				continue;
			}
			Vec3 candidate = directionVector(direction);
			if (!canPezSlide(face, candidate)) {
				continue;
			}
			double score = random.nextDouble();
			if (previous.lengthSqr() > 1.0E-4D) {
				// 同向加分，反向重罚，垂直略有加分。
				double continuity = candidate.dot(previous);
				score += continuity > 0.5D ? 8.0D : continuity < -0.5D ? -12.0D : 2.0D;
			}
			if (score > bestScore) {
				best = direction;
				bestScore = score;
			}
		}
		return best;
	}

	/**
	 * 贴墙等待阶段：持续向墙面方向施加微小压力，保持贴附。
	 */
	private void stickPezToFace(LivingEntity target) {
		Direction face = getPezAttachFace();
		if (face == null || face == Direction.UP || !hasAttachSurface(face)) {
			face = choosePezStrikeAttachFace();
			setPezAttachFace(face);
		}
		setNoGravity(face != Direction.UP);
		Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
		setDeltaMovement(normal.scale(-0.035D));
		hasImpulse = true;
		getLookControl().setLookAt(target);
	}

	/**
	 * 释放滚轮攻击：朝目标冲刺，并从贴面方向获得一个额外冲量。
	 */
	private void releasePezRollAttack(LivingEntity target) {
		Direction face = getPezAttachFace();
		setNoGravity(false);
		Vec3 toTarget = target.getEyePosition().subtract(position());
		if (toTarget.lengthSqr() < 1.0E-4D) {
			toTarget = getLookAngle();
		}
		Vec3 faceKick = face == null ? Vec3.ZERO : Vec3.atLowerCornerOf(face.getNormal()).scale(0.38D);
		Vec3 dash = toTarget.normalize().scale(2.65D).add(faceKick);
		setDeltaMovement(dash);
		hasImpulse = true;
		pezRollAttackReleased = true;
		playSound(SoundEvents.SLIME_JUMP, 1.5F, 0.7F);
	}

	/**
	 * 滚轮攻击命中检测。
	 *
	 * <p>如果目标格挡，会提前结束攻击阶段，并给目标一个轻微位移。
	 */
	private void damagePezRollAttackTargets() {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.9D), this::canBossTarget)) {
			if (target == this) {
				continue;
			}
			if (target.isBlocking()) {
				Vec3 away = position().subtract(target.position());
				target.push(-away.x * 0.08D, 0.12D, -away.z * 0.08D);
				target.hurtMarked = true;
				setPezRollTicks(Math.min(getPezRollTicks(), PEZ_ROLL_REST_TICKS));
				continue;
			}
			if (target.hurt(damageSources().mobAttack(this), pezSourceContactDamage())) {
				Vec3 velocity = getDeltaMovement();
				double horizontal = Math.max(0.1D, velocity.horizontalDistance());
				target.push(velocity.x / horizontal * 1.35D, 0.58D, velocity.z / horizontal * 1.35D);
				target.hurtMarked = true;
				setPezRollTicks(Math.min(getPezRollTicks(), PEZ_ROLL_REST_TICKS));
			}
		}
	}

	/**
	 * 封闭空间滚动时的擦碰伤害。有 4 tick 冷却，避免同一目标被连续伤害。
	 */
	private void damagePezRollBrushTargets(Vec3 tangent, Vec3 previousPosition) {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		if (pezRollBrushDamageCooldown > 0) {
			pezRollBrushDamageCooldown--;
			return;
		}
		AABB sweptBox = getBoundingBox().minmax(getBoundingBox().move(previousPosition.subtract(position()))).inflate(0.35D);
		for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, sweptBox, this::canBossTarget)) {
			if (target == this) {
				continue;
			}
			Vec3 contact = target.position().subtract(position());
			if (contact.lengthSqr() < 1.0E-4D) {
				contact = tangent;
			}
			spawnPezRollBreakParticles(contact.normalize());
			if (target.hurt(damageSources().mobAttack(this), pezSourceContactDamage() * 0.5F)) {
				target.push(tangent.x * 0.65D, 0.22D, tangent.z * 0.65D);
				pezRollBrushDamageCooldown = 4;
			}
			target.hurtMarked = true;
		}
	}

	/**
	 * 在指定法线方向生成 Pez 果冻球碎屑粒子。
	 */
	private void spawnPezRollBreakParticles(Vec3 normal) {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		Vec3 direction = normal.lengthSqr() > 1.0E-4D ? normal.normalize() : Vec3.ZERO;
		serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new net.minecraft.world.item.ItemStack(CCItems.PEZ_JELLY_BALL.get())),
				getX() + direction.x * getBbWidth() * 0.55D,
				getY() + getBbHeight() * 0.5D + direction.y * getBbHeight() * 0.45D,
				getZ() + direction.z * getBbWidth() * 0.55D,
				12, 0.12D, 0.12D, 0.12D, 0.08D);
	}

	/**
	 * 滚轮结束后的休息阶段。
	 */
	private void restPezAfterRoll() {
		setNoGravity(false);
		setDeltaMovement(0.0D, Math.min(0.0D, getDeltaMovement().y), 0.0D);
		getNavigation().stop();
		if (level() instanceof ServerLevel serverLevel && tickCount % 4 == 0) {
			serverLevel.sendParticles(ParticleTypes.FALLING_WATER, getX(), getY() + getBbHeight() * 0.75D, getZ(),
					10, getBbWidth() * 0.35D, getBbHeight() * 0.25D, getBbWidth() * 0.35D, 0.04D);
		}
	}

	/**
	 * Pez 滚轮接触伤害基准值，等于尺寸（尺寸 10 时每次 10）。
	 */
	private float pezSourceContactDamage() {
		return getSize();
	}

	/**
	 * smootherStep：平滑插值函数，用于滚动速度包络，避免突兀的加速/减速。
	 */
	private static float smootherStep(float value) {
		float clamped = Mth.clamp(value, 0.0F, 1.0F);
		return clamped * clamped * clamped * (clamped * (clamped * 6.0F - 15.0F) + 10.0F);
	}

	/**
	 * 各 Boss 砸地的水平速度。
	 */
	private double bossSlamHorizontalPower() {
		if (isJellyQueen()) {
			// 蓝色模式砸地冲刺更远。
			return getJellyQueenMode() == JELLY_QUEEN_BLUE_MODE ? 0.82D : 0.58D;
		}
		if (isKingSlime()) {
			return 0.52D;
		}
		return 0.58D;
	}

	/**
	 * 砸地伤害结算。半径约 {@code 0.6 * size}，需要视线可见。
	 * 只要命中至少一个目标，就消耗本次伤害（避免一次砸地反复结算）。
	 */
	private int damageBossSlamTargets() {
		if (!bossSlamDamageReady || level().isClientSide) {
			return 0;
		}
		double radius = 0.6D * getSize();
		int hits = 0;
		for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius, 1.25D, radius), this::canBossTarget)) {
			if (target == this) {
				continue;
			}
			if (!hasLineOfSight(target) || distanceToSqr(target) >= radius * radius) {
				continue;
			}
			if (target.hurt(damageSources().mobAttack(this), bossSlamDamage())) {
				playSound(SoundEvents.SLIME_ATTACK, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
				hits++;
			}
		}
		if (hits > 0) {
			bossSlamDamageReady = false;
		}
		return hits;
	}

	/**
	 * 砸地伤害公式。
	 */
	private float bossSlamDamage() {
		int size = getSize();
		if (isJellyQueen()) {
			return size * 2.0F;
		}
		if (isKingSlime()) {
			return size * 2.5F;
		}
		return size;
	}

	/**
	 * 记录砸地组合：
	 * <ul>
	 *     <li>Pez：累计到 10 次砸地立即触发滚轮</li>
	 *     <li>国王/女王：连续多次命中 2 个以上目标后触发膨胀</li>
	 * </ul>
	 */
	private void trackBossSlamCombo(int hits) {
		if (isPezJelly()) {
			pezSlamCount++;
			if (pezSlamCount >= 10 && pezRollCooldown <= 0 && getPezRollTicks() == 0) {
				LivingEntity target = findBossAttackTarget();
				if (target != null) {
					startPezRollSkill(target);
				}
				pezSlamCount = 0;
			}
			return;
		}
		if (!(isKingSlime() || isJellyQueen())) {
			return;
		}
		if (hits >= 2) {
			bossMultiTargetSlamCount++;
		}
		if (bossMultiTargetSlamCount >= 3 && getKingExpandTicks() == 0 && kingExpandCooldown <= 0) {
			startKingExpandAttack();
			bossMultiTargetSlamCount = 0;
		}
	}

	/**
	 * 砸地落地特效。
	 * <ul>
	 *     <li>女王棕色模式：上下双重爆炸</li>
	 *     <li>国王：1/5 概率单次爆炸</li>
	 * </ul>
	 */
	private void triggerBossSlamLandingEffects() {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		if (isJellyQueen() && getJellyQueenMode() == JELLY_QUEEN_BROWN_MODE) {
			serverLevel.explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.NONE);
			serverLevel.explode(this, getX(), getY() + 2.0D, getZ(), 3.0F, Level.ExplosionInteraction.NONE);
		} else if (isKingSlime() && random.nextInt(5) == 0) {
			serverLevel.explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.NONE);
		}
	}

	/**
	 * 砸地时附带的召唤/减益效果。
	 * <ul>
	 *     <li>Pez：1/5 概率召唤龙卷风果冻</li>
	 *     <li>国王：1/3 概率召唤黄色果冻；1/10 概率给玩家缓慢</li>
	 * </ul>
	 */
	private void performBossSlamSpecials(LivingEntity target) {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		if (isPezJelly() && random.nextInt(5) == 0) {
			BasicCandySlimeEntity tornado = CCEntityTypes.TORNADO_JELLY.get().create(serverLevel);
			if (tornado != null) {
				tornado.setJellySummoner(this, target);
				tornado.moveTo(getX(), getY() + 0.5D, getZ(), random.nextFloat() * 360.0F, 0.0F);
				serverLevel.addFreshEntity(tornado);
			}
		} else if (isKingSlime()) {
			if (random.nextInt(3) == 0) {
				BasicCandySlimeEntity jelly = CCEntityTypes.YELLOW_JELLY.get().create(serverLevel);
				if (jelly != null) {
					jelly.setJellySummoner(this, target);
					jelly.moveTo(getX(), getY() + 0.5D, getZ(), random.nextFloat() * 360.0F, 0.0F);
					serverLevel.addFreshEntity(jelly);
				}
			}
			if (target instanceof Player player && random.nextInt(10) == 0) {
				player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 2), this);
			}
		}
	}

	/**
	 * Boss 对玩家的接触伤害。需要视线和距离满足条件。
	 */
	private boolean hurtPlayerWithLegacyBossContact(Player player) {
		int size = getSize();
		double range = 0.6D * size;
		if (!hasLineOfSight(player) || distanceToSqr(player) >= range * range) {
			return false;
		}
		float damage = isJellyQueen() ? size * 2.0F : isKingSlime() ? size * 2.5F : size;
		if (player.hurt(damageSources().mobAttack(this), damage)) {
			playSound(SoundEvents.SLIME_ATTACK, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
			return true;
		}
		return false;
	}

	/**
	 * 每 tick 检查当前目标是否已经与自身碰撞箱重叠，重叠则主动攻击。
	 * 这样即使没有 AI 路径也能造成接触伤害。
	 */
	private void tickRetaliationTarget() {
		LivingEntity target = getTarget();
		if (target == null) {
			return;
		}
		if (!canAttackTarget(target)) {
			setTarget(null);
			return;
		}
		AABB bounds = getBoundingBox();
		AABB targetBounds = target.getBoundingBox();
		double padding = 0.15D;
		if (bounds.minX - padding < targetBounds.maxX && bounds.maxX + padding > targetBounds.minX
				&& bounds.minY - padding < targetBounds.maxY && bounds.maxY + padding > targetBounds.minY
				&& bounds.minZ - padding < targetBounds.maxZ && bounds.maxZ + padding > targetBounds.minZ) {
			doHurtTarget(target);
		}
	}

	/**
	 * Boss 对实体的近战伤害包装：需要目标合法、有视线、在射程内。
	 */
	private boolean hurtLegacyBossTarget(Entity target) {
		int size = getSize();
		double range = 0.6D * size;
		if (!canAttackTarget(target) || !hasLineOfSight(target) || distanceToSqr(target) >= range * range) {
			return false;
		}
		float damage = isJellyQueen() ? size * 2.0F : isKingSlime() ? size * 2.5F : size;
		return hurtCandyTarget(target, damage);
	}

	/**
	 * 通用伤害工具：命中时播放果冻攻击音效。
	 */
	private boolean hurtCandyTarget(Entity target, float damage) {
		if (!canAttackTarget(target)) {
			return false;
		}
		if (target.hurt(damageSources().mobAttack(this), damage)) {
			playSound(SoundEvents.SLIME_ATTACK, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
			return true;
		}
		return false;
	}

	/**
	 * 设置报复目标（被攻击后反击）。
	 */
	private void setRetaliationTarget(LivingEntity attacker) {
		if (canRetaliateAgainst(attacker)) {
			setTarget(attacker);
			if (isCandyBoss()) {
				setBossAwake(true);
			}
		}
	}

	/**
	 * 被攻击时唤醒 Boss。
	 */
	private void activateBossFromDamage(LivingEntity attacker) {
		if (canBossWakeFrom(attacker)) {
			bossLostTargetTicks = BOSS_LOST_TARGET_TICKS;
			bossRetaliationTarget = attacker;
			setBossAwake(true);
			updateJellyQueenMode();
			if (CandyTargeting.canAttackEntity(attacker)) {
				setTarget(attacker);
				getNavigation().stop();
				setBaseValueIfChanged(getAttribute(Attributes.MOVEMENT_SPEED),
						isJellyQueen() ? 0.7D : isKingSlime() ? 0.45D : 0.38D);
			}
		}
	}

	/**
	 * 是否可攻击该目标：Boss 走 Boss 判定，普通果冻走报复判定。
	 */
	private boolean canAttackTarget(Entity target) {
		return isCandyBoss() ? canBossTarget(target) : canRetaliateAgainst(target);
	}

	/**
	 * Boss 可攻击目标判定：
	 * <ul>
	 *     <li>必须是活体</li>
	 *     <li>不能是自己的召唤盟友</li>
	 *     <li>其他糖果史莱姆只有在“报复目标”或“强制冲突”时才可攻击</li>
	 *     <li>必须通过 {@code CandyTargeting.canAttackEntity}</li>
	 * </ul>
	 */
	private boolean canBossTarget(Entity target) {
		return target instanceof LivingEntity
				&& target.isAlive()
				&& !(target instanceof BasicCandySlimeEntity slimeTarget && isSummonedAlly(slimeTarget))
				&& (!(target instanceof BasicCandySlimeEntity bossSlimeTarget) || isBossRetaliationTarget(bossSlimeTarget) || isForcedJellyConflict(this, bossSlimeTarget))
				&& CandyTargeting.canAttackEntity(target);
	}

	/**
	 * 该实体是否是当前 Boss 的报复目标。
	 */
	private boolean isBossRetaliationTarget(Entity target) {
		return bossRetaliationTarget != null && bossRetaliationTarget == target;
	}

	/**
	 * Boss 是否可被该来源唤醒。
	 */
	private boolean canBossWakeFrom(Entity source) {
		return source instanceof LivingEntity
				&& source.isAlive()
				&& !(source instanceof BasicCandySlimeEntity slimeSource && isSummonedAlly(slimeSource))
				&& (!(source instanceof BasicCandySlimeEntity wakeSlimeSource) || isCandyBoss() || isForcedJellyConflict(wakeSlimeSource, this));
	}

	/**
	 * 普通果冻是否可报复该目标。
	 */
	private boolean canRetaliateAgainst(Entity target) {
		return target instanceof LivingEntity
				&& target.isAlive()
				&& !(target instanceof BasicCandySlimeEntity slimeTarget && isSummonedAlly(slimeTarget))
				&& (!(target instanceof BasicCandySlimeEntity retaliationSlimeTarget) || isForcedJellyConflict(this, retaliationSlimeTarget))
				&& CandyTargeting.canAttackEntity(target);
	}

	/**
	 * 两个糖果史莱姆是否处于“强制冲突”（互相锁定），此时可以互相伤害。
	 */
	private static boolean isForcedJellyConflict(BasicCandySlimeEntity attacker, BasicCandySlimeEntity victim) {
		return attacker.getTarget() == victim || victim.getTarget() == attacker;
	}

	/**
	 * 休眠 Boss 是否应被非玩家生物攻击唤醒。
	 */
	private boolean shouldDormantBossWakeFromMobAttack(LivingEntity attacker) {
		return isCandyBoss()
				&& !isBossAwake()
				&& !(attacker instanceof Player)
				&& !isPlayerAlliedEntity(attacker)
				&& canBossWakeFrom(attacker)
				&& CandyTargeting.canAttackEntity(attacker);
	}

	/**
	 * 判断实体是否是玩家友方（如已驯服的宠物）。
	 */
	private static boolean isPlayerAlliedEntity(Entity entity) {
		if (entity instanceof TamableAnimal tamable && tamable.isTame() && tamable.getOwner() instanceof Player) {
			return true;
		}
		return false;
	}

	/**
	 * 寻找当前 Boss 应攻击的目标。
	 *
	 * <p>优先当前 target，其次 bossRetaliationTarget。目标需在范围内且合法。
	 */
	private LivingEntity findBossAttackTarget() {
		LivingEntity target = getTarget();
		if (target != null) {
			if (!canBossTarget(target)) {
				setTarget(null);
				return null;
			}
			if (distanceToSqr(target) <= BOSS_TARGET_RANGE * BOSS_TARGET_RANGE) {
				return target;
			}
		}
		LivingEntity retaliationTarget = bossRetaliationTarget;
		if (retaliationTarget != null) {
			if (!canBossTarget(retaliationTarget)) {
				bossRetaliationTarget = null;
			} else {
				if (distanceToSqr(retaliationTarget) <= BOSS_TARGET_RANGE * BOSS_TARGET_RANGE) {
					setTarget(retaliationTarget);
					return retaliationTarget;
				}
			}
		}
		return null;
	}

	/**
	 * 设置召唤者与初始继承目标，用于召唤物之间的结盟与目标继承。
	 */
	private void setJellySummoner(BasicCandySlimeEntity summoner, @Nullable LivingEntity target) {
		jellySummonerUuid = summoner.getUUID();
		if (target != null && canInheritBossTarget(target)) {
			inheritedBossTargetUuid = target.getUUID();
			inheritedTargetResolveCooldown = 0;
			setTarget(target);
		}
	}

	/**
	 * 每 tick 尝试解析继承目标。若目标被清除，则停止继承。
	 */
	private void tickInheritedBossTarget() {
		if (level().isClientSide || inheritedBossTargetUuid == null || !(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		LivingEntity currentTarget = getTarget();
		if (currentTarget != null && inheritedBossTargetUuid.equals(currentTarget.getUUID())) {
			if (canInheritBossTarget(currentTarget)) {
				return;
			}
			setTarget(null);
			inheritedBossTargetUuid = null;
			inheritedTargetResolveCooldown = 0;
			return;
		}
		if (inheritedTargetResolveCooldown > 0) {
			inheritedTargetResolveCooldown--;
			return;
		}
		inheritedTargetResolveCooldown = INHERITED_TARGET_RESOLVE_INTERVAL;
		Entity inheritedTarget = serverLevel.getEntity(inheritedBossTargetUuid);
		if (inheritedTarget instanceof LivingEntity livingTarget && canInheritBossTarget(livingTarget)) {
			setTarget(livingTarget);
		} else if (inheritedTarget != null) {
			if (getTarget() == inheritedTarget) {
				setTarget(null);
			}
			inheritedBossTargetUuid = null;
			inheritedTargetResolveCooldown = 0;
		}
	}

	/**
	 * 是否可继承该目标。
	 */
	private boolean canInheritBossTarget(Entity target) {
		return target instanceof LivingEntity
				&& target.isAlive()
				&& !(target instanceof BasicCandySlimeEntity slimeTarget && isSummonedAlly(slimeTarget))
				&& CandyTargeting.canAttackEntity(target);
	}

	/**
	 * 判断两个糖果史莱姆是否是盟友。
	 *
	 * <p>盟约规则：共享同一个召唤者 UUID，或一方召唤了另一方。
	 */
	private boolean isSummonedAlly(BasicCandySlimeEntity other) {
		UUID thisId = getUUID();
		UUID otherId = other.getUUID();
		return (jellySummonerUuid != null && (jellySummonerUuid.equals(otherId)
				|| jellySummonerUuid.equals(other.jellySummonerUuid)))
				|| (other.jellySummonerUuid != null && other.jellySummonerUuid.equals(thisId));
	}

	/**
	 * Boss 丢失目标后的行为：静止并倒计时，超时则休眠。
	 */
	private void tickBossLostTarget(@Nullable AttributeInstance speed) {
		setBaseValueIfChanged(speed, 0.0D);
		stopHorizontalMovement(false);
		getNavigation().stop();
		setJumping(false);
		if (!isBossAwake()) {
			if (isJellyQueen()) {
				setJellyQueenMode(JELLY_QUEEN_SLEEP_MODE);
			}
			return;
		}
		if (bossLostTargetTicks > 0) {
			bossLostTargetTicks--;
			updateJellyQueenMode();
			return;
		}
		putBossToSleep();
		setBossSlamTicks(0);
		setKingExpandTicks(0);
		setKingDashTicks(0);
		if (isJellyQueen()) {
			setJellyQueenMode(JELLY_QUEEN_SLEEP_MODE);
		}
	}

	/**
	 * 休眠 Boss 回血。
	 */
	private void tickDormantBossRegeneration() {
		if (tickCount % DORMANT_BOSS_HEAL_INTERVAL_TICKS == 0) {
			heal(DORMANT_BOSS_HEAL_AMOUNT);
		}
	}

	/**
	 * 根据当前生命值更新女王阶段。
	 * <ul>
	 *     <li>未唤醒：睡眠</li>
	 *     <li>生命 <= 25%：棕色</li>
	 *     <li>生命 <= 50%：蓝色</li>
	 *     <li>否则：粉色</li>
	 * </ul>
	 */
	private void updateJellyQueenMode() {
		if (!isBossAwake()) {
			setJellyQueenMode(JELLY_QUEEN_SLEEP_MODE);
		} else if (getHealth() <= getMaxHealth() * 0.25F) {
			setJellyQueenMode(JELLY_QUEEN_BROWN_MODE);
		} else if (getHealth() <= getMaxHealth() * 0.5F) {
			setJellyQueenMode(JELLY_QUEEN_BLUE_MODE);
		} else {
			setJellyQueenMode(JELLY_QUEEN_PINK_MODE);
		}
	}

	/**
	 * 女王下一次跳跃延迟。蓝色模式更快。
	 */
	private int nextJellyQueenJumpDelay() {
		return getJellyQueenMode() == JELLY_QUEEN_BLUE_MODE ? random.nextInt(10) + 5 : random.nextInt(40) + 5;
	}

	/**
	 * 设置女王阶段（仅女王有效）。
	 */
	private void setJellyQueenMode(int mode) {
		if (!isJellyQueen()) {
			return;
		}
		entityData.set(JELLY_QUEEN_MODE, Mth.clamp(mode, JELLY_QUEEN_SLEEP_MODE, JELLY_QUEEN_BROWN_MODE));
	}

	/**
	 * 设置砸地动画剩余 tick。
	 */
	private void setBossSlamTicks(int ticks) {
		if (!isCandyBoss()) {
			return;
		}
		entityData.set(BOSS_SLAM_TICKS, Mth.clamp(ticks, 0, BOSS_SLAM_POSE_TICKS));
	}

	/**
	 * 设置膨胀动画剩余 tick。
	 */
	private void setKingExpandTicks(int ticks) {
		if (!isCandyBoss()) {
			return;
		}
		entityData.set(KING_EXPAND_TICKS, Mth.clamp(ticks, 0, KING_EXPAND_POSE_TICKS));
	}

	/**
	 * 设置冲刺动画剩余 tick。
	 */
	private void setKingDashTicks(int ticks) {
		if (!(isKingSlime() || isJellyQueen())) {
			return;
		}
		entityData.set(KING_DASH_TICKS, Mth.clamp(ticks, 0, KING_DASH_POSE_TICKS));
	}

	/**
	 * 设置 Pez 滚轮剩余 tick。
	 */
	private void setPezRollTicks(int ticks) {
		if (!isPezJelly()) {
			return;
		}
		entityData.set(PEZ_ROLL_TICKS, Mth.clamp(ticks, 0, PEZ_ROLL_TOTAL_TICKS));
	}

	/**
	 * 设置 Pez 当前贴附面。变化时会自动触发过渡动画。
	 */
	private void setPezAttachFace(Direction face) {
		if (!isPezJelly()) {
			return;
		}
		Direction currentFace = getPezAttachFace();
		if (currentFace == face) {
			return;
		}
		entityData.set(PEZ_PREVIOUS_ATTACH_FACE, currentFace.ordinal());
		entityData.set(PEZ_ATTACH_FACE, face.ordinal());
		setPezAttachTransitionTicks(PEZ_ATTACH_TRANSITION_DURATION);
	}

	/**
	 * 每 tick 递减贴面过渡计时。
	 */
	private void tickPezAttachTransition() {
		int ticks = entityData.get(PEZ_ATTACH_TRANSITION_TICKS);
		if (ticks > 0) {
			setPezAttachTransitionTicks(ticks - 1);
		}
	}

	/**
	 * 设置贴面过渡剩余 tick。
	 */
	private void setPezAttachTransitionTicks(int ticks) {
		if (isPezJelly()) {
			entityData.set(PEZ_ATTACH_TRANSITION_TICKS, Mth.clamp(ticks, 0, PEZ_ATTACH_TRANSITION_DURATION));
		}
	}

	/**
	 * 设置 Pez 滚动方向。
	 */
	private void setPezRollDirection(Direction direction) {
		if (!isPezJelly()) {
			return;
		}
		entityData.set(PEZ_ROLL_DIRECTION, direction.ordinal());
	}

	/**
	 * 设置 Pez 滚动步数。
	 */
	private void setPezRollSteps(int steps) {
		if (!isPezJelly()) {
			return;
		}
		entityData.set(PEZ_ROLL_STEPS, Math.max(0, steps));
	}

	/**
	 * 设置 Pez 累计滚动距离。
	 */
	private void setPezRollDistance(float distance) {
		if (!isPezJelly()) {
			return;
		}
		entityData.set(PEZ_ROLL_DISTANCE, Math.max(0.0F, distance));
	}

	/**
	 * 累加 Pez 滚动距离。
	 */
	private void addPezRollDistance(double distance) {
		if (distance > 1.0E-4D) {
			setPezRollDistance(getPezRollDistance() + (float) distance);
		}
	}

	/**
	 * 设置弹跳动画剩余 tick。
	 */
	private void setBossBounceTicks(int ticks) {
		if (!(isKingSlime() || isJellyQueen())) {
			return;
		}
		entityData.set(BOSS_BOUNCE_TICKS, Mth.clamp(ticks, 0, BOSS_BOUNCE_POSE_TICKS));
	}

	/**
	 * 设置休息剩余 tick。
	 */
	private void setBossRestingTicks(int ticks) {
		if (!isCandyBoss()) {
			return;
		}
		entityData.set(BOSS_RESTING_TICKS, Mth.clamp(ticks, 0, BOSS_REST_TICKS));
	}

	// ===== 客户端读取接口 =====

	/**
	 * 获取女王阶段模式。
	 */
	public int getJellyQueenMode() {
		return isJellyQueen() ? entityData.get(JELLY_QUEEN_MODE) : JELLY_QUEEN_SLEEP_MODE;
	}

	/**
	 * 获取砸地动画剩余 tick。
	 */
	public int getBossSlamTicks() {
		return isCandyBoss() ? entityData.get(BOSS_SLAM_TICKS) : 0;
	}

	/**
	 * 获取膨胀动画剩余 tick。
	 */
	public int getKingExpandTicks() {
		return isCandyBoss() ? entityData.get(KING_EXPAND_TICKS) : 0;
	}

	/**
	 * 获取冲刺动画剩余 tick。
	 */
	public int getKingDashTicks() {
		return isKingSlime() || isJellyQueen() ? entityData.get(KING_DASH_TICKS) : 0;
	}

	/**
	 * 获取 Pez 滚轮剩余 tick。
	 */
	public int getPezRollTicks() {
		return isPezJelly() ? entityData.get(PEZ_ROLL_TICKS) : 0;
	}

	/**
	 * 获取 Pez 当前贴面。
	 */
	public Direction getPezAttachFace() {
		if (!isPezJelly()) {
			return Direction.UP;
		}
		int ordinal = Mth.clamp(entityData.get(PEZ_ATTACH_FACE), 0, DIRECTIONS.length - 1);
		return DIRECTIONS[ordinal];
	}

	/**
	 * 获取 Pez 上一次贴面。
	 */
	public Direction getPezPreviousAttachFace() {
		if (!isPezJelly()) {
			return Direction.UP;
		}
		int ordinal = Mth.clamp(entityData.get(PEZ_PREVIOUS_ATTACH_FACE), 0, DIRECTIONS.length - 1);
		return DIRECTIONS[ordinal];
	}

	/**
	 * 贴面过渡进度（0~1），用于在两个贴面之间做插值渲染。
	 */
	public float getPezAttachTransitionProgress(float partialTicks) {
		if (!isPezJelly()) {
			return 1.0F;
		}
		float remaining = Math.max(0.0F, entityData.get(PEZ_ATTACH_TRANSITION_TICKS) - partialTicks);
		return 1.0F - Mth.clamp(remaining / PEZ_ATTACH_TRANSITION_DURATION, 0.0F, 1.0F);
	}

	/**
	 * 获取 Pez 滚动方向。
	 */
	public Direction getPezRollDirection() {
		if (!isPezJelly()) {
			return Direction.NORTH;
		}
		int ordinal = Mth.clamp(entityData.get(PEZ_ROLL_DIRECTION), 0, DIRECTIONS.length - 1);
		return DIRECTIONS[ordinal];
	}

	/**
	 * 获取 Pez 滚动步数。
	 */
	public int getPezRollSteps() {
		return isPezJelly() ? entityData.get(PEZ_ROLL_STEPS) : 0;
	}

	/**
	 * 获取 Pez 累计滚动距离。
	 */
	public float getPezRollDistance() {
		return isPezJelly() ? entityData.get(PEZ_ROLL_DISTANCE) : 0.0F;
	}

	/**
	 * 获取弹跳动画剩余 tick。
	 */
	public int getBossBounceTicks() {
		return isKingSlime() || isJellyQueen() ? entityData.get(BOSS_BOUNCE_TICKS) : 0;
	}

	/**
	 * 获取休息剩余 tick。
	 */
	public int getBossRestingTicks() {
		return isCandyBoss() ? entityData.get(BOSS_RESTING_TICKS) : 0;
	}

	/**
	 * 当前果冻对应的落地粒子。
	 */
	private ParticleOptions jellyLandingParticle() {
		return new ItemParticleOption(ParticleTypes.ITEM, new net.minecraft.world.item.ItemStack(jellyLandingParticleItem()));
	}

	/**
	 * 各类型对应的果冻球物品，用于落地粒子。
	 */
	private net.minecraft.world.item.Item jellyLandingParticleItem() {
		if (isYellowJelly()) {
			return CCItems.LEMON_JELLY_BALL.get();
		}
		if (isRedJelly()) {
			return CCItems.RASPBERRY_JELLY_BALL.get();
		}
		if (isTornadoJelly()) {
			return CCItems.MINT_JELLY_BALL.get();
		}
		if (isPezJelly()) {
			return CCItems.PEZ_JELLY_BALL.get();
		}
		if (isKingSlime()) {
			return CCItems.CARAMEL_KING_JELLY_BALL.get();
		}
		if (isJellyQueen()) {
			return CCItems.STRAWBERRY_QUEEN_JELLY_BALL.get();
		}
		return CCItems.GUMMY_BALL.get();
	}

	/**
	 * 女王砸地动画进度（保留旧接口）。
	 */
	public float getJellyQueenSlamProgress(float partialTicks) {
		return getBossSlamProgress(partialTicks);
	}

	/**
	 * 砸地动画进度（0~1，剩余 tick 比例）。
	 */
	public float getBossSlamProgress(float partialTicks) {
		if (!isCandyBoss()) {
			return 0.0F;
		}
		float ticks = Math.max(0.0F, getBossSlamTicks() - partialTicks);
		return Mth.clamp(ticks / (float) BOSS_SLAM_POSE_TICKS, 0.0F, 1.0F);
	}

	/**
	 * 膨胀动画进度。
	 */
	public float getKingExpandProgress(float partialTicks) {
		if (!(isPezJelly() || isKingSlime() || isJellyQueen())) {
			return 0.0F;
		}
		float ticks = Math.max(0.0F, getKingExpandTicks() - partialTicks);
		return Mth.clamp(ticks / (float) KING_EXPAND_POSE_TICKS, 0.0F, 1.0F);
	}

	/**
	 * 冲刺动画进度。
	 */
	public float getKingDashProgress(float partialTicks) {
		if (!(isKingSlime() || isJellyQueen())) {
			return 0.0F;
		}
		float ticks = Math.max(0.0F, getKingDashTicks() - partialTicks);
		return Mth.clamp(ticks / (float) KING_DASH_POSE_TICKS, 0.0F, 1.0F);
	}

	/**
	 * 冲刺蓄力进度（0~1）。
	 */
	public float getKingDashChargeProgress(float partialTicks) {
		float dashProgress = getKingDashProgress(partialTicks);
		if (dashProgress <= 0.0F) {
			return 0.0F;
		}
		float elapsed = (1.0F - dashProgress) * KING_DASH_POSE_TICKS;
		return Mth.clamp(elapsed / (float) KING_DASH_CHARGE_TICKS, 0.0F, 1.0F);
	}

	/**
	 * Pez 滚轮总进度。
	 */
	public float getPezRollProgress(float partialTicks) {
		if (!isPezJelly()) {
			return 0.0F;
		}
		float ticks = Math.max(0.0F, getPezRollTicks() - partialTicks);
		return Mth.clamp(ticks / (float) PEZ_ROLL_TOTAL_TICKS, 0.0F, 1.0F);
	}

	/**
	 * Pez 滚轮已消耗比例（0~1）。
	 */
	public float getPezRollElapsed(float partialTicks) {
		if (!isPezJelly()) {
			return 0.0F;
		}
		return Mth.clamp(1.0F - getPezRollProgress(partialTicks), 0.0F, 1.0F);
	}

	/**
	 * 弹跳动画进度。
	 */
	public float getBossBounceProgress(float partialTicks) {
		if (!(isKingSlime() || isJellyQueen())) {
			return 0.0F;
		}
		float ticks = Math.max(0.0F, getBossBounceTicks() - partialTicks);
		return Mth.clamp(ticks / (float) BOSS_BOUNCE_POSE_TICKS, 0.0F, 1.0F);
	}

	/**
	 * 休息动画进度。
	 */
	public float getBossRestingProgress(float partialTicks) {
		if (!isCandyBoss()) {
			return 0.0F;
		}
		float ticks = Math.max(0.0F, getBossRestingTicks() - partialTicks);
		return Mth.clamp(ticks / (float) BOSS_REST_TICKS, 0.0F, 1.0F);
	}

	/**
	 * 是否处于技能后的休息状态。
	 */
	public boolean isBossResting() {
		return getBossRestingTicks() > 0;
	}

	/**
	 * 是否已唤醒（非 Boss 恒为 false）。
	 */
	public boolean isBossAwake() {
		return isCandyBoss() && entityData.get(BOSS_AWAKE);
	}

	/**
	 * 设置唤醒状态。切换时重置休眠朝向记录，并同步血条可见性。
	 */
	private void setBossAwake(boolean awake) {
		if (!isCandyBoss()) {
			return;
		}
		if (isBossAwake() != awake) {
			dormantRotationInitialized = false;
		}
		entityData.set(BOSS_AWAKE, awake);
		if (!level().isClientSide) {
			bossEvent.setVisible(awake);
		}
		if (isJellyQueen()) {
			setJellyQueenMode(awake ? getJellyQueenMode() == JELLY_QUEEN_SLEEP_MODE ? JELLY_QUEEN_PINK_MODE : getJellyQueenMode() : JELLY_QUEEN_SLEEP_MODE);
		}
	}

	/**
	 * 地牢 Boss 生成时调用：重置所有状态，进入休眠。
	 */
	public void prepareDungeonBossSpawn() {
		applyLegacySpawnSize();
		putBossToSleep();
		bossJumpCooldown = 0;
		bossLostTargetTicks = 0;
		bossRangedCooldown = 0;
		kingExpandCooldown = 0;
		kingDashCooldown = 0;
		kingDashChargeTicks = 0;
		kingDashTarget = null;
		kingExpandDamageReady = false;
		bossMultiTargetSlamCount = 0;
		pezSlamCount = 0;
		pezRollCooldown = 0;
		pezRollTarget = null;
		pezRollAttackReleased = false;
		pezOpenGroundRoll = false;
		pezOpenRollHitTargets.clear();
		bossSlamAttackActive = false;
		bossSlamDamageReady = false;
		setBossSlamTicks(0);
		setKingExpandTicks(0);
		setKingDashTicks(0);
		setPezRollTicks(0);
		setPezAttachFace(Direction.UP);
		setPezRollSteps(0);
		setPezRollDistance(0.0F);
		setBossBounceTicks(0);
		setBossRestingTicks(0);
		setDeltaMovement(0.0D, 0.0D, 0.0D);
		if (isJellyQueen()) {
			setJellyQueenMode(JELLY_QUEEN_SLEEP_MODE);
		}
	}

	/**
	 * 让 Boss 进入休眠，清理技能与目标。
	 */
	private void putBossToSleep() {
		setBossAwake(false);
		bossRetaliationTarget = null;
		bossLostTargetTicks = 0;
		bossSlamAttackActive = false;
		bossSlamDamageReady = false;
		kingDashTarget = null;
		kingDashChargeTicks = 0;
		pezRollTarget = null;
		pezRollAttackReleased = false;
		pezOpenGroundRoll = false;
		pezOpenRollHitTargets.clear();
		pezRollBrushDamageCooldown = 0;
		setPezRollTicks(0);
		setPezRollSteps(0);
		setPezRollDistance(0.0F);
		setBossBounceTicks(0);
		setBossRestingTicks(0);
		setNoGravity(false);
		setTarget(null);
	}

	/**
	 * 国王受伤后按生命比例缩小。生命越低，尺寸越小（最小 1）。
	 */
	private void shrinkKingSlimeFromHealth() {
		double percent = (double) (getHealth() / getMaxHealth()) * 12.0D;
		int targetSize = Math.max(1, (int) percent + 1);
		if (getSize() > targetSize) {
			setSize(targetSize, false);
		}
	}

	/**
	 * Pez 死亡分裂：尺寸 > 1 时爆炸并生成一个更小的 Pez（休眠）。
	 */
	private void splitPezJellyOnDeath() {
		if (pezDeathSplitSpawned || !isPezJelly() || getHealth() > 0.0F || !(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		pezDeathSplitSpawned = true;
		serverLevel.explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.NONE);
		BasicCandySlimeEntity slime = CCEntityTypes.PEZ_JELLY.get().create(serverLevel);
		if (slime != null) {
			slime.setSize(getSize() - 1, true);
			slime.setBossAwake(false);
			slime.moveTo(getX(), getY() + 0.5D, getZ(), random.nextFloat() * 360.0F, 0.0F);
			serverLevel.addFreshEntity(slime);
		}
	}

	/**
	 * 击退攻击者玩家。方向由 Boss 指向玩家，力度 2。
	 */
	private void knockbackAttackingPlayer(Player player) {
		double dx = getX() - player.getX();
		double dz = getZ() - player.getZ();
		// 避免玩家与 Boss 完全重合导致零向量。
		while (dx * dx + dz * dz < 1.0E-4D) {
			dx = (random.nextDouble() - random.nextDouble()) * 0.01D;
			dz = (random.nextDouble() - random.nextDouble()) * 0.01D;
		}
		player.knockback(2.0D, dx, dz);
	}

	/**
	 * 冻结休眠 Boss：固定朝向，清目标，停止移动。
	 */
	private void freezeSleepingBoss() {
		if (!dormantRotationInitialized) {
			dormantYRot = getYRot();
			dormantYHeadRot = getYHeadRot();
			dormantRotationInitialized = true;
		}
		if (getTarget() != null) {
			setTarget(null);
		}
		getNavigation().stop();
		setJumping(false);
		setYRot(dormantYRot);
		yRotO = dormantYRot;
		yBodyRot = dormantYRot;
		yBodyRotO = dormantYRot;
		yHeadRot = dormantYHeadRot;
		yHeadRotO = dormantYHeadRot;
		stopHorizontalMovement(true);
		setBaseValueIfChanged(getAttribute(Attributes.MOVEMENT_SPEED), 0.0D);
	}

	/**
	 * 仅当属性基础值变化时才设置，减少不必要的同步。
	 */
	private static void setBaseValueIfChanged(@Nullable AttributeInstance attribute, double value) {
		if (attribute != null && attribute.getBaseValue() != value) {
			attribute.setBaseValue(value);
		}
	}

	/**
	 * 停止水平移动，可选是否同时限制向上的速度。
	 */
	private void stopHorizontalMovement(boolean clampUpwardMovement) {
		Vec3 movement = getDeltaMovement();
		double verticalMovement = clampUpwardMovement ? Math.min(0.0D, movement.y) : movement.y;
		if (movement.x != 0.0D || movement.z != 0.0D || movement.y != verticalMovement) {
			setDeltaMovement(0.0D, verticalMovement, 0.0D);
		}
	}

	/**
	 * 反射弹射物。方向优先指向攻击者，否则原路反弹。
	 */
	private void reflectProjectile(DamageSource source) {
		if (level().isClientSide || !(source.getDirectEntity() instanceof Projectile projectile)) {
			return;
		}
		Vec3 direction = projectile.getDeltaMovement().scale(-1.0D);
		Entity attacker = source.getEntity();
		if (attacker != null) {
			direction = attacker.getEyePosition().subtract(projectile.position());
		}
		if (direction.lengthSqr() < 1.0E-4D) {
			direction = projectile.position().subtract(position());
		}
		if (direction.lengthSqr() < 1.0E-4D) {
			direction = new Vec3(0.0D, 0.15D, 1.0D);
		}
		double speed = Math.max(1.2D, projectile.getDeltaMovement().length());
		Vec3 reflected = direction.normalize().scale(speed * 1.25D);
		projectile.setDeltaMovement(reflected);
		projectile.setYRot((float) (Mth.atan2(reflected.x, reflected.z) * (180.0D / Math.PI)));
		projectile.setXRot((float) (Mth.atan2(reflected.y, reflected.horizontalDistance()) * (180.0D / Math.PI)));
		projectile.hasImpulse = true;
	}

	/**
	 * 更新 Boss 血条进度/颜色/可见性。
	 */
	private void updateBossBar() {
		if (!isCandyBoss() || level().isClientSide) {
			return;
		}
		if (isJellyQueen()) {
			bossEvent.setColor(getBossBarColor());
		}
		bossEvent.setProgress(Math.max(0.0F, Math.min(1.0F, getHealth() / getMaxHealth())));
		bossEvent.setVisible(isBossAwake());
	}

	/**
	 * 各 Boss 血条颜色。
	 */
	private BossEvent.BossBarColor getBossBarColor() {
		if (isKingSlime()) {
			return BossEvent.BossBarColor.YELLOW;
		}
		if (isJellyQueen()) {
			return switch (getJellyQueenMode()) {
				case JELLY_QUEEN_BLUE_MODE -> BossEvent.BossBarColor.BLUE;
				case JELLY_QUEEN_BROWN_MODE -> BossEvent.BossBarColor.YELLOW;
				default -> BossEvent.BossBarColor.PINK;
			};
		}
		return BossEvent.BossBarColor.PURPLE;
	}

	// ===== 类型判断工具方法 =====

	/**
	 * 是否是黄色果冻。
	 */
	public boolean isYellowJelly() {
		return getType() == CCEntityTypes.YELLOW_JELLY.get();
	}

	/**
	 * 是否是红色果冻。
	 */
	public boolean isRedJelly() {
		return getType() == CCEntityTypes.RED_JELLY.get();
	}

	/**
	 * 是否是龙卷风/薄荷果冻。
	 */
	public boolean isTornadoJelly() {
		return getType() == CCEntityTypes.TORNADO_JELLY.get();
	}

	/**
	 * 是否是 Pez 果冻。
	 */
	public boolean isPezJelly() {
		return getType() == CCEntityTypes.PEZ_JELLY.get();
	}

	/**
	 * 是否是国王史莱姆。
	 */
	public boolean isKingSlime() {
		return getType() == CCEntityTypes.KING_SLIME.get();
	}

	/**
	 * 是否是果冻女王。
	 */
	public boolean isJellyQueen() {
		return getType() == CCEntityTypes.JELLY_QUEEN.get();
	}

	/**
	 * 是否是 Boss 类（Pez / 国王 / 女王）。
	 */
	private boolean isCandyBoss() {
		return isPezJelly() || isKingSlime() || isJellyQueen();
	}

	/**
	 * 是否站在/泡在 Grenadine 液体中（脚部或眼部任一即可）。
	 */
	private boolean isInGrenadine() {
		FluidState feet = level().getFluidState(blockPosition());
		FluidState eye = level().getFluidState(BlockPos.containing(getX(), getEyeY(), getZ()));
		return isGrenadine(feet) || isGrenadine(eye);
	}

	/**
	 * 该流体状态是否是 Grenadine（源或流动）。
	 */
	private static boolean isGrenadine(FluidState state) {
		return state.is(CCFluids.SOURCE_GRENADINE.get()) || state.is(CCFluids.FLOWING_GRENADINE.get());
	}

	/**
	 * 是否是“类似生存模式”的玩家（可被攻击）。
	 */
	private static boolean isSurvivalLike(Player player) {
		return CandyTargeting.canAttackPlayer(player);
	}

	/**
	 * 应用旧版固定尺寸规则：
	 * <ul>
	 *     <li>红色：2</li>
	 *     <li>黄色/龙卷风：1</li>
	 *     <li>Pez：10</li>
	 *     <li>国王：13</li>
	 *     <li>女王：6</li>
	 * </ul>
	 */
	private void applyLegacySpawnSize() {
		if (isRedJelly()) {
			setSize(2, true);
		} else if (isYellowJelly() || isTornadoJelly()) {
			setSize(1, true);
		} else if (isPezJelly()) {
			setSize(10, true);
		} else if (isKingSlime()) {
			setSize(13, true);
		} else if (isJellyQueen()) {
			setSize(6, true);
		}
	}
}