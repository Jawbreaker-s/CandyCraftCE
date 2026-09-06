package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.utils.MCTimeUnit.Companion.tick
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.item.Item
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block

object CUtils {

    fun never(vararg any: Any): Boolean = false
    fun always(vararg any: Any): Boolean = true
    fun <R> ResourceLocation.toKey(registry: ResourceKey<Registry<R>>): ResourceKey<R> {
        return ResourceKey.create(registry, this)
    }

    fun String.modLoc(modId: String = MOD_ID) = ResourceLocation(modId, this)

    /**
     * @return namespace:textures/gui/(path).png
     */
    fun ResourceLocation.guiTex(): ResourceLocation = withPath { "textures/gui/$it.png" }

    /**
     * @return namespace:textures/entity/(path).png
     */
    fun ResourceLocation.entityTex(): ResourceLocation = withPath { "textures/entity/$it.png" }
    fun ResourceLocation.model(location: String) = ModelResourceLocation(namespace, location, path)

    fun String.mcLoc() = ResourceLocation(this)
    fun <V> ResourceLocation.get(register: Registry<V>): V? = register.get(this)

    fun <V : Any> Registry<V>.createKey(id: ResourceLocation) = ResourceKey.create(this.key(), id)!!
    fun <V : Any> Registry<in V>.register(id: ResourceLocation, value: V): V = Registry.register(this, id, value)
    fun <R : Any, V : R> Registry<R>.register(id: ResourceKey<R>, value: V): V = Registry.register(this, id, value)


    val Item.key get() = BuiltInRegistries.ITEM.getKey(this)
    val Block.key get() = BuiltInRegistries.BLOCK.getKey(this)

    /**
     * @param amplifier 药水等级
     * @param ambient 是否显示粒子
     * */
    fun MobEffect.instance(
        duration: MCTimeUnit = 0.tick,
        amplifier: Int = 0,
        ambient: Boolean = false,
        visible: Boolean = true,
        showIcon: Boolean = visible,
    ): MobEffectInstance {
        return MobEffectInstance(this, duration.toTick, amplifier, ambient, visible, showIcon)
    }

    fun GameRules.Key<GameRules.BooleanValue>.get(level: Level): Boolean = level.gameRules.getBoolean(this)
    fun GameRules.Key<GameRules.IntegerValue>.get(level: Level): Int = level.gameRules.getInt(this)
    fun <T : GameRules.Value<T>> GameRules.Key<T>.get(level: Level): T = level.gameRules.getRule(this)

    inline fun PoseStack.use(crossinline action: PoseStack.() -> Unit) {
        pushPose()
        action()
        popPose()
    }

    //读取并自动写入复合nbt里面的数据
    fun <R> CompoundTag.useCompound(key: String, block: (CompoundTag) -> R): R {
        if (this.contains(key, Tag.TAG_COMPOUND.toInt())) {
            return block(getCompound(key))
        } else {
            val tag = CompoundTag()
            val r = block(tag)
            put(key, tag)
            return r
        }
    }
}
