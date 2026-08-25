package cn.jawbreakers.candycraftce.item

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import kotlin.math.max

class SugarPillItem(properties: Properties) : net.minecraft.world.item.Item(properties) {
    override fun finishUsingItem(stack: ItemStack, level: Level, living: LivingEntity): ItemStack {
        val result = super.finishUsingItem(stack, level, living)

        if (!level.isClientSide && living is Player) {
            //清除玩家身上的所有效果
            living.removeAllEffects()
            for (effect in getEffects(stack)) {
                living.addEffect(MobEffectInstance(effect))
            }
        }

        return result
    }

    override fun appendHoverText(stack: ItemStack, level: Level?, tooltip: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack, level, tooltip, flag)
        for (effect in getEffects(stack)) {
            var name: Component = Component.translatable(effect.descriptionId)
            val amplifier = effect.amplifier
            if (amplifier > 0) {
                name = Component.translatable(
                    "potion.withAmplifier", name, Component.translatable("potion.potency.$amplifier")
                )
            }
            name = Component.translatable(
                "potion.withDuration",
                name,
                MobEffectUtilCompat.formatDuration(effect)
            )
            tooltip.add(name.copy().withStyle(effect.effect.category.tooltipFormatting))
        }
        if (getEffects(stack).isEmpty()) {
            tooltip.add(
                Component.translatable("item.$MOD_ID.sugar_pill.empty")
                    .withStyle(ChatFormatting.GRAY)
            )
        }
    }

    private object MobEffectUtilCompat {
        fun formatDuration(effect: MobEffectInstance): Component {
            val seconds = max(1, effect.duration / 20)
            return Component.literal("%d:%02d".format(seconds / 60, seconds % 60))
        }
    }

    companion object {
        private const val TAG_EFFECTS = "Effects"
        private const val TAG_COLORS = "Colors"

        fun getEffects(stack: ItemStack): List<MobEffectInstance> {
            val effects = mutableListOf<MobEffectInstance>()
            val tag = stack.tag
            if (tag == null || !tag.contains(TAG_EFFECTS, Tag.TAG_LIST.toInt())) {
                return effects
            }

            val list = tag.getList(TAG_EFFECTS, Tag.TAG_COMPOUND.toInt())
            for (element in list) {
                if (element is CompoundTag) {
                    val id = element.getString("Id")
                    val effect = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.tryParse(id))
                    if (effect != null) {
                        effects.add(
                            MobEffectInstance(
                                effect,
                                element.getInt("Duration"),
                                element.getInt("Amplifier"),
                                false,
                                true,
                                true
                            )
                        )
                    }
                }
            }
            return effects
        }

        fun getLayerColor(stack: ItemStack, tintIndex: Int): Int {
            if (tintIndex <= 0) {
                return -1
            }

            val tag = stack.tag
            if (tag == null || !tag.contains(TAG_COLORS, Tag.TAG_INT_ARRAY.toInt())) {
                return -1
            }

            val colors = tag.getIntArray(TAG_COLORS)
            val colorIndex = tintIndex - 1
            return if (colorIndex < colors.size) colors[colorIndex] else 0xFFFFFF
        }

        fun setData(stack: ItemStack, effects: MutableList<MobEffectInstance>, colors: IntArray) {
            val tag = stack.getOrCreateTag()
            val list = net.minecraft.nbt.ListTag()
            for (effect in effects) {
                val effectTag = CompoundTag()
                effectTag.putString("Id", BuiltInRegistries.MOB_EFFECT.getKey(effect.effect).toString())
                effectTag.putInt("Duration", effect.duration)
                effectTag.putInt("Amplifier", effect.amplifier)
                list.add(effectTag)
            }
            tag.put(TAG_EFFECTS, list)
            tag.putIntArray(TAG_COLORS, colors)
        }
    }
}
