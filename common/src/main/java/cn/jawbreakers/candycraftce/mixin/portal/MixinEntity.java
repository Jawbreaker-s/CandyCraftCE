package cn.jawbreakers.candycraftce.mixin.portal;

import cn.jawbreakers.candycraftce.registry.worldgen.CLevels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created in 2026/9/20 23:56 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 * Description:
 * A Mixin to manage entity's in portal behavior
 */
@Mixin(Entity.class)
public abstract class MixinEntity {

	@Shadow
	public abstract Level level();

	@Shadow
	public abstract float getXRot();

	@Shadow
	public abstract float getYRot();

	@Shadow
	public abstract BlockPos blockPosition();

	@Shadow
	public abstract Vec3 position();

	@Shadow
	public abstract Vec3 getDeltaMovement();

	@Shadow
	@Final
	protected RandomSource random;


	@Inject(method = "findDimensionEntryPoint",
			at = @At("HEAD"),
			cancellable = true
	)
	private void findDimensionEntryPoint(@NotNull ServerLevel destination, CallbackInfoReturnable<PortalInfo> cir) {
		ResourceKey<Level> from = level().dimension();
		ResourceKey<Level> dest = destination.dimension();
		if (from == Level.OVERWORLD && dest == CLevels.candyland) {
			cir.setReturnValue(new PortalInfo(position().with(Direction.Axis.Y, 336f), getDeltaMovement(), getXRot(), getYRot()));
		}
		if (from == CLevels.candyland && dest == Level.OVERWORLD) {
			BlockPos pos1 = destination.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPosition());
			cir.setReturnValue(new PortalInfo(Vec3.atCenterOf(pos1), getDeltaMovement(), this.getXRot(), this.getYRot()));
		}

	}

}
