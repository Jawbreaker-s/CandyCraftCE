package cn.jawbreakers.candycraftce.mixin.entity;

import cn.jawbreakers.candycraftce.mixin_stub.ICandyStuckProjectileCarrier;
import cn.jawbreakers.candycraftce.mixin_stub.IPurpleJellyStuckEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements IPurpleJellyStuckEntity, ICandyStuckProjectileCarrier {
	@Shadow
	@Final
	protected SynchedEntityData entityData;
	@Unique
	private final String candycraftce$honeyBoltCountKey = "HoneyBoltCount";
	@Unique
	private final String candycraftce$honeyArrowCountKey = "HoneyArrowCount";
	@Unique
	private final String candycraftce$purpleJellyStuckKey = "PurpleJellyStuck";
	@Unique
	private static final EntityDataAccessor<Boolean> candycraftce$purpleJellyStuck = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
	@Unique
	private static final EntityDataAccessor<Integer> candycraftce$honeyArrowCount = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.INT);
	@Unique
	private static final EntityDataAccessor<Integer> candycraftce$honeyBoltCount = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.INT);


	@ModifyVariable(
			method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
			at = @At("HEAD"),
			argsOnly = true
	)
	private Vec3 applyPurpleJellyStuckMovement(Vec3 movement) {
		if (!getCandycraftce_purpleJellyStuck()) {
			return movement;
		}
		setCandycraftce_purpleJellyStuck(false);
		Entity entity = (Entity) (Object) this;
		entity.setDeltaMovement(Vec3.ZERO);
		return movement.multiply(0.25D, 0.05D, 0.25D);
	}

	@Inject(method = "saveWithoutId",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
	)
	private void saveCandy(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
		compound.putInt(candycraftce$honeyBoltCountKey, getCandycraftce_honeyBoltCount());
		compound.putInt(candycraftce$honeyArrowCountKey, getCandycraftce_honeyArrowCount());
		compound.putBoolean(candycraftce$purpleJellyStuckKey, getCandycraftce_purpleJellyStuck());
	}

	@Inject(method = "load",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
	)
	private void loadCandy(CompoundTag compound, CallbackInfo ci) {
		setCandycraftce_honeyBoltCount(compound.getInt(candycraftce$honeyBoltCountKey));
		setCandycraftce_honeyArrowCount(compound.getInt(candycraftce$honeyArrowCountKey));
		setCandycraftce_purpleJellyStuck(compound.getBoolean(candycraftce$purpleJellyStuckKey));
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void defineCandyData(CallbackInfo ci) {
		entityData.define(candycraftce$honeyArrowCount, 0);
		entityData.define(candycraftce$honeyBoltCount, 0);
		entityData.define(candycraftce$purpleJellyStuck, false);
	}

	@Override
	public int getCandycraftce_honeyArrowCount() {
		return entityData.get(candycraftce$honeyArrowCount);
	}

	@Override
	public void setCandycraftce_honeyArrowCount(int i) {
		entityData.set(candycraftce$honeyArrowCount, i);
	}

	@Override
	public int getCandycraftce_honeyBoltCount() {
		return entityData.get(candycraftce$honeyBoltCount);
	}

	@Override
	public void setCandycraftce_honeyBoltCount(int i) {
		entityData.set(candycraftce$honeyBoltCount, i);
	}


	@Override
	public boolean getCandycraftce_purpleJellyStuck() {
		return entityData.get(candycraftce$purpleJellyStuck);
	}

	@Override
	public void setCandycraftce_purpleJellyStuck(boolean flag) {
		entityData.set(candycraftce$purpleJellyStuck, flag);
	}
}
