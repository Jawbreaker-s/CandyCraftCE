package cn.jawbreakers.candycraftce.item

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.registry.CItems
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.util.Mth
import net.minecraft.util.Mth.clamp
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level
import kotlin.math.roundToInt

class JumpWandItem(properties: Properties) : Item(properties) {
    companion object {
        val barColor = Mth.hsvToRgb(0.58f, 0.7f, 1.0f)
        private const val USES_TAG = "JumpWandUses"
        
        const val MAX_USES = 10 //最大耐久度
        const val RECHARGE_USES = 1 //每次充能
        const val CHARGE_TICKS = 30 //最大充能时间

        const val TOOLTIP_WAND_USED = "tooltip.$MOD_ID.wand_uses"
        const val TOOLTIP_WAND_RESTORE = "tooltip.$MOD_ID.wand_recharge"
    }

    fun getUses(stack: ItemStack): Int {
        val tag = stack.tag
        if (tag == null || !tag.contains(USES_TAG)) return MAX_USES

        return clamp(tag.getInt(USES_TAG), 0, MAX_USES)
    }

    private fun setUses(stack: ItemStack, uses: Int) {
        stack.getOrCreateTag().putInt(USES_TAG, clamp(uses, 0, MAX_USES))
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack?> {
        val stack = player.getItemInHand(hand)
        //潜行充能
        if ((player.isShiftKeyDown || getUses(stack) <= 0) && tryRecharge(player, stack)) {
            level.playSound(
                null, player.x, player.y, player.z,
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7f, 1.4f
            )
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
        }
        if (getUses(stack) <= 0) {
            return InteractionResultHolder.fail(stack)
        }
        player.startUsingItem(hand)
        return InteractionResultHolder.consume(stack)
    }

    override fun releaseUsing(stack: ItemStack, level: Level, entity: LivingEntity, timeLeft: Int) {
        if (getUses(stack) <= 0) return

        val usedTicks = getUseDuration(stack) - timeLeft
        val charge =
            clamp(usedTicks / CHARGE_TICKS.toFloat(), 0.15f, 1.0f)

        if (!level.isClientSide) {
            launch(entity, charge)
        }

        level.playSound(
            null, entity.x, entity.y, entity.z,
            SoundEvents.SLIME_JUMP, SoundSource.PLAYERS,
            0.9f, 0.75f + charge * 0.35f
        )

        if (entity is Player) {
            entity.awardStat(Stats.ITEM_USED.get(this))

            if (entity.abilities.instabuild || entity.isSpectator) {
                //不消耗呢哎就
                return
            }
        }
        setUses(stack, getUses(stack) - 1)
    }

    fun tryRecharge(player: Player, wand: ItemStack): Boolean {
        if (getUses(wand) >= MAX_USES) return false

        if (!player.abilities.instabuild) {
            val gummy = findGummyBall(player)
            if (gummy.isEmpty) return false
            gummy.shrink(1)
        }
        setUses(wand, getUses(wand) + RECHARGE_USES)
        return true
    }

    private fun findGummyBall(player: Player): ItemStack {
        val offhand = player.offhandItem
        if (offhand.`is`(CItems.gummy_ball.get())) return offhand

        return player.inventory.items.firstOrNull { it.`is`(CItems.gummy_ball.get()) }
            ?: ItemStack.EMPTY
    }

    private fun launch(entity: LivingEntity, charge: Float) {
        entity.deltaMovement = entity.deltaMovement.multiply(0.45, 0.0, 0.45)
            .add(0.0, 0.95 + 1.65 * charge, 0.0)
        entity.hasImpulse = true //标记实体具有冲量
        entity.fallDistance = 0.0f //重置坠落距离
    }

    override fun getUseDuration(stack: ItemStack): Int = 72000

    override fun getUseAnimation(stack: ItemStack): UseAnim = UseAnim.BOW

    override fun isBarVisible(stack: ItemStack): Boolean = getUses(stack) < MAX_USES

    override fun getBarWidth(stack: ItemStack): Int = (13.0f * getUses(stack) / MAX_USES).roundToInt()

    override fun getBarColor(stack: ItemStack): Int = barColor

    override fun isValidRepairItem(stack: ItemStack, repairCandidate: ItemStack): Boolean =
        repairCandidate.`is`(CItems.gummy_ball.get())

    override fun appendHoverText(stack: ItemStack, level: Level?, tooltip: MutableList<Component>, flag: TooltipFlag) {
        tooltip.add(
            Component.translatable(TOOLTIP_WAND_USED, getUses(stack), MAX_USES)
                .withStyle(net.minecraft.ChatFormatting.YELLOW)
        )
        tooltip.add(
            Component.translatable(
                TOOLTIP_WAND_RESTORE,
                CItems.gummy_ball.get().description, RECHARGE_USES
            ).withStyle(net.minecraft.ChatFormatting.GREEN)
        )
    }

}
