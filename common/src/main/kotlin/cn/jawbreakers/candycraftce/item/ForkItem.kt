package cn.jawbreakers.candycraftce.item

import net.minecraft.world.item.TieredItem

class ForkItem(
    tier: net.minecraft.world.item.Tier,
    attackDamageModifier: Float,
    attackSpeedModifier: Float,
    properties: Properties,
) : TieredItem(tier, properties) {
//    private val defaultModifiers: Multimap<Attribute?, AttributeModifier?> =
//        ImmutableMultimap.builder<Attribute?, AttributeModifier?>()
//            .put(
//                Attributes.ATTACK_DAMAGE,
//                AttributeModifier(
//                    BASE_ATTACK_DAMAGE_UUID,
//                    "Weapon modifier",
//                    attackDamageModifier.toDouble(),
//                    AttributeModifier.Operation.ADDITION
//                )
//            )
//            .put(
//                Attributes.ATTACK_SPEED,
//                AttributeModifier(
//                    BASE_ATTACK_SPEED_UUID,
//                    "Weapon modifier",
//                    attackSpeedModifier.toDouble(),
//                    AttributeModifier.Operation.ADDITION
//                )
//            )
//            .build()
//
//    override fun getDefaultAttributeModifiers(slot: net.minecraft.world.entity.EquipmentSlot): Multimap<Attribute?, AttributeModifier?> {
//        return if (slot == net.minecraft.world.entity.EquipmentSlot.MAINHAND) defaultModifiers else super.getDefaultAttributeModifiers(
//            slot
//        )
//    }
//
////    public override fun initializeClient(consumer: java.util.function.Consumer<IClientItemExtensions?>) {
////        consumer.accept(object : IClientItemExtensions() {
////            private var renderer: BlockEntityWithoutLevelRenderer? = null
////
////            val customRenderer: BlockEntityWithoutLevelRenderer
////                get() {
////                    if (renderer == null) {
////                        renderer = ForkItemRenderer(
////                            net.minecraft.client.Minecraft.getInstance().blockEntityRenderDispatcher
////                        )
////                    }
////                    return renderer
////                }
////
////            public override fun getArmPose(
////                entity: LivingEntity?,
////                hand: InteractionHand?,
////                stack: ItemStack?,
////            ): HumanoidModel.ArmPose {
////                return com.valentin4311.candycraftmod.client.ForkClientAnimations.getArmPose(stack)
////            }
////
////            public override fun applyForgeHandTransform(
////                poseStack: com.mojang.blaze3d.vertex.PoseStack?,
////                player: net.minecraft.client.player.LocalPlayer?,
////                arm: net.minecraft.world.entity.HumanoidArm?,
////                stack: ItemStack?,
////                partialTick: Float,
////                equipProcess: Float,
////                swingProcess: Float,
////            ): Boolean {
////                return com.valentin4311.candycraftmod.client.ForkClientAnimations.applyFirstPersonTransform(
////                    poseStack, arm, stack, partialTick
////                )
////            }
////        })
//    }
//    public override fun onBlockStartBreak(
//        stack: ItemStack,
//        pos: net.minecraft.core.BlockPos,
//        player: net.minecraft.world.entity.player.Player,
//    ): Boolean {
//        if (!player.isShiftKeyDown) {
//            return false
//        }
//        val level = player.level()
//        val state = level.getBlockState(pos)
//        if (isForkProtected(level, pos, state)) {
//            return true
//        }
//        if (!canForkEat(stack, state)) {
//            return false
//        }
//        if (hasHeldBlock(stack) || player.cooldowns.isOnCooldown(this)) {
//            return true
//        }
//        pickUpBlock(stack, level, pos, player, state)
//        return true
//    }
//
//    override fun useOn(context: net.minecraft.world.item.context.UseOnContext): net.minecraft.world.InteractionResult {
//        val player = context.player
//        val stack = context.itemInHand
//        val level = context.level
//        if (player == null) {
//            return net.minecraft.world.InteractionResult.PASS
//        }
//        if (isEatAnimationPlaying(stack)) {
//            return net.minecraft.world.InteractionResult.FAIL
//        }
//        if (stack.damageValue >= stack.maxDamage - 1) {
//            return net.minecraft.world.InteractionResult.FAIL
//        }
//
//        val pos = context.clickedPos
//        val state = level.getBlockState(pos)
//        if (beginForkingBlock(stack, level, pos, player, context.hand, state)) {
//            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide)
//        }
//
//        clearPendingBlock(stack)
//        player.startUsingItem(context.hand)
//        return net.minecraft.world.InteractionResult.CONSUME
//    }
//
//    override fun use(
//        level: net.minecraft.world.level.Level,
//        player: net.minecraft.world.entity.player.Player,
//        hand: InteractionHand,
//    ): net.minecraft.world.InteractionResultHolder<ItemStack?> {
//        val stack = player.getItemInHand(hand)
//        if (isEatAnimationPlaying(stack)) {
//            return net.minecraft.world.InteractionResultHolder.fail<ItemStack?>(stack)
//        }
//        if (stack.damageValue >= stack.maxDamage - 1) {
//            return net.minecraft.world.InteractionResultHolder.fail<ItemStack?>(stack)
//        }
//        clearPendingBlock(stack)
//        player.startUsingItem(hand)
//        return net.minecraft.world.InteractionResultHolder.consume<ItemStack?>(stack)
//    }
//
//    override fun releaseUsing(
//        stack: ItemStack,
//        level: net.minecraft.world.level.Level,
//        entity: LivingEntity,
//        timeLeft: Int,
//    ) {
//        if (entity !is net.minecraft.world.entity.player.Player) {
//            return
//        }
//        if (isEatAnimationPlaying(stack)) {
//            return
//        }
//        val usedTicks = getUseDuration(stack) - timeLeft
//        val hand = entity.usedItemHand
//        if (hasHeldBlock(stack)) {
//            clearPendingBlock(stack)
//            if (usedTicks < THROW_CHARGE_TICKS) {
//                startEatAnimation(stack)
//                if (!level.isClientSide) {
//                    eatOneBite(stack, entity, hand)
//                }
//            } else if (!level.isClientSide) {
//                throwHeldBlock(stack, entity, hand)
//            }
//            return
//        }
//
//        if (usedTicks < THROW_CHARGE_TICKS) {
//            val pendingPos: net.minecraft.core.BlockPos? = getPendingBlock(stack)
//            if (!level.isClientSide && pendingPos != null) {
//                val pendingState = level.getBlockState(pendingPos)
//                tryPickUpBlock(stack, level, pendingPos, entity, pendingState)
//            }
//            clearPendingBlock(stack)
//            return
//        }
//
//        clearPendingBlock(stack)
//        if (!level.isClientSide) {
//            val thrownStack = stack.copy()
//            thrownStack.count = 1
//            stack.hurtAndBreak<net.minecraft.world.entity.player.Player?>(
//                1,
//                entity,
//                java.util.function.Consumer { living: net.minecraft.world.entity.player.Player? ->
//                    living!!.broadcastBreakEvent(hand)
//                })
//            val fork: ThrownForkEntity = ThrownForkEntity(level, entity, thrownStack)
//            fork.shootFromRotation(entity, entity.xRot, entity.yRot, 0.0f, 2.5f, 1.0f)
//            if (entity.abilities.instabuild) {
//                fork.pickup = net.minecraft.world.entity.projectile.AbstractArrow.Pickup.CREATIVE_ONLY
//            }
//            if (level.addFreshEntity(fork) && entity is net.minecraft.server.level.ServerPlayer) {
//                CCCriteriaTriggers.THROW_FORK.trigger(player)
//            }
//            level.playSound(
//                null,
//                fork,
//                net.minecraft.sounds.SoundEvents.TRIDENT_THROW,
//                net.minecraft.sounds.SoundSource.PLAYERS,
//                1.0f,
//                1.0f
//            )
//            if (!entity.abilities.instabuild) {
//                stack.shrink(1)
//            }
//        }
//    }
//
//    override fun getUseDuration(stack: ItemStack): Int {
//        return 72000
//    }
//
//    override fun getUseAnimation(stack: ItemStack): net.minecraft.world.item.UseAnim {
//        return net.minecraft.world.item.UseAnim.SPEAR
//    }
//
//    override fun appendHoverText(
//        stack: ItemStack,
//        level: net.minecraft.world.level.Level?,
//        tooltip: MutableList<net.minecraft.network.chat.Component?>,
//        flag: net.minecraft.world.item.TooltipFlag,
//    ) {
//        tooltip.add(
//            net.minecraft.network.chat.Component.translatable("tooltip.candycraftmod.fork.pick_up")
//                .withStyle(net.minecraft.ChatFormatting.GRAY)
//        )
//        tooltip.add(
//            net.minecraft.network.chat.Component.translatable("tooltip.candycraftmod.fork.use")
//                .withStyle(net.minecraft.ChatFormatting.GRAY)
//        )
//    }
//
//    override fun isEnchantable(stack: ItemStack): Boolean {
//        return true
//    }
//
//    override fun getEnchantmentValue(): Int {
//        return tier.enchantmentValue
//    }
//
//    public override fun canApplyAtEnchantingTable(
//        stack: ItemStack?,
//        enchantment: net.minecraft.world.item.enchantment.Enchantment?,
//    ): Boolean {
//        if (enchantment === CCEnchantments.HONEY_SOURCE.get()) {
//            return true
//        }
//        if (enchantment === CCEnchantments.DEVOURING.get()) {
//            return true
//        }
//        if (enchantment === CCEnchantments.GLUTTONY.get()) {
//            return false
//        }
//        val configured: Boolean? = CCToolProperties.configuredEnchantmentRule(stack, enchantment)
//        return if (configured != null)
//            configured
//        else
//            enchantment === net.minecraft.world.item.enchantment.Enchantments.UNBREAKING || enchantment === net.minecraft.world.item.enchantment.Enchantments.MENDING
//    }
//
//    override fun getTooltipImage(stack: ItemStack): java.util.Optional<net.minecraft.world.inventory.tooltip.TooltipComponent?> {
//        val state: net.minecraft.world.level.block.state.BlockState = getHeldBlockState(stack)
//        return if (state.isAir) java.util.Optional.empty<net.minecraft.world.inventory.tooltip.TooltipComponent?>() else java.util.Optional.of<net.minecraft.world.inventory.tooltip.TooltipComponent?>(
//            ForkHeldBlockTooltip(state)
//        )
//    }
//
//    override fun inventoryTick(
//        stack: ItemStack,
//        level: net.minecraft.world.level.Level,
//        entity: net.minecraft.world.entity.Entity,
//        slot: Int,
//        selected: Boolean,
//    ) {
//        super.inventoryTick(stack, level, entity, slot, selected)
//        val tag = stack.tag
//        if (tag == null || !tag.contains(EAT_ANIMATION_TAG)) {
//            return
//        }
//        val remaining = tag.getInt(EAT_ANIMATION_TAG) - 1
//        if (remaining > 0) {
//            tag.putInt(EAT_ANIMATION_TAG, remaining)
//        } else {
//            tag.remove(EAT_ANIMATION_TAG)
//            if (tag.isEmpty) {
//                stack.setTag(null)
//            }
//        }
//    }
//
//    companion object {
//        const val HELD_BLOCK_TAG: String = "CandyForkBlock"
//        private const val HELD_BLOCK_STATE_TAG = "CandyForkBlockState"
//        private const val EAT_BITES_TAG = "CandyForkEatBites"
//        private const val EAT_ANIMATION_TAG = "CandyForkEatAnimation"
//        private const val PENDING_BLOCK_TAG = "CandyForkPendingBlock"
//        private const val BITES_TO_EAT = 4
//        private const val THROW_CHARGE_TICKS = 10
//        private const val HELD_BLOCK_DURABILITY_COST = 3
//        const val EAT_ANIMATION_TICKS: Int = 12
//        val FORK_EDIBLE: net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block?> =
//            net.minecraft.tags.BlockTags.create(
//                net.minecraft.resources.ResourceLocation(
//                    CandyCraft.MODID,
//                    "fork_edible"
//                )
//            )
//
//        fun hasHeldBlock(stack: ItemStack): Boolean {
//            val tag = stack.tag
//            return tag != null && tag.contains(HELD_BLOCK_TAG)
//        }
//
//        fun getHeldBlockState(stack: ItemStack): net.minecraft.world.level.block.state.BlockState {
//            val tag = stack.tag
//            if (tag == null || !tag.contains(HELD_BLOCK_TAG)) {
//                return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
//            }
//            if (tag.contains(HELD_BLOCK_STATE_TAG, net.minecraft.nbt.Tag.TAG_COMPOUND.toInt())) {
//                return net.minecraft.nbt.NbtUtils.readBlockState(
//                    net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(),
//                    tag.getCompound(HELD_BLOCK_STATE_TAG)
//                )
//            }
//            val id = net.minecraft.resources.ResourceLocation.tryParse(tag.getString(HELD_BLOCK_TAG))
//            if (id == null) {
//                return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
//            }
//            return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOptional(id)
//                .map<net.minecraft.world.level.block.state.BlockState>(java.util.function.Function { obj: net.minecraft.world.level.block.Block? -> obj!!.defaultBlockState() })
//                .orElse(net.minecraft.world.level.block.Blocks.AIR.defaultBlockState())
//        }
//
//        fun getEatProgress(stack: ItemStack): Float {
//            return kotlin.math.min(
//                1.0f,
//                getEatBites(stack) / BITES_TO_EAT.toFloat()
//            )
//        }
//
//        fun getEatAnimationTicks(stack: ItemStack): Int {
//            val tag = stack.tag
//            return if (tag == null) 0 else kotlin.math.max(0, tag.getInt(EAT_ANIMATION_TAG))
//        }
//
//        fun isEatAnimationPlaying(stack: ItemStack): Boolean {
//            return getEatAnimationTicks(stack) > 0
//        }
//
//        fun tryPickUpBlock(
//            stack: ItemStack,
//            level: net.minecraft.world.level.Level,
//            pos: net.minecraft.core.BlockPos,
//            player: net.minecraft.world.entity.player.Player,
//            state: net.minecraft.world.level.block.state.BlockState,
//        ): Boolean {
//            if (stack.damageValue >= stack.maxDamage - 1 || !player.isShiftKeyDown || hasHeldBlock(
//                    stack
//                )
//                || !player.mayBuild() || player.cooldowns.isOnCooldown(stack.item)
//                || !canForkEat(stack, state) || isForkProtected(level, pos, state)
//            ) {
//                return false
//            }
//            if (!level.isClientSide) {
//                pickUpBlock(stack, level, pos, player, state)
//            }
//            return true
//        }
//
//        fun beginForkingBlock(
//            stack: ItemStack,
//            level: net.minecraft.world.level.Level,
//            pos: net.minecraft.core.BlockPos,
//            player: net.minecraft.world.entity.player.Player,
//            hand: InteractionHand,
//            state: net.minecraft.world.level.block.state.BlockState,
//        ): Boolean {
//            if (isEatAnimationPlaying(stack)
//                || stack.damageValue >= stack.maxDamage - 1 || !player.isShiftKeyDown || hasHeldBlock(
//                    stack
//                )
//                || !player.mayBuild() || player.cooldowns.isOnCooldown(stack.item)
//                || !canForkEat(stack, state) || isForkProtected(level, pos, state)
//            ) {
//                return false
//            }
//            stack.getOrCreateTag().putLong(PENDING_BLOCK_TAG, pos.asLong())
//            player.startUsingItem(hand)
//            return true
//        }
//
//        private fun isForkProtected(
//            level: net.minecraft.world.level.Level,
//            pos: net.minecraft.core.BlockPos,
//            state: net.minecraft.world.level.block.state.BlockState,
//        ): Boolean {
//            return CCDimensions.isDungeon(level)
//                    || DungeonTeleporterBlock.isProtectedSupport(level, pos)
//                    || state.getDestroySpeed(level, pos) < 0.0f
//        }
//
//        private fun eatOneBite(
//            stack: ItemStack,
//            player: net.minecraft.world.entity.player.Player,
//            hand: InteractionHand,
//        ) {
//            val bites: Int = getEatBites(stack) + 1
//            stack.getOrCreateTag().putInt(EAT_BITES_TAG, bites)
//            player.level().playSound(
//                null,
//                player.blockPosition(),
//                net.minecraft.sounds.SoundEvents.GENERIC_EAT,
//                net.minecraft.sounds.SoundSource.PLAYERS,
//                0.45f,
//                0.9f + player.getRandom().nextFloat() * 0.2f
//            )
//            spawnHeldBlockParticles(stack, player, hand, 10)
//            if (bites >= BITES_TO_EAT) {
//                finishEating(stack, player, hand)
//            }
//        }
//
//        private fun startEatAnimation(stack: ItemStack) {
//            stack.getOrCreateTag().putInt(EAT_ANIMATION_TAG, EAT_ANIMATION_TICKS)
//        }
//
//        private fun throwHeldBlock(
//            stack: ItemStack,
//            player: net.minecraft.world.entity.player.Player,
//            hand: InteractionHand,
//        ) {
//            val state: net.minecraft.world.level.block.state.BlockState = getHeldBlockState(stack)
//            val shattersOnImpact = getEatBites(stack) > 0
//            clearHeldBlock(stack)
//            if (state.isAir) {
//                return
//            }
//
//            val projectile: ThrownForkBlockEntity = ThrownForkBlockEntity(
//                player.level(), player, state, shattersOnImpact
//            )
//            projectile.shootFromRotation(player, player.xRot, player.yRot, 0.0f, 1.5f, 0.25f)
//            player.level().addFreshEntity(projectile)
//            player.level().playSound(
//                null,
//                projectile,
//                net.minecraft.sounds.SoundEvents.TRIDENT_THROW,
//                net.minecraft.sounds.SoundSource.PLAYERS,
//                0.8f,
//                1.15f
//            )
//            if (!player.abilities.instabuild) {
//                stack.hurtAndBreak<net.minecraft.world.entity.player.Player?>(
//                    HELD_BLOCK_DURABILITY_COST,
//                    player,
//                    java.util.function.Consumer { living: net.minecraft.world.entity.player.Player? ->
//                        living!!.broadcastBreakEvent(hand)
//                    })
//            }
//        }
//
//        private fun finishEating(
//            stack: ItemStack,
//            player: net.minecraft.world.entity.player.Player,
//            hand: InteractionHand,
//        ) {
//            if (player.level() is net.minecraft.server.level.ServerLevel) {
//                spawnHeldBlockParticles(stack, player, hand, 18)
//                player.getFoodData().eat(1, 0.0f)
//                val devouringLevel = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
//                    CCEnchantments.DEVOURING.get(),
//                    stack
//                )
//                if (devouringLevel > 0) {
//                    player.heal(2.0f + kotlin.math.min(3, devouringLevel))
//                }
//                if (player is net.minecraft.server.level.ServerPlayer) {
//                    CCCriteriaTriggers.EAT_BLOCK.trigger(player)
//                }
//                player.level().playSound(
//                    null,
//                    player.blockPosition(),
//                    net.minecraft.sounds.SoundEvents.PLAYER_BURP,
//                    net.minecraft.sounds.SoundSource.PLAYERS,
//                    0.5f,
//                    0.9f + player.getRandom().nextFloat() * 0.1f
//                )
//                if (!player.abilities.instabuild) {
//                    stack.hurtAndBreak<net.minecraft.world.entity.player.Player?>(
//                        HELD_BLOCK_DURABILITY_COST,
//                        player,
//                        java.util.function.Consumer { living: net.minecraft.world.entity.player.Player? ->
//                            living!!.broadcastBreakEvent(hand)
//                        })
//                }
//            }
//            clearHeldBlock(stack)
//            player.cooldowns.addCooldown(stack.item, 8)
//        }
//
//        private fun spawnHeldBlockParticles(
//            stack: ItemStack,
//            player: net.minecraft.world.entity.player.Player,
//            hand: InteractionHand?,
//            count: Int,
//        ) {
//            val state: net.minecraft.world.level.block.state.BlockState = getHeldBlockState(stack)
//            if (state.isAir || player.level() !is net.minecraft.server.level.ServerLevel) {
//                return
//            }
//            val view = player.getViewVector(1.0f)
//            var horizontalRight = net.minecraft.world.phys.Vec3(view.z, 0.0, -view.x)
//            if (horizontalRight.lengthSqr() > 1.0E-6) {
//                horizontalRight = horizontalRight.normalize()
//            }
//            val usedArm = if (hand == InteractionHand.MAIN_HAND)
//                player.mainArm
//            else
//                player.mainArm.opposite
//            val handSide = if (usedArm == net.minecraft.world.entity.HumanoidArm.RIGHT) 1.0 else -1.0
//            val particlePos = player.eyePosition
//                .add(view.scale(0.42))
//                .add(horizontalRight.scale(0.08 * handSide))
//                .add(0.0, -0.12, 0.0)
//            serverLevel.sendParticles<net.minecraft.core.particles.BlockParticleOption?>(
//                net.minecraft.core.particles.BlockParticleOption(
//                    net.minecraft.core.particles.ParticleTypes.BLOCK,
//                    state
//                ),
//                particlePos.x,
//                particlePos.y,
//                particlePos.z,
//                count,
//                0.12,
//                0.06,
//                0.12,
//                0.025
//            )
//        }
//
//        private fun canForkEat(
//            stack: ItemStack,
//            state: net.minecraft.world.level.block.state.BlockState,
//        ): Boolean {
//            if (state.isAir) {
//                return false
//            }
//            if (net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
//                    CCEnchantments.GLUTTONY.get(),
//                    stack
//                ) > 0
//            ) {
//                return true
//            }
//            if (state.`is`(CCBlocks.JAW_BREAKER_BLOCK.get()) || state.`is`(CCBlocks.JAW_BREAKER_LIGHT.get())) {
//                return false
//            }
//            // Every candycraftmod block is edible by default so newly registered
//            // blocks work without touching the tag. Unbreakable blocks (bedrock
//            // hardness, portals, dungeon locks) are rejected by isForkProtected.
//            val blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.block)
//            if (blockId != null && CandyCraft.MODID.equals(blockId.namespace)) {
//                return true
//            }
//            // The data tag remains the opt-in mechanism for other namespaces
//            // (vanilla honey block, cake, mushroom blocks, ...).
//            return state.`is`(FORK_EDIBLE)
//        }
//
//        private fun pickUpBlock(
//            stack: ItemStack,
//            level: net.minecraft.world.level.Level,
//            pos: net.minecraft.core.BlockPos,
//            player: net.minecraft.world.entity.player.Player,
//            state: net.minecraft.world.level.block.state.BlockState,
//        ) {
//            if (level.isClientSide || (level !is net.minecraft.server.level.ServerLevel) || !player.mayBuild()) {
//                return
//            }
//            storeHeldBlock(stack, state)
//            resetEatBites(stack)
//            removeForkedBlock(level, pos, player, state)
//            level.playSound(
//                null,
//                pos,
//                state.getSoundType(level, pos, player).breakSound,
//                net.minecraft.sounds.SoundSource.BLOCKS,
//                0.6f,
//                1.15f
//            )
//            player.cooldowns.addCooldown(stack.item, 4)
//        }
//
//        private fun removeForkedBlock(
//            level: net.minecraft.server.level.ServerLevel,
//            pos: net.minecraft.core.BlockPos,
//            player: net.minecraft.world.entity.player.Player?,
//            state: net.minecraft.world.level.block.state.BlockState,
//        ) {
//            dropBlockEntityContents(level, pos)
//            if (state.block is net.minecraft.world.level.block.DoorBlock) {
//                val lowerPos =
//                    if (state.getValue<net.minecraft.world.level.block.state.properties.DoubleBlockHalf?>(net.minecraft.world.level.block.DoorBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER) pos else pos.below()
//                val upperPos = lowerPos.above()
//                val lowerState = level.getBlockState(lowerPos)
//                val upperState = level.getBlockState(upperPos)
//                emitForkBreak(level, lowerPos, player, lowerState)
//                emitForkBreak(level, upperPos, player, upperState)
//                level.removeBlock(upperPos, false)
//                level.removeBlock(lowerPos, false)
//                return
//            }
//
//            emitForkBreak(level, pos, player, state)
//            level.removeBlock(pos, false)
//        }
//
//        private fun dropBlockEntityContents(
//            level: net.minecraft.server.level.ServerLevel,
//            pos: net.minecraft.core.BlockPos,
//        ) {
//            val blockEntity = level.getBlockEntity(pos)
//            if (blockEntity == null) {
//                return
//            }
//
//            if (blockEntity is net.minecraft.world.Container) {
//                for (slot in 0..<blockEntity.containerSize) {
//                    val removed = blockEntity.removeItemNoUpdate(slot)
//                    if (!removed.isEmpty) {
//                        net.minecraft.world.Containers.dropItemStack(
//                            level,
//                            pos.x + 0.5,
//                            pos.y + 0.5,
//                            pos.z + 0.5,
//                            removed
//                        )
//                    }
//                }
//                blockEntity.setChanged()
//                return
//            }
//
//            blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent({ handler ->
//                for (slot in 0..<handler.getSlots()) {
//                    val removed: ItemStack =
//                        handler.extractItem(slot, Int.MAX_VALUE, false)
//                    if (!removed.isEmpty) {
//                        net.minecraft.world.Containers.dropItemStack(
//                            level,
//                            pos.x + 0.5,
//                            pos.y + 0.5,
//                            pos.z + 0.5,
//                            removed
//                        )
//                    }
//                }
//            })
//        }
//
//        private fun emitForkBreak(
//            level: net.minecraft.server.level.ServerLevel,
//            pos: net.minecraft.core.BlockPos,
//            player: net.minecraft.world.entity.player.Player?,
//            state: net.minecraft.world.level.block.state.BlockState,
//        ) {
//            if (state.isAir) {
//                return
//            }
//            level.levelEvent(2001, pos, net.minecraft.world.level.block.Block.getId(state))
//            level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.BLOCK_DESTROY, pos)
//        }
//
//        private fun storeHeldBlock(
//            stack: ItemStack,
//            state: net.minecraft.world.level.block.state.BlockState,
//        ) {
//            val id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.block)
//            stack.getOrCreateTag().putString(HELD_BLOCK_TAG, id.toString())
//            stack.getOrCreateTag()
//                .put(HELD_BLOCK_STATE_TAG, net.minecraft.nbt.NbtUtils.writeBlockState(state))
//        }
//
//        fun clearHeldBlock(stack: ItemStack) {
//            val tag = stack.tag
//            if (tag == null) {
//                return
//            }
//            tag.remove(HELD_BLOCK_TAG)
//            tag.remove(HELD_BLOCK_STATE_TAG)
//            tag.remove(EAT_BITES_TAG)
//            if (tag.isEmpty) {
//                stack.setTag(null)
//            }
//        }
//
//        private fun getPendingBlock(stack: ItemStack): net.minecraft.core.BlockPos? {
//            val tag = stack.tag
//            return if (tag != null && tag.contains(PENDING_BLOCK_TAG)) net.minecraft.core.BlockPos.of(
//                tag.getLong(PENDING_BLOCK_TAG)
//            ) else null
//        }
//
//        private fun clearPendingBlock(stack: ItemStack) {
//            val tag = stack.tag
//            if (tag == null) {
//                return
//            }
//            tag.remove(PENDING_BLOCK_TAG)
//            if (tag.isEmpty) {
//                stack.setTag(null)
//            }
//        }
//
//        private fun resetEatBites(stack: ItemStack) {
//            val tag = stack.tag
//            if (tag != null) {
//                tag.putInt(EAT_BITES_TAG, 0)
//            }
//        }
//
//        private fun getEatBites(stack: ItemStack): Int {
//            val tag = stack.tag
//            return if (tag == null) 0 else tag.getInt(EAT_BITES_TAG)
//        }
//    }
}
