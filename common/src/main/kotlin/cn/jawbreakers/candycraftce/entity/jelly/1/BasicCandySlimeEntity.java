package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCFluids;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSoundEvents;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 果冻女王（{@code JELLY_QUEEN}）实体类。
 *
 * <p>本文件由 {@code BasicCandySlimeEntity} 精简而来：断言当前实例恒为果冻女王，
 * 因此所有 {@code isJellyQueen() / isCandyBoss()} 的类型分支都被展开为女王一侧的取值，
 * 与普通果冻（黄/红/龙卷风）、Pez 滚轮、国王专属相关的字段与方法已整体移除。
 *
 * <p>数值、判定顺序、tick 时长均保持原样，未新增任何行为：
 * <ul>
 *     <li>阶段模式：休眠 / 粉色（生命 &gt; 50%）/ 蓝色（&le; 50%，跳得更快、砸地更远）/ 棕色（&le; 25%，落地双重爆炸）</li>
 *     <li>技能：砸地、膨胀、冲刺、弹跳、远程果冻球</li>
 *     <li>状态机：休眠冻结回血 → 被攻击唤醒 → 追击 → 丢失目标后重新休眠</li>
 *     <li>服务端 Boss 血条，颜色随阶段变化（粉 / 蓝 / 黄）</li>
 * </ul>
 *
 * <p>已移除的主要部分（女王实例永远不会走到）：
 * <ul>
 *     <li>Pez 滚轮全部：贴面检测、沿墙滚动、贴墙攻击、碎屑粒子、{@code travel} 覆写、死亡分裂</li>
 *     <li>普通果冻的接触爆炸/自毁与报复目标逻辑</li>
 *     <li>国王的按生命缩小、召唤黄色果冻与缓慢效果</li>
 *     <li>召唤物目标继承（{@code inheritedBossTargetUuid} 一整套）</li>
 * </ul>
 */
public class BasicCandySlimeEntity extends Slime {
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
	 * 膨胀动作持续多少 tick。
	 */
	private static final int KING_EXPAND_POSE_TICKS = 36;
	/**
	 * 冲刺动作持续多少 tick。
	 */
	private static final int KING_DASH_POSE_TICKS = 54;
	/**
	 * 冲刺前蓄力多少 tick，之后才真正释放冲刺。
	 */
	private static final int KING_DASH_CHARGE_TICKS = 22;
	/**
	 * Boss 弹跳动作持续多少 tick。
	 */
	private static final int BOSS_BOUNCE_POSE_TICKS = 64;
	/**
	 * 弹跳前蓄力多少 tick。
	 */
	private static final int BOSS_BOUNCE_CHARGE_TICKS = 40;
	/**
	 * 技能释放后强制休息多少 tick。
	 */
	private static final int BOSS_REST_TICKS = 40;

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
	 * 同步到客户端的砸地动画剩余 tick。
	 */
	private static final EntityDataAccessor<Integer> BOSS_SLAM_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
	/**
	 * 同步到客户端的膨胀动画剩余 tick。
	 */
	private static final EntityDataAccessor<Integer> KING_EXPAND_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
	/**
	 * 同步到客户端的冲刺动画剩余 tick。
	 */
	private static final EntityDataAccessor<Integer> KING_DASH_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
	/**
	 * 同步到客户端的弹跳动画剩余 tick。
	 */
	private static final EntityDataAccessor<Integer> BOSS_BOUNCE_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
	/**
	 * 同步到客户端的休息剩余 tick。
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
	 * 膨胀技能冷却。
	 */
	private int kingExpandCooldown;
	/**
	 * 冲刺技能冷却。
	 */
	private int kingDashCooldown;
	/**
	 * 冲刺蓄力剩余 tick。
	 */
	private int kingDashChargeTicks;
	/**
	 * 弹跳技能冷却。
	 */
	private int bossBounceCooldown;
	/**
	 * 本次弹跳是否还未结算伤害。
	 */
	private boolean bossBounceDamageReady;
	/**
	 * 本次膨胀是否还未结算伤害。
	 */
	private boolean kingExpandDamageReady;
	/**
	 * 冲刺锁定的目标。
	 */
	@Nullable
	private LivingEntity kingDashTarget;
	/**
	 * 连续多次砸中多个目标时的计数，累积到 3 次触发膨胀。
	 */
	private int bossMultiTargetSlamCount;
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
	 * 服务端 Boss 血条。
	 */
	private final ServerBossEvent bossEvent = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
	/**
	 * Boss 被攻击后临时锁定的报复目标。
	 */
	@Nullable
	private LivingEntity bossRetaliationTarget;
	/**
	 * 召唤者 UUID，用于识别盟友（女王自身通常为 null，但会读取其他实体的该字段）。
	 */
	@Nullable
	private UUID jellySummonerUuid;

