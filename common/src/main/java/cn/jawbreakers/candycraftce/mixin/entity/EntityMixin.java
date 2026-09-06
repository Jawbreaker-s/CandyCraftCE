package cn.jawbreakers.candycraftce.mixin.entity;

import cn.jawbreakers.candycraftce.mixin_stub.IPurpleJellyStuckEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityMixin implements IPurpleJellyStuckEntity {
	@Unique
	private boolean candycraftce$purpleJellyStuck;

	@Unique
	@Override
	public void candycraftce$setPurpleJellyStuck() {
		candycraftce$purpleJellyStuck = true;
	}

	@ModifyVariable(
			method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
			at = @At("HEAD"),
			argsOnly = true
	)
	private Vec3 applyPurpleJellyStuckMovement(Vec3 movement) {
		if (!candycraftce$purpleJellyStuck) {
			return movement;
		}
		candycraftce$purpleJellyStuck = false;
		Entity entity = (Entity) (Object) this;
		entity.setDeltaMovement(Vec3.ZERO);
		return movement.multiply(0.25D, 0.05D, 0.25D);
	}
}
