package cn.jawbreakers.candycraftce.fabric.mixin.fluid;

import cn.jawbreakers.candycraftce.CandyCraftCE;
import cn.jawbreakers.candycraftce.fabric.CandyCraftCEFabric;
import cn.jawbreakers.candycraftce.fluid.CFluidProperties;
import cn.jawbreakers.candycraftce.fluid.CFluidType;
import cn.jawbreakers.candycraftce.utils.CUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {
	@Unique
	private static final TagKey<Fluid> candycraftce$MOD_TAG = TagKey.create(Registries.FLUID, CUtils.INSTANCE.modLoc("fluid_tag", CandyCraftCE.MOD_ID));
	@Unique
	private CFluidProperties candycraftce$targetFluid = null;

	@Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At("TAIL"))
	private void onUpdateInWaterStateAndDoFluidPushing(CallbackInfoReturnable<Boolean> cir) {
		CandyCraftCEFabric.Companion.getInstance().getInitializedFluidProperties().forEach((prop) -> {
			CFluidType type = prop.getType();
			if (type.getCanPushEntity()) {
				candycraftce$targetFluid = prop;
				((Entity) (Object) this).updateFluidHeightAndDoFluidPushing(candycraftce$MOD_TAG, type.getMotionScale());
			}
		});
		candycraftce$targetFluid = null;
	}

	@WrapOperation(
			method = "updateFluidHeightAndDoFluidPushing(Lnet/minecraft/tags/TagKey;D)Z",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"
			))
	private boolean wrapIsFluid(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original) {
		if (tag == candycraftce$MOD_TAG) {
			return instance.is(candycraftce$targetFluid.getSource().invoke().get()) || instance.is(candycraftce$targetFluid.getFlowing().invoke().get());
		}
		return original.call(instance, tag);
	}


}
