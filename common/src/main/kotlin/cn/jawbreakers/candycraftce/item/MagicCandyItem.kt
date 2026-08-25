package cn.jawbreakers.candycraftce.item


import net.minecraft.world.item.Item

//todo teleport
class MagicCandyItem(properties: Properties) : Item(properties) {
}
//    override fun use(
//        level: net.minecraft.world.level.Level,
//        player: net.minecraft.world.entity.player.Player,
//        hand: net.minecraft.world.InteractionHand
//    ): net.minecraft.world.InteractionResultHolder<net.minecraft.world.item.ItemStack?> {
//        val stack = player.getItemInHand(hand)
//        if (level.dimension() !== net.minecraft.world.level.Level.OVERWORLD && level.dimension() !== CCDimensions.CANDY_WORLD) {
//            return net.minecraft.world.InteractionResultHolder.fail<net.minecraft.world.item.ItemStack?>(stack)
//        }
//        if (!level.isClientSide && player is net.minecraft.server.level.ServerPlayer) {
//            val target: net.minecraft.world.level.Level? = if (level.dimension() === CCDimensions.CANDY_WORLD)
//                player.server.getLevel(net.minecraft.world.level.Level.OVERWORLD)
//            else
//                player.server.getLevel(CCDimensions.CANDY_WORLD)
//            if (target == null) {
//                return net.minecraft.world.InteractionResultHolder.fail<net.minecraft.world.item.ItemStack?>(stack)
//            }
//        }
//        return super.use(level, player, hand)
//    }
//
//    override fun finishUsingItem(
//        stack: net.minecraft.world.item.ItemStack,
//        level: net.minecraft.world.level.Level,
//        living: net.minecraft.world.entity.LivingEntity
//    ): net.minecraft.world.item.ItemStack {
//        if (living !is net.minecraft.world.entity.player.Player) {
//            return super.finishUsingItem(stack, level, living)
//        }
//
//        val hand = living.getUsedItemHand()
//        super.finishUsingItem(stack, level, living)
//        if (!living.getAbilities().instabuild) {
//            stack.setCount(1)
//        }
//
//        if (!level.isClientSide && living is net.minecraft.server.level.ServerPlayer
//            && CandyPortalBlock.teleportPlayer(player)
//        ) {
//            stack.hurtAndBreak<net.minecraft.world.entity.player.Player?>(
//                1,
//                living,
//                java.util.function.Consumer { brokenPlayer: net.minecraft.world.entity.player.Player? ->
//                    brokenPlayer!!.broadcastBreakEvent(hand)
//                })
//        }
//        return stack
//    }
//
//    override fun isEnchantable(stack: net.minecraft.world.item.ItemStack): kotlin.Boolean {
//        return true
//    }
//
//    override fun getEnchantmentValue(): kotlin.Int {
//        return 10
//    }
//
//    public override fun canApplyAtEnchantingTable(
//        stack: net.minecraft.world.item.ItemStack?,
//        enchantment: net.minecraft.world.item.enchantment.Enchantment?
//    ): kotlin.Boolean {
//        val configured: kotlin.Boolean? = CCToolProperties.configuredEnchantmentRule(stack, enchantment)
//        return if (configured != null)
//            configured
//        else
//            enchantment === net.minecraft.world.item.enchantment.Enchantments.UNBREAKING || enchantment === net.minecraft.world.item.enchantment.Enchantments.MENDING
//    }
//
//    companion object {
//        const val USES: kotlin.Int = 4
//    }
