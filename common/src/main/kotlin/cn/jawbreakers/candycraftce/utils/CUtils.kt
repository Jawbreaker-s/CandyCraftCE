package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.utils.MCTimeUnit.Companion.tick
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.item.Item
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import java.awt.Color
import java.security.MessageDigest
import java.util.zip.CRC32
import kotlin.text.Charsets.UTF_8

object CUtils {
    val Int.rgb get() = Color(this)
    val String.rgb
        get() = run {
            require(startsWith("#") || startsWith("0x")) { "Invalid color format" }
            Color(Integer.decode(this))
        }

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

    fun GameRules.Key<GameRules.BooleanValue>.get(level: Level): Boolean {
        return level.gameRules.getBoolean(this)
    }

    fun GameRules.Key<GameRules.IntegerValue>.get(level: Level): Int {
        return level.gameRules.getInt(this)
    }

    fun <T : GameRules.Value<T>> GameRules.Key<T>.get(level: Level): T {
        return level.gameRules.getRule(this)
    }

    //读取并自动写入复合nbt里面的数据
    fun <R> CompoundTag.use(key: String, block: (CompoundTag) -> R): R {
        if (key in this) {
            return block(getCompound(key))
        } else {
            val tag = CompoundTag()
            val r = block(tag)
            put(key, tag)
            return r
        }
    }

    fun CompoundTag.getOrCreateCompound(key: String): CompoundTag {
        return if (key in this) getCompound(key) else CompoundTag().also { put(key, it) }
    }


    inline fun <R> trying(supplier: () -> R): R? {
        return try {
            supplier()
        } catch (e: Throwable) {
            null
        }
    }

    fun String.crc32(): String {
        val crc = CRC32()
        crc.update(this.toByteArray(UTF_8))
        // 将 Long 转换为 8 位十六进制（因为 CRC32 结果实际为 32 位）
        return crc.value.toUInt().toString(16).padStart(8, '0')
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(this.toByteArray(UTF_8))
        return digest.toHexString()
    }
}
