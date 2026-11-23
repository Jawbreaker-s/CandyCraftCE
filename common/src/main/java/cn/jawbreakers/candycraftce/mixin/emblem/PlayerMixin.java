package cn.jawbreakers.candycraftce.mixin.emblem;

import cn.jawbreakers.candycraftce.misc.EmblemHelper;
import cn.jawbreakers.candycraftce.mixin_stub.IEmblemDataHolder;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Created in 2026/11/1 00:08 by <a href="https://github.com/Bread-NiceCat">Bread_NiceCat</a>
 * Project: CandyCraftCE
 */
@Mixin(Player.class)
public abstract class PlayerMixin implements IEmblemDataHolder {
	@Shadow
	private Optional<GlobalPos> lastDeathLocation;
	@Unique
	private static final EntityDataAccessor<CompoundTag> candycraftce$EMBLEM_DATA = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
	@Unique
	private final String candycraftce$EMBLEM_DATA_TAG = "EmblemData";

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	public void defineSynchedData(CallbackInfo ci) {
		Player living = (Player) (Object) this;
		living.getEntityData().define(candycraftce$EMBLEM_DATA, new CompoundTag());
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void saveEmblemData(CompoundTag compound, CallbackInfo ci) {
		compound.put(candycraftce$EMBLEM_DATA_TAG, getCandycraftce_emblemData());
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void readEmblemData(CompoundTag compound, CallbackInfo ci) {
		setCandycraftce_emblemData(compound.getCompound(candycraftce$EMBLEM_DATA_TAG));
	}

	@Override
	@Unique
	public @NotNull CompoundTag getCandycraftce_emblemData() {
		Player self = (Player) (Object) this;
		return self.getEntityData().get(candycraftce$EMBLEM_DATA);
	}

	@Override
	@Unique
	public void setCandycraftce_emblemData(@NotNull CompoundTag compoundTag) {
		Player self = (Player) (Object) this;
		self.getEntityData().set(candycraftce$EMBLEM_DATA, compoundTag);
	}

	@Inject(method = "serverAiStep", at = @At("HEAD"))
	private void onServerAiStep(CallbackInfo ci) {
		Player self = (Player) (Object) this;
		EmblemHelper.INSTANCE.onServerTick(self);
	}
}