	public BasicCandySlimeEntity(EntityType<? extends BasicCandySlimeEntity> type, Level level) {
		super(type, level);
		// 构造时初始化血条名称/颜色（颜色随后由阶段更新覆盖）。
//		bossEvent.setName(type.getDescription());
//		bossEvent.setColor(getBossBarColor());
	}

	/**
	 * 注册 AI 目标。
	 *
	 * <p>清空原版史莱姆的所有 target goal，只保留“攻击玩家”。
	 * 是否真的能攻击由 {@code CandyTargeting.canAttackPlayer} 决定（创造/和平模式跳过）。
	 */
//	@Override
//	protected void registerGoals() {
//		super.registerGoals();
//		targetSelector.removeAllGoals(goal -> true);
//		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
//				entity -> entity instanceof Player player && CandyTargeting.canAttackPlayer(player)));
//	}

	/**
	 * 定义需要同步到客户端的数据。女王固定注册以下 7 项。
	 */
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(BOSS_AWAKE, false);
		entityData.define(JELLY_QUEEN_MODE, JELLY_QUEEN_SLEEP_MODE);
		entityData.define(BOSS_SLAM_TICKS, 0);
		entityData.define(KING_EXPAND_TICKS, 0);
		entityData.define(KING_DASH_TICKS, 0);
		entityData.define(BOSS_BOUNCE_TICKS, 0);
		entityData.define(BOSS_RESTING_TICKS, 0);
	}


