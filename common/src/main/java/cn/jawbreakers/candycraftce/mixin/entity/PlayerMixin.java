package cn.jawbreakers.candycraftce.mixin.entity;

import cn.jawbreakers.candycraftce.mixin_stub.ICandyBossTarget;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

/**
 * Created in 2026/10/9 20:03 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@Mixin(Player.class)
public class PlayerMixin implements ICandyBossTarget {
	@Unique
	private static final EntityDataAccessor<Optional<UUID>> candycraftce$BOSS_SOURCE = SynchedEntityData.defineId(Player.class, EntityDataSerializers.OPTIONAL_UUID);

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	public void defineSynchedData(CallbackInfo ci) {
		Player player = (Player) (Object) this;
		player.getEntityData().define(candycraftce$BOSS_SOURCE, Optional.empty());
	}

	@Override
	public @Nullable UUID getCandycraftce_bossSource() {
		Player player = (Player) (Object) this;
		return player.getEntityData().get(candycraftce$BOSS_SOURCE).orElse(null);
	}

	@Override
	public void setCandycraftce_bossSource(@Nullable UUID uuid) {
		Player player = (Player) (Object) this;
		player.getEntityData().set(candycraftce$BOSS_SOURCE, Optional.ofNullable(uuid));
	}
}
