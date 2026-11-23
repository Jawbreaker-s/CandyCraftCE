package cn.jawbreakers.candycraftce.item

import net.minecraft.world.item.Item

class JellyDungeonKeyItem(
    properties: Properties,
    suguardDungeon: Boolean = false,
) : Item(properties) {
//    private val kind: DungeonKind
//
//    init {
//        this.kind = if (suguardDungeon) DungeonKind.SUGUARD else DungeonKind.JELLY
//    }
//
//    override fun useOn(context: net.minecraft.world.item.context.UseOnContext): net.minecraft.world.InteractionResult {
//        val level = context.getLevel()
//        if (context.getClickedFace() != net.minecraft.core.Direction.UP || context.getPlayer() == null) {
//            return net.minecraft.world.InteractionResult.FAIL
//        }
//        val supportPos = context.getClickedPos()
//        if (!level.getBlockState(supportPos).isCollisionShapeFullBlock(level, supportPos)) {
//            return net.minecraft.world.InteractionResult.FAIL
//        }
//        val pos = supportPos.above()
//        val replaced = level.getBlockState(pos)
//        if (!replaced.canBeReplaced()) {
//            return net.minecraft.world.InteractionResult.FAIL
//        }
//        if (level.isClientSide) {
//            return net.minecraft.world.InteractionResult.SUCCESS
//        }
//
//        if (context.getPlayer() !is net.minecraft.server.level.ServerPlayer || level !is net.minecraft.server.level.ServerLevel) {
//            return net.minecraft.world.InteractionResult.FAIL
//        }
//        val stack = context.getItemInHand()
//        refreshState(stack, player)
//        val tag = stack.getOrCreateTag()
//        if (tag.getBoolean(JellyDungeonKeyItem.Companion.EXHAUSTED_TAG)) {
//            player.displayClientMessage(
//                net.minecraft.network.chat.Component.translatable("message.candycraftmod.dungeon.key_exhausted"),
//                true
//            )
//            return net.minecraft.world.InteractionResult.FAIL
//        }
//
//        val dungeonLevel: net.minecraft.server.level.ServerLevel? = player.server.getLevel(
//            if (kind === DungeonKind.JELLY) CCDimensions.JELLY_DUNGEON else CCDimensions.SUGUARD_DUNGEON
//        )
//        if (dungeonLevel == null) {
//            return net.minecraft.world.InteractionResult.FAIL
//        }
//        val data: DungeonProgressData = DungeonProgressData.get(player.server)
//        val active: Instance? = data.getActive(player.getUUID(), kind)
//        val bound = tag.contains(JellyDungeonKeyItem.Companion.INSTANCE_TAG)
//        if (bound && (tag.hasUUID(JellyDungeonKeyItem.Companion.OWNER_TAG) && tag.getUUID(JellyDungeonKeyItem.Companion.OWNER_TAG) != player.getUUID() || active == null || active.id() !== tag.getLong(
//                JellyDungeonKeyItem.Companion.INSTANCE_TAG
//            ))
//        ) {
//            tag.putBoolean(JellyDungeonKeyItem.Companion.EXHAUSTED_TAG, true)
//            player.displayClientMessage(
//                net.minecraft.network.chat.Component.translatable("message.candycraftmod.dungeon.key_exhausted"),
//                true
//            )
//            return net.minecraft.world.InteractionResult.FAIL
//        }
//        if (active != null && data.hasLiveEntrancePortal(player.server, player.getUUID(), kind, active.id())) {
//            tag.putBoolean(JellyDungeonKeyItem.Companion.PORTAL_PLACED_TAG, true)
//            player.displayClientMessage(
//                net.minecraft.network.chat.Component.translatable("message.candycraftmod.dungeon.portal_already_open"),
//                true
//            )
//            return net.minecraft.world.InteractionResult.FAIL
//        }
//
//        val instance: Instance = if (active == null) data.getOrCreate(player, kind) else active
//        if (!level.setBlock(
//                pos,
//                DungeonTeleporterBlock.state(kind, PortalRole.ENTRY),
//                net.minecraft.world.level.block.CBlockTags.UPDATE_ALL
//            )
//        ) {
//            return net.minecraft.world.InteractionResult.FAIL
//        }
//        JellyDungeonKeyItem.Companion.bind(
//            stack,
//            player,
//            instance,
//            data.getCompletionCount(player.getUUID(), kind),
//            true
//        )
//        data.registerPortal(level, pos, player.getUUID(), kind, instance.id())
//        level.playSound(
//            null,
//            pos,
//            net.minecraft.sounds.SoundEvents.PORTAL_TRIGGER,
//            net.minecraft.sounds.SoundSource.BLOCKS,
//            1.0f,
//            1.25f
//        )
//        player.displayClientMessage(
//            net.minecraft.network.chat.Component.translatable("message.candycraftmod.dungeon.portal_opened"),
//            true
//        )
//        return net.minecraft.world.InteractionResult.CONSUME
//    }
//
//    fun recoverPortal(
//        stack: net.minecraft.world.item.ItemStack,
//        player: net.minecraft.server.level.ServerPlayer,
//        portal: PortalRecord
//    ): kotlin.Boolean {
//        if (portal.kind() !== kind || !portal.owner().equals(player.getUUID())) {
//            return false
//        }
//        val data: DungeonProgressData = DungeonProgressData.get(player.server)
//        val active: Instance? = data.getActive(player.getUUID(), kind)
//        if (active == null || active.id() !== portal.instanceId()) {
//            return false
//        }
//        JellyDungeonKeyItem.Companion.bind(
//            stack,
//            player,
//            active,
//            data.getCompletionCount(player.getUUID(), kind),
//            false
//        )
//        return true
//    }
//
//    fun matchesDungeon(dungeonKind: DungeonKind?): kotlin.Boolean {
//        return kind === dungeonKind
//    }
//
//    override fun inventoryTick(
//        stack: net.minecraft.world.item.ItemStack,
//        level: net.minecraft.world.level.Level,
//        entity: net.minecraft.world.entity.Entity,
//        slot: kotlin.Int,
//        selected: kotlin.Boolean
//    ) {
//        super.inventoryTick(stack, level, entity, slot, selected)
//        if (!level.isClientSide && entity is net.minecraft.server.level.ServerPlayer && entity.tickCount % 10 == 0) {
//            refreshState(stack, entity)
//        }
//    }
//
//    private fun refreshState(
//        stack: net.minecraft.world.item.ItemStack,
//        player: net.minecraft.server.level.ServerPlayer
//    ) {
//        val data: DungeonProgressData = DungeonProgressData.get(player.server)
//        val tag = stack.getOrCreateTag()
//        val active: Instance? = data.getActive(player.getUUID(), kind)
//        val bound = tag.contains(JellyDungeonKeyItem.Companion.INSTANCE_TAG)
//        val exhausted =
//            bound && (active == null || active.id() !== tag.getLong(JellyDungeonKeyItem.Companion.INSTANCE_TAG))
//        val portalPlaced = bound && !exhausted && data.hasLiveEntrancePortal(
//            player.server,
//            player.getUUID(),
//            kind,
//            tag.getLong(JellyDungeonKeyItem.Companion.INSTANCE_TAG)
//        )
//        tag.putInt(JellyDungeonKeyItem.Companion.COMPLETIONS_TAG, data.getCompletionCount(player.getUUID(), kind))
//        tag.putBoolean(JellyDungeonKeyItem.Companion.ACTIVE_TAG, bound && !exhausted)
//        tag.putBoolean(JellyDungeonKeyItem.Companion.EXHAUSTED_TAG, exhausted)
//        tag.putBoolean(JellyDungeonKeyItem.Companion.PORTAL_PLACED_TAG, portalPlaced)
//    }
//
//    override fun isFoil(stack: net.minecraft.world.item.ItemStack): kotlin.Boolean {
//        return false
//    }
//
//    override fun appendHoverText(
//        stack: net.minecraft.world.item.ItemStack,
//        level: net.minecraft.world.level.Level?,
//        tooltip: kotlin.collections.MutableList<net.minecraft.network.chat.Component?>,
//        flag: net.minecraft.world.item.TooltipFlag
//    ) {
//        val tag = stack.getTag()
//        val completions = if (tag == null) 0 else tag.getInt(JellyDungeonKeyItem.Companion.COMPLETIONS_TAG)
//        tooltip.add(
//            net.minecraft.network.chat.Component.translatable(
//                "tooltip.candycraftmod.dungeon_key.progress." + kind.getSerializedName(), completions
//            ).withStyle(net.minecraft.ChatFormatting.GRAY)
//        )
//        if (tag != null && tag.getBoolean(JellyDungeonKeyItem.Companion.EXHAUSTED_TAG)) {
//            tooltip.add(
//                net.minecraft.network.chat.Component.translatable("tooltip.candycraftmod.dungeon_key.exhausted")
//                    .withStyle(net.minecraft.ChatFormatting.RED)
//            )
//        } else if (tag != null && tag.getBoolean(JellyDungeonKeyItem.Companion.ACTIVE_TAG) && tag.getBoolean(
//                JellyDungeonKeyItem.Companion.PORTAL_PLACED_TAG
//            )
//        ) {
//            tooltip.add(
//                net.minecraft.network.chat.Component.translatable("tooltip.candycraftmod.dungeon_key.active")
//                    .withStyle(net.minecraft.ChatFormatting.YELLOW)
//            )
//        } else if (tag != null && tag.getBoolean(JellyDungeonKeyItem.Companion.ACTIVE_TAG)) {
//            tooltip.add(
//                net.minecraft.network.chat.Component.translatable("tooltip.candycraftmod.dungeon_key.charged")
//                    .withStyle(net.minecraft.ChatFormatting.GREEN)
//            )
//        }
//    }
//
//    companion object {
//        private const val OWNER_TAG = "CandyCraftDungeonKeyOwner"
//        private const val INSTANCE_TAG = "CandyCraftDungeonKeyInstance"
//        private const val COMPLETIONS_TAG = "CandyCraftDungeonKeyCompletions"
//        private const val ACTIVE_TAG = "CandyCraftDungeonKeyActive"
//        private const val EXHAUSTED_TAG = "CandyCraftDungeonKeyExhausted"
//        private const val PORTAL_PLACED_TAG = "CandyCraftDungeonKeyPortalPlaced"
//        private fun bind(
//            stack: net.minecraft.world.item.ItemStack,
//            player: net.minecraft.server.level.ServerPlayer,
//            instance: Instance,
//            completions: kotlin.Int,
//            portalPlaced: kotlin.Boolean
//        ) {
//            val tag = stack.getOrCreateTag()
//            tag.putUUID(JellyDungeonKeyItem.Companion.OWNER_TAG, player.getUUID())
//            tag.putLong(JellyDungeonKeyItem.Companion.INSTANCE_TAG, instance.id())
//            tag.putInt(JellyDungeonKeyItem.Companion.COMPLETIONS_TAG, completions)
//            tag.putBoolean(JellyDungeonKeyItem.Companion.ACTIVE_TAG, true)
//            tag.putBoolean(JellyDungeonKeyItem.Companion.EXHAUSTED_TAG, false)
//            tag.putBoolean(JellyDungeonKeyItem.Companion.PORTAL_PLACED_TAG, portalPlaced)
//        }
//    }
}