//	/**
//	 * 生成时收尾：应用旧版（1.12 以前）的固定尺寸规则。
//	 */
//	@Override
//	@Nullable
//	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
//		SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
//		applyLegacySpawnSize();
//		return data;
//	}

	/**
	 * 每 tick 的 AI 主循环（服务端与客户端都会跑）。
	 *
	 * <p>执行顺序：
	 * <ol>
	 *     <li>休眠冻结（{@code super.aiStep} 前后各一次，防止原版逻辑改动朝向）</li>
	 *     <li>原版 Slime AI</li>
	 *     <li>Boss 血条刷新</li>
	 *     <li>接触伤害冷却递减</li>
	 *     <li>Boss 服务端行为（技能状态机）</li>
	 *     <li>与目标碰撞箱重叠时主动攻击</li>
	 * </ol>
	 */
	@Override
	public void aiStep() {
		boolean clientSide = level().isClientSide;
//		if (!isBossAwake()) {
//			freezeSleepingBoss();
//		}
		super.aiStep();
		if (!clientSide) {
			updateBossBar();
		}
		if (specialAttackCooldown > 0) {
			specialAttackCooldown--;
		}
//		if (!clientSide) {
//			tickServerBossBehavior();
//		}
//		// 再次冻结，防止 super.aiStep 或 Boss 行为修改了朝向/速度。
//		if (!isBossAwake()) {
//			freezeSleepingBoss();
//		}
		if (!clientSide) {
			tickRetaliationTarget();
		}
	}

	/**
	 * Boss 服务端行为的统一入口，按顺序推进各类技能/动画状态机。
	 */
	private void tickServerBossBehavior() {
		tickBossSlamLandingAndAnimation();
		tickBossSpecialAnimation();
		tickBossBounceAndRest();
//		tickBossAwakeBehavior();
	}

	/**
	 * 跳跃间隔：由当前阶段决定，蓝色模式跳得最快（5~14 tick），其余 5~44 tick。
	 */
	@Override
	protected int getJumpDelay() {
		return nextJellyQueenJumpDelay();
	}

	/**
	 * 起跳。休眠状态下不允许起跳。
	 */
	@Override
	protected void jumpFromGround() {
		if (!isBossAwake()) {
			return;
		}
		super.jumpFromGround();
	}

	/**
	 * 玩家与实体碰撞时触发。
	 *
	 * <p>原版史莱姆的接触伤害被 {@link #isDealsDamage()} 禁用，走自定义的 Boss 接触伤害：
	 * 需要视线可见且在 {@code 0.6 * size} 范围内，伤害为 {@code size * 2}。
	 */
	@Override
	public void playerTouch(Player player) {
		if (!isSurvivalLike(player)) {
			return;
		}
		if (!isAlive() || specialAttackCooldown > 0) {
			return;
		}
		if (!isBossAwake()) {
			return;
		}
		specialAttackCooldown = 15;
		hurtPlayerWithLegacyBossContact(player);
	}

	/**
	 * 关闭原版 Slime 的接触伤害路径。
	 *
	 * <p>原因有二：
	 * <ol>
	 *     <li>所有接触伤害都在 {@link #playerTouch} / {@link #doHurtTarget} 里手动实现</li>
	 *     <li>原版 {@code isDealsDamage()} 还会让史莱姆直接攻击铁傀儡，本项目不希望这样</li>
	 * </ol>
	 */
	@Override
	protected boolean isDealsDamage() {
		return false;
	}

	/**
	 * 是否在远离玩家时自动消失。女王不消失。
	 */
	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	/**
	 * 和平模式是否消失。女王不消失。
	 */
	@Override
	public boolean shouldDespawnInPeaceful() {
		return false;
	}

	/**
	 * 受伤入口。判定顺序很重要：
	 * <ol>
	 *     <li>召唤盟友免伤</li>
	 *     <li>Grenadine 液体中的环境伤害免疫</li>
	 *     <li>弹射物：反射 + 未唤醒时免伤 + 唤醒后 3/4 概率免伤，剩余情况半伤</li>
	 *     <li>被生物攻击时唤醒并设置报复目标</li>
	 *     <li>免疫摔落伤害</li>
	 *     <li>女王：唤醒 + 阶段更新 + 近战击退攻击玩家</li>
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
		// 弹射物：唤醒判定 + 反射 + 减伤。
		if (source.is(DamageTypeTags.IS_PROJECTILE)) {
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
		// 有活体攻击者时唤醒。
		if (!level().isClientSide && livingAttacker != null) {
			activateBossFromDamage(livingAttacker);
		}
		// 免疫摔落伤害。
		if (source.is(DamageTypeTags.IS_FALL)) {
			return false;
		}
		// 女王：唤醒 + 阶段更新 + 击退近战玩家（创造模式除外）。
		if (!level().isClientSide && livingAttacker != null) {
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
	 * 主动攻击目标（AI 调用）。目标是任意实体，需要先经过 {@link #canAttackTarget} 过滤。
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
		if (!isBossAwake()) {
			return false;
		}
		specialAttackCooldown = 15;
		return hurtLegacyBossTarget(target);
	}

	/**
	 * 玩家开始追踪本实体时加入其 Boss 血条。
	 */
	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		bossEvent.addPlayer(player);
	}

	/**
	 * 玩家不再追踪本实体时从血条移除。
	 */
	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		bossEvent.removePlayer(player);
	}

	/**
	 * 死亡掉落：唱片 + 果冻钥匙 + 果冻徽章 + 草莓女王果冻球 16~32 个。
	 */
	@Override
	protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
		spawnAtLocation(CCItems.RECORD_1.get());
		spawnAtLocation(CCItems.JELLY_KEY.get());
		spawnAtLocation(CCItems.JELLY_EMBLEM.get());
		dropBossJellyBalls(CCItems.STRAWBERRY_QUEEN_JELLY_BALL.get());
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
	 * 保存数据到 NBT。0 值字段用 {@code putIntIfNonZero} 跳过以精简存档。
	 */
	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (isBossAwake()) {
			tag.putBoolean("BossAwake", true);
		}
		putIntIfNonZero(tag, "BossLostTargetTicks", bossLostTargetTicks);
		putIntIfNonZero(tag, "BossRangedCooldown", bossRangedCooldown);
		putIntIfNonZero(tag, "KingExpandCooldown", kingExpandCooldown);
		putIntIfNonZero(tag, "KingDashCooldown", kingDashCooldown);
		putIntIfNonZero(tag, "BossMultiTargetSlamCount", bossMultiTargetSlamCount);
		if (jellySummonerUuid != null) {
			tag.putUUID("JellySummoner", jellySummonerUuid);
		}
		tag.putInt("JellyQueenMode", getJellyQueenMode());
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
	 * 从 NBT 读取数据。阶段模式有默认回退：已唤醒则为粉色，否则为休眠。
	 */
	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setBossAwake(tag.getBoolean("BossAwake"));
		bossLostTargetTicks = tag.getInt("BossLostTargetTicks");
		bossRangedCooldown = tag.getInt("BossRangedCooldown");
		kingExpandCooldown = tag.getInt("KingExpandCooldown");
		kingDashCooldown = tag.getInt("KingDashCooldown");
		bossMultiTargetSlamCount = tag.getInt("BossMultiTargetSlamCount");
		jellySummonerUuid = tag.hasUUID("JellySummoner") ? tag.getUUID("JellySummoner") : null;
		setJellyQueenMode(tag.contains("JellyQueenMode")
				? tag.getInt("JellyQueenMode")
				: isBossAwake() ? JELLY_QUEEN_PINK_MODE : JELLY_QUEEN_SLEEP_MODE);
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
	 * 落地粒子类型：草莓女王果冻球。
	 */
	@Override
	protected ParticleOptions getParticleType() {
		return jellyLandingParticle();
	}

	/**
	 * 自定义落地粒子。纯客户端执行，围绕自身一圈生成果冻球粒子，数量与尺寸成正比。
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
	 * 设置尺寸。女王固定：经验 500，最大生命 300。
	 */
	@Override
	public void setSize(int size, boolean resetHealth) {
		super.setSize(size, resetHealth);
		AttributeInstance maxHealth = getAttribute(Attributes.MAX_HEALTH);
		if (maxHealth != null) {
			xpReward = 500;
			setBaseValueIfChanged(maxHealth, 300.0D);
			if (resetHealth) {
				setHealth(getMaxHealth());
			}
		}
	}

