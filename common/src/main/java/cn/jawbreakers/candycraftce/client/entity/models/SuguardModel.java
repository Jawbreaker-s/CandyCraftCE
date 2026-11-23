package cn.jawbreakers.candycraftce.client.entity.models;

import cn.jawbreakers.candycraftce.entity.Suguard;
import cn.jawbreakers.candycraftce.utils.CUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;

public class SuguardModel<T extends Suguard> extends EntityModel<T> implements ArmedModel {
	public static final ResourceLocation BASE = CUtils.prefix("suguard");
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(BASE, "main");
	ModelPart leg1;
	ModelPart leg2;
	ModelPart body;
	ModelPart head;
	ModelPart nose;
	ModelPart hatBrim;
	ModelPart earRight;
	ModelPart earLeft;
	ModelPart leftArm;
	ModelPart rightArm;
	ModelPart shield;
	ModelPart hatTop;

	public SuguardModel(ModelPart root) {
		leg1 = root.getChild("leg1");
		leg2 = root.getChild("leg2");
		body = root.getChild("body");
		head = root.getChild("head");
		nose = root.getChild("nose");
		hatBrim = root.getChild("hat_brim");
		earRight = root.getChild("ear_right");
		earLeft = root.getChild("ear_left");
		leftArm = root.getChild("left_arm");
		rightArm = root.getChild("right_arm");
		shield = leftArm.getChild("shield");
		hatTop = root.getChild("hat_top");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 2.0F), PartPose.offset(1.0F, 20.0F, -1.0F));
		root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 2.0F), PartPose.offset(-3.0F, 20.0F, -1.0F));
		root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, 0.0F, 0.0F, 6.0F, 6.0F, 4.0F), PartPose.offset(-3.0F, 14.0F, -2.0F));
		root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(0.0F, 11.0F, 0.0F));
		root.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 22).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 12.0F, 0.0F));
		root.addOrReplaceChild("hat_brim", CubeListBuilder.create().texOffs(12, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 1.0F, 4.0F), PartPose.offset(0.0F, 11.0F, 0.0F));
		root.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(4, 22).addBox(1.0F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 12.0F, 0.0F));
		root.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(4, 22).addBox(-2.0F, 2.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 10.0F, 0.0F));
		PartDefinition leftArm = root.addOrReplaceChild("left_arm",
				CubeListBuilder.create().texOffs(20, 6).addBox(0.0F, 0.0F, 0.0F, 1.0F, 4.0F, 2.0F),
				PartPose.offsetAndRotation(3.0F, 15.0F, 0.0F, -1.570796F, 0.0F, 0.0F));
		leftArm.addOrReplaceChild("shield",
				CubeListBuilder.create().texOffs(8, 16).addBox(0.0F, 0.0F, 0.0F, 5.0F, 5.0F, 1.0F),
				PartPose.offsetAndRotation(-2.0F, 5.0F, -1.5F, 1.570796F, 0.0F, 0.0F));
		root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(20, 6).addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 2.0F), PartPose.offsetAndRotation(-4.0F, 15.0F, 0.0F, -1.050296F, 0.0F, 0.0F));
		root.addOrReplaceChild("hat_top", CubeListBuilder.create().texOffs(28, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offset(0.0F, 10.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 32);
	}

	private void resetPose() {
		leg1.resetPose();
		leg2.resetPose();
		body.resetPose();
		head.resetPose();
		nose.resetPose();
		hatBrim.resetPose();
		earRight.resetPose();
		earLeft.resetPose();
		leftArm.resetPose();
		rightArm.resetPose();
		shield.resetPose();
		hatTop.resetPose();
	}


	@Override
	public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		float yaw = netHeadYaw * ((float) Math.PI / 180F);
		this.head.yRot = yaw;
		this.nose.yRot = yaw;
		this.hatBrim.yRot = yaw;
		this.hatTop.yRot = yaw;
		this.earRight.yRot = yaw;
		this.earLeft.yRot = yaw;

		this.leg1.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.leg2.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
		this.leg1.yRot = 0.0F;
		this.leg2.yRot = 0.0F;

		if (this.riding) {
			this.leg1.xRot = -1.5F;
			this.leg2.xRot = -1.5F;
		}
	}

	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		leg1.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		leg2.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		nose.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		hatBrim.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		earRight.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		earLeft.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		leftArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		rightArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		hatTop.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);

	}

	@Override
	public void translateToHand(@NotNull HumanoidArm side, @NotNull PoseStack poseStack) {
		ModelPart arm = switch (side) {
			case RIGHT -> rightArm;
			case LEFT -> leftArm;
		};
		arm.translateAndRotate(poseStack);
		poseStack.scale(0.45f, 0.45f, 0.45f);
	}
}