//	/**
//	 * 唤醒状态的主行为入口：未唤醒时持续回血，随后交给女王专属循环。
//	 */
//	private void tickBossAwakeBehavior() {
//		if (level().isClientSide) {
//			return;
//		}
//		// 未唤醒时持续回血。
//		if (!isBossAwake()) {
//			tickDormantBossRegeneration();
//		}
//		tickJellyQueenBossBehavior();
//	}

	/**
	 * 果冻女王的独立 Boss 行为循环（每 tick，服务端）。
	 *
	 * <p>流程：
	 * <ol>
	 *     <li>找不到目标：进入丢失目标逻辑（静止倒计时 → 休眠）</li>
	 *     <li>未唤醒：保持睡眠模式，速度归零</li>
	 *     <li>已唤醒：速度 0.7，休息阶段速度归零</li>
	 *     <li>更新阶段模式 → 远程攻击判定 → 膨胀/弹跳/冲刺随机判定</li>
	 *     <li>落地时按阶段节奏起跳砸地</li>
	 * </ol>
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
		tickJellyQueenSpecials(target);
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
		if (level().isClientSide) {
			return;
		}
		int slamTicks = getBossSlamTicks();
		if (slamTicks > 0) {
			setBossSlamTicks(slamTicks - 1);
		}
		boolean grounded = onGround();
		if (bossSlamAttackActive && !bossWasOnGround && grounded) {
			// 落地瞬间：记录下落距离，播放半个砸地动作，结算范围伤害。
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
	 * 膨胀 / 冲刺技能倒计时与主流程。
	 *
	 * <ul>
	 *     <li>膨胀：锁定移动，最后 10 tick 结算一次范围伤害，结束进入休息</li>
	 *     <li>冲刺：蓄力 {@link #KING_DASH_CHARGE_TICKS} tick → 释放 → 命中检测</li>
	 * </ul>
	 */
	private void tickBossSpecialAnimation() {
		if (level().isClientSide) {
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
	 * 弹跳技能倒计时与休息阶段处理。
	 *
	 * <p>休息阶段：完全静止并滴水粒子；弹跳：蓄力完成后结算一次范围伤害，结束进入休息。
	 */
	private void tickBossBounceAndRest() {
		if (level().isClientSide) {
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
	 * <p>水平速度由阶段决定（蓝色模式更远），竖直速度在水里更大（4.0 否则 1.5）。
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
		playSound(getJumpSound(), getSoundVolume(), ((random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
	}

	/**
	 * 选择砸地方向。
	 *
	 * <p>默认朝目标直冲；若被阻挡或没有视线，则尝试左右偏转（类似“绕行”），
	 * 偏转角度从 ±24° 到 ±62°，选一个最接近目标且路径可通行的方向。
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
	 * 检查砸地方向是否可通行：沿方向采样 4 个点，看碰撞箱是否会被挡住。
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
	 * 远程攻击：目标较高（&gt; 4 格）或较远（距离平方 &gt; 144，即 12 格）且视线可见时，发射一颗 GummyBall。
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
		// 女王射击最频繁。
		bossRangedCooldown = 48 + random.nextInt(28);
	}

	/**
	 * 发射一颗草莓女王果冻球。
	 *
	 * <p>射击时朝目标加上一点弧度补偿（{@code dy + distance * 0.08}），避免近距离完全平飞。
	 */
	private void shootBossJellyBall(ServerLevel level, LivingEntity target) {
		GummyBallEntity ball = new GummyBallEntity(level, this, 5);
		ball.setVisualVariant(GummyBallEntity.STRAWBERRY_QUEEN_JELLY_VISUAL);
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
	 * 随机特殊技能判定。三个技能互斥（一次只触发一个），各自有距离/随机数条件：
	 * <ol>
	 *     <li>膨胀：距离平方 &lt; 196（14 格）内，1/28 概率</li>
	 *     <li>弹跳：距离平方 &lt; 81（9 格）内，1/48 概率</li>
	 *     <li>冲刺：距离平方 &gt; 49（7 格）外，1/28 概率</li>
	 * </ol>
	 */
	private void tickJellyQueenSpecials(LivingEntity target) {
		if (getKingExpandTicks() == 0 && kingExpandCooldown <= 0 && onGround() && Math.abs(getDeltaMovement().y) < 0.05D && distanceToSqr(target) < 196.0D && random.nextInt(28) == 0) {
			startKingExpandAttack();
			return;
		}
		if (getBossBounceTicks() == 0 && bossBounceCooldown <= 0 && onGround() && distanceToSqr(target) < 81.0D && random.nextInt(48) == 0) {
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
		bossBounceCooldown = 210 + random.nextInt(100);
		bossBounceDamageReady = true;
		setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
		getNavigation().stop();
		playSound(SoundEvents.SLIME_SQUISH, 1.35F, 0.5F);
	}

	/**
	 * 弹跳伤害结算：半径 {@code max(4.25, size * 0.58)}，伤害 4，命中后带击退。
	 */
	private void damageBossBounceTargets() {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		double radius = Math.max(4.25D, getSize() * 0.58D);
		for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius, 1.8D, radius), this::canBossTarget)) {
			if (target == this) {
				continue;
			}
			if (target.hurt(damageSources().mobAttack(this), 4.0F)) {
				double dx = target.getX() - getX();
				double dz = target.getZ() - getZ();
				double length = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
				target.push(dx / length * 1.35D, 0.52D, dz / length * 1.35D);
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
	 * 开始膨胀技能。女王冷却 190~279 tick。
	 */
	private void startKingExpandAttack() {
		setKingExpandTicks(KING_EXPAND_POSE_TICKS);
		kingExpandCooldown = 190 + random.nextInt(90);
		kingExpandDamageReady = true;
		setDeltaMovement(0.0D, 0.0D, 0.0D);
		getNavigation().stop();
		playSound(SoundEvents.SLIME_SQUISH, 1.2F, 0.55F);
	}

	/**
	 * 从高处落地后，按下落距离有概率额外触发冲刺。下落越高概率越大（最高 1/2，最低 1/10）。
	 */
	private void tryStartHeightBonusDash() {
		LivingEntity target = findBossAttackTarget();
		if (target == null || getKingDashTicks() > 0 || kingDashCooldown > 0) {
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
	 * 膨胀伤害结算：半径 {@code max(7.5, size * 1.05)}，伤害 4.5，命中后带击退。
	 */
	private void damageKingExpandTargets() {
		double radius = Math.max(7.5D, getSize() * 1.05D);
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius, 3.2D, radius), this::canBossTarget)) {
			if (target == this) {
				continue;
			}
			if (target.hurt(damageSources().mobAttack(this), 4.5F)) {
				double dx = target.getX() - getX();
				double dz = target.getZ() - getZ();
				double length = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
				target.push(dx / length * 1.8D, 0.7D, dz / length * 1.8D);
				target.hurtMarked = true;
			}
		}
		serverLevel.sendParticles(jellyLandingParticle(), getX(), getY() + 0.2D, getZ(), 76, radius * 0.33D, 0.38D, radius * 0.33D, 0.12D);
		playSound(SoundEvents.SLIME_ATTACK, 1.9F, 0.54F);
	}

	/**
	 * 开始冲刺技能：先蓄力，然后锁定目标。女王冷却 220~319 tick。
	 */
	private void startKingDashAttack(LivingEntity target) {
		kingDashTarget = target;
		kingDashChargeTicks = KING_DASH_CHARGE_TICKS;
		kingDashCooldown = 220 + random.nextInt(100);
		setKingDashTicks(KING_DASH_POSE_TICKS);
		setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
		getNavigation().stop();
		getLookControl().setLookAt(target);
		playSound(SoundEvents.SLIME_SQUISH, 1.3F, 0.72F);
	}

	/**
	 * 蓄力完成，释放冲刺。速度 1.65 朝目标方向，并叠加 15% 当前动量。
	 */
	private void releaseKingDash(LivingEntity target) {
		Vec3 desired = target.position().subtract(position());
		Vec3 current = getDeltaMovement();
		if (desired.lengthSqr() < 1.0E-4D) {
			desired = getLookAngle();
		}
		Vec3 corrected = desired.normalize().scale(1.65D).add(current.scale(0.15D));
		setDeltaMovement(corrected.x, Math.max(0.08D, corrected.y * 0.15D + 0.28D), corrected.z);
		hasImpulse = true;
		playSound(SoundEvents.SLIME_JUMP, 1.45F, 0.82F);
	}

	/**
	 * 冲刺命中检测：伤害 7，命中后带击退，并把冲刺动画缩短到 10 tick 以内（避免连续命中同一目标）。
	 */
	private void damageKingDashTargets() {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.8D), this::canBossTarget)) {
			if (target == this) {
				continue;
			}
			if (target.hurt(damageSources().mobAttack(this), 7.0F)) {
				Vec3 velocity = getDeltaMovement();
				double horizontal = Math.max(0.1D, velocity.horizontalDistance());
				target.push(velocity.x / horizontal * 0.72D, 0.35D, velocity.z / horizontal * 0.72D);
				target.hurtMarked = true;
				setKingDashTicks(Math.min(getKingDashTicks(), 10));
			}
		}
	}

	/**
	 * 冲刺时在身后留下草莓女王果冻球粒子拖尾（蓄力阶段不生成）。
	 */
	private void spawnKingDashTrail() {
		if (!(level() instanceof ServerLevel serverLevel) || kingDashChargeTicks > 0) {
			return;
		}
		serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(CCItems.STRAWBERRY_QUEEN_JELLY_BALL.get())),
				getX() - getDeltaMovement().x * 0.7D, getY() + getBbHeight() * 0.45D, getZ() - getDeltaMovement().z * 0.7D,
				4, getBbWidth() * 0.2D, getBbHeight() * 0.15D, getBbWidth() * 0.2D, 0.03D);
	}

	/**
	 * 砸地水平速度：蓝色模式 0.82（冲刺更远），其余 0.58。
	 */
	private double bossSlamHorizontalPower() {
		return getJellyQueenMode() == JELLY_QUEEN_BLUE_MODE ? 0.82D : 0.58D;
	}

	/**
	 * 砸地伤害结算。半径 {@code 0.6 * size}，需要视线可见。
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
	 * 砸地伤害公式：{@code size * 2}。
	 */
	private float bossSlamDamage() {
		return getSize() * 2.0F;
	}

	/**
	 * 记录砸地组合：连续 3 次砸中 2 个以上目标后触发膨胀（需膨胀未在冷却/进行中）。
	 */
	private void trackBossSlamCombo(int hits) {
		if (hits >= 2) {
			bossMultiTargetSlamCount++;
		}
		if (bossMultiTargetSlamCount >= 3 && getKingExpandTicks() == 0 && kingExpandCooldown <= 0) {
			startKingExpandAttack();
			bossMultiTargetSlamCount = 0;
		}
	}

	/**
	 * 砸地落地特效。棕色模式：脚下与头顶各一次强度 3 的爆炸（不破坏方块）。
	 */
	private void triggerBossSlamLandingEffects() {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		if (getJellyQueenMode() == JELLY_QUEEN_BROWN_MODE) {
			serverLevel.explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.NONE);
			serverLevel.explode(this, getX(), getY() + 2.0D, getZ(), 3.0F, Level.ExplosionInteraction.NONE);
		}
	}

	/**
	 * Boss 对玩家的接触伤害：需要视线可见且在 {@code 0.6 * size} 范围内，伤害 {@code size * 2}。
	 */
	private boolean hurtPlayerWithLegacyBossContact(Player player) {
		int size = getSize();
		double range = 0.6D * size;
		if (!hasLineOfSight(player) || distanceToSqr(player) >= range * range) {
			return false;
		}
		float damage = size * 2.0F;
		if (player.hurt(damageSources().mobAttack(this), damage)) {
			playSound(SoundEvents.SLIME_ATTACK, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
			return true;
		}
		return false;
	}

	/**
	 * 每 tick 检查当前目标是否已经与自身碰撞箱重叠（含 0.15 的宽容量），重叠则主动攻击。
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
	 * 近战伤害包装：目标合法、有视线且在 {@code 0.6 * size} 射程内才生效。
	 */
	private boolean hurtLegacyBossTarget(Entity target) {
		int size = getSize();
		double range = 0.6D * size;
		if (!canAttackTarget(target) || !hasLineOfSight(target) || distanceToSqr(target) >= range * range) {
			return false;
		}
		return hurtCandyTarget(target, size * 2.0F);
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
	 * 被攻击时唤醒 Boss：刷新丢失目标计时、记录报复目标、设置目标并提速到 0.7。
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
				setBaseValueIfChanged(getAttribute(Attributes.MOVEMENT_SPEED), 0.7D);
			}
		}
	}

	/**
	 * 是否可攻击该目标：女王恒走 Boss 判定。
	 */
	private boolean canAttackTarget(Entity target) {
		return canBossTarget(target);
	}

	/**
	 * Boss 可攻击目标判定：
	 * <ul>
	 *     <li>必须是活体</li>
	 *     <li>不能是自己的召唤盟友</li>
	 *     <li>其他糖果史莱姆只有在“报复目标”或“强制冲突（互相锁定）”时才可攻击</li>
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
	 * Boss 是否可被该来源唤醒：活体、非盟友、非同类（除非强制冲突）。
	 */
	private boolean canBossWakeFrom(Entity source) {
		return source instanceof LivingEntity
				&& source.isAlive()
				&& !(source instanceof BasicCandySlimeEntity slimeSource && isSummonedAlly(slimeSource))
				&& (!(source instanceof BasicCandySlimeEntity wakeSlimeSource) || isForcedJellyConflict(wakeSlimeSource, this));
	}

	/**
	 * 两个糖果史莱姆是否处于“强制冲突”（互相锁定），此时可以互相伤害。
	 */
	private static boolean isForcedJellyConflict(BasicCandySlimeEntity attacker, BasicCandySlimeEntity victim) {
		return attacker.getTarget() == victim || victim.getTarget() == attacker;
	}

	/**
	 * 休眠 Boss 是否应被非玩家生物攻击唤醒（排除玩家友方，如已驯服宠物）。
	 */
	private boolean shouldDormantBossWakeFromMobAttack(LivingEntity attacker) {
		return !isBossAwake()
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
	 * 寻找当前 Boss 应攻击的目标：优先当前 target，其次报复目标；需在 64 格内且合法。
	 */
	@Nullable
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
	 * Boss 丢失目标后的行为：静止并倒计时，超时则休眠（并清空所有技能动画）。
	 */
	private void tickBossLostTarget(@Nullable AttributeInstance speed) {
		setBaseValueIfChanged(speed, 0.0D);
		stopHorizontalMovement(false);
		getNavigation().stop();
		setJumping(false);
		if (!isBossAwake()) {
			setJellyQueenMode(JELLY_QUEEN_SLEEP_MODE);
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
		setJellyQueenMode(JELLY_QUEEN_SLEEP_MODE);
	}

//	/**
//	 * 休眠 Boss 回血：每 20 tick 回 5 点。
//	 */
//	private void tickDormantBossRegeneration() {
//		if (tickCount % DORMANT_BOSS_HEAL_INTERVAL_TICKS == 0) {
//			heal(DORMANT_BOSS_HEAL_AMOUNT);
//		}
//	}

	/**
	 * 根据当前生命值更新女王阶段：
	 * <ul>
	 *     <li>未唤醒：休眠模式</li>
	 *     <li>生命 &le; 25%：棕色</li>
	 *     <li>生命 &le; 50%：蓝色</li>
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
	 * 下一次跳跃延迟：蓝色模式 5~14 tick，其余 5~44 tick。
	 */
	private int nextJellyQueenJumpDelay() {
		return getJellyQueenMode() == JELLY_QUEEN_BLUE_MODE ? random.nextInt(10) + 5 : random.nextInt(40) + 5;
	}

	// ===== 同步数据的写入接口 =====

	/**
	 * 设置女王阶段（钳制在合法范围 [0, 3]）。
	 */
	private void setJellyQueenMode(int mode) {
		entityData.set(JELLY_QUEEN_MODE, Mth.clamp(mode, JELLY_QUEEN_SLEEP_MODE, JELLY_QUEEN_BROWN_MODE));
	}

	/**
	 * 设置砸地动画剩余 tick。
	 */
	private void setBossSlamTicks(int ticks) {
		entityData.set(BOSS_SLAM_TICKS, Mth.clamp(ticks, 0, BOSS_SLAM_POSE_TICKS));
	}

	/**
	 * 设置膨胀动画剩余 tick。
	 */
	private void setKingExpandTicks(int ticks) {
		entityData.set(KING_EXPAND_TICKS, Mth.clamp(ticks, 0, KING_EXPAND_POSE_TICKS));
	}

	/**
	 * 设置冲刺动画剩余 tick。
	 */
	private void setKingDashTicks(int ticks) {
		entityData.set(KING_DASH_TICKS, Mth.clamp(ticks, 0, KING_DASH_POSE_TICKS));
	}

	/**
	 * 设置弹跳动画剩余 tick。
	 */
	private void setBossBounceTicks(int ticks) {
		entityData.set(BOSS_BOUNCE_TICKS, Mth.clamp(ticks, 0, BOSS_BOUNCE_POSE_TICKS));
	}

	/**
	 * 设置休息剩余 tick。
	 */
	private void setBossRestingTicks(int ticks) {
		entityData.set(BOSS_RESTING_TICKS, Mth.clamp(ticks, 0, BOSS_REST_TICKS));
	}

	// ===== 客户端读取接口 =====

	/**
	 * 获取女王阶段模式。
	 */
	public int getJellyQueenMode() {
		return entityData.get(JELLY_QUEEN_MODE);
	}

	/**
	 * 获取砸地动画剩余 tick。
	 */
	public int getBossSlamTicks() {
		return entityData.get(BOSS_SLAM_TICKS);
	}

	/**
	 * 获取膨胀动画剩余 tick。
	 */
	public int getKingExpandTicks() {
		return entityData.get(KING_EXPAND_TICKS);
	}

	/**
	 * 获取冲刺动画剩余 tick。
	 */
	public int getKingDashTicks() {
		return entityData.get(KING_DASH_TICKS);
	}

	/**
	 * 获取弹跳动画剩余 tick。
	 */
	public int getBossBounceTicks() {
		return entityData.get(BOSS_BOUNCE_TICKS);
	}

	/**
	 * 获取休息剩余 tick。
	 */
	public int getBossRestingTicks() {
		return entityData.get(BOSS_RESTING_TICKS);
	}

	/**
	 * 落地粒子：草莓女王果冻球的物品粒子。
	 */
	private ParticleOptions jellyLandingParticle() {
		return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(CCItems.STRAWBERRY_QUEEN_JELLY_BALL.get()));
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
		float ticks = Math.max(0.0F, getBossSlamTicks() - partialTicks);
		return Mth.clamp(ticks / (float) BOSS_SLAM_POSE_TICKS, 0.0F, 1.0F);
	}

	/**
	 * 膨胀动画进度。
	 */
	public float getKingExpandProgress(float partialTicks) {
		float ticks = Math.max(0.0F, getKingExpandTicks() - partialTicks);
		return Mth.clamp(ticks / (float) KING_EXPAND_POSE_TICKS, 0.0F, 1.0F);
	}

	/**
	 * 冲刺动画进度。
	 */
	public float getKingDashProgress(float partialTicks) {
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
	 * 弹跳动画进度。
	 */
	public float getBossBounceProgress(float partialTicks) {
		float ticks = Math.max(0.0F, getBossBounceTicks() - partialTicks);
		return Mth.clamp(ticks / (float) BOSS_BOUNCE_POSE_TICKS, 0.0F, 1.0F);
	}

	/**
	 * 休息动画进度。
	 */
	public float getBossRestingProgress(float partialTicks) {
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
	 * 是否已唤醒。
	 */
	public boolean isBossAwake() {
		return entityData.get(BOSS_AWAKE);
	}

	/**
	 * 设置唤醒状态。切换时重置休眠朝向记录，并同步血条可见性；
	 * 女王唤醒后若仍处于休眠模式则自动切到粉色模式。
	 */
	private void setBossAwake(boolean awake) {
		if (isBossAwake() != awake) {
			dormantRotationInitialized = false;
		}
		entityData.set(BOSS_AWAKE, awake);
		if (!level().isClientSide) {
			bossEvent.setVisible(awake);
		}
		setJellyQueenMode(awake
				? (getJellyQueenMode() == JELLY_QUEEN_SLEEP_MODE ? JELLY_QUEEN_PINK_MODE : getJellyQueenMode())
				: JELLY_QUEEN_SLEEP_MODE);
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
		bossSlamAttackActive = false;
		bossSlamDamageReady = false;
		setBossSlamTicks(0);
		setKingExpandTicks(0);
		setKingDashTicks(0);
		setBossBounceTicks(0);
		setBossRestingTicks(0);
		setDeltaMovement(0.0D, 0.0D, 0.0D);
		setJellyQueenMode(JELLY_QUEEN_SLEEP_MODE);
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
		setBossBounceTicks(0);
		setBossRestingTicks(0);
		setNoGravity(false);
		setTarget(null);
	}

	/**
	 * 击退攻击者玩家。方向由 Boss 指向玩家，力度 2（创造模式外的近战攻击者才会被触发）。
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
//	private void freezeSleepingBoss() {
//		if (!dormantRotationInitialized) {
//			dormantYRot = getYRot();
//			dormantYHeadRot = getYHeadRot();
//			dormantRotationInitialized = true;
//		}
//		if (getTarget() != null) {
//			setTarget(null);
//		}
//		getNavigation().stop();
//		setJumping(false);
//		setYRot(dormantYRot);
//		yRotO = dormantYRot;
//		yBodyRot = dormantYRot;
//		yBodyRotO = dormantYRot;
//		yHeadRot = dormantYHeadRot;
//		yHeadRotO = dormantYHeadRot;
//		stopHorizontalMovement(true);
////		setBaseValueIfChanged(getAttribute(Attributes.MOVEMENT_SPEED), 0.0D);
//	}

//	/**
//	 * 仅当属性基础值变化时才设置，减少不必要的同步。
//	 */
//	private static void setBaseValueIfChanged(@Nullable AttributeInstance attribute, double value) {
//		if (attribute != null && attribute.getBaseValue() != value) {
//			attribute.setBaseValue(value);
//		}
//	}

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
	 * 更新 Boss 血条：女王颜色随阶段变化，进度为当前生命比例，仅在唤醒时可见。
	 */
	private void updateBossBar() {
		if (level().isClientSide) {
			return;
		}
		bossEvent.setColor(getBossBarColor());
		bossEvent.setProgress(Math.max(0.0F, Math.min(1.0F, getHealth() / getMaxHealth())));
		bossEvent.setVisible(isBossAwake());
	}

	/**
	 * 女王血条颜色：蓝色模式 → 蓝，棕色模式 → 黄，其余（休眠/粉色）→ 粉。
	 */
	private BossEvent.BossBarColor getBossBarColor() {
		return switch (getJellyQueenMode()) {
			case JELLY_QUEEN_BLUE_MODE -> BossEvent.BossBarColor.BLUE;
			case JELLY_QUEEN_BROWN_MODE -> BossEvent.BossBarColor.YELLOW;
			default -> BossEvent.BossBarColor.PINK;
		};
	}

	// ===== 类型判断工具方法 =====

	/**
	 * 是否是果冻女王（保留作为对外类型标识，供渲染层/外部代码读取）。
	 */
	public boolean isJellyQueen() {
		return getType() == CCEntityTypes.JELLY_QUEEN.get();
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
	 * 应用旧版固定尺寸规则：女王固定尺寸 6。
	 */
	private void applyLegacySpawnSize() {
		setSize(6, true);
	}
}
