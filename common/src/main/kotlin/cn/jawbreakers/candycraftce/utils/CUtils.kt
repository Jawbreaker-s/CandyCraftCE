package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifDev
import cn.jawbreakers.candycraftce.utils.TickUnit.tick
import cn.jawbreakers.candycraftce.utils.registry.MutableAccessor
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.Vec3

object CUtils {

    fun never(vararg any: Any): Boolean = false
    fun always(vararg any: Any): Boolean = true
    fun <E> RandomSource.choice(k: List<E>): E {
        require(k.isNotEmpty()) { "Collection must not be empty" }
        return k[nextInt(k.size)]
    }

    fun <T : Any> SynchedEntityData.synched(key: EntityDataAccessor<T>): MutableAccessor<T> {
        return MutableAccessor.lambda({ get(key) }, { set(key, it) })
    }

    fun <T : Any> Entity.synched(key: EntityDataAccessor<T>): MutableAccessor<T> = entityData.synched(key)
    fun <T : Any> defineId(type: EntityDataSerializer<T>): EntityDataAccessor<T> {
        var caller = CLogUtils.walker.callerClass

        val enclosing: Class<*>? = caller.enclosingClass
        val meta: Metadata? = caller.getAnnotation(Metadata::class.java)
        // 伴生对象（匿名或具名）被编译为 SYNTHETIC_CLASS (kind = 3)
        if (enclosing != null && meta != null && meta.kind == 3) {
            caller = enclosing
        } else {
            ifDev {
                CLogUtils.debugLog.info("`defineId` auto-detected `$caller`")
            }
        }

        if (!Entity::class.java.isAssignableFrom(caller)) {
            throw IllegalArgumentException("Cannot define entity data for a non-entity class $caller")
        }

        @Suppress("UNCHECKED_CAST")// runtime-checked by isAssignableFrom
        return SynchedEntityData.defineId(caller as Class<out Entity>, type)
    }

    fun String.modLoc(modId: String = MOD_ID) = ResourceLocation(modId, this)

    //for java usage
    @JvmStatic
    fun prefix(path: String) = path.modLoc()

    /**
     * @return namespace:textures/gui/(path)].png
     */
    fun ResourceLocation.guiTex(suffix: String = ""): ResourceLocation = withPath { "textures/gui/$it$suffix.png" }

    /**
     * @return namespace:textures/entity/(path).png
     */
    fun ResourceLocation.entityTex(suffix: String = ""): ResourceLocation =
        withPath { "textures/entity/$it$suffix.png" }

    fun String.mcLoc() = ResourceLocation(this)
    fun <V> ResourceLocation.get(register: Registry<V>): V? = register.get(this)
    fun <V : Any> Registry<in V>.register(id: ResourceLocation, value: V): V = Registry.register(this, id, value)
    fun <R : Any, V : R> Registry<R>.register(id: ResourceKey<R>, value: V): V = Registry.register(this, id, value)


    val Item.key get() = BuiltInRegistries.ITEM.getKey(this)
    val Block.key get() = BuiltInRegistries.BLOCK.getKey(this)

    /**
     * @param amplifier 药水等级
     * @param ambient 是否显示粒子
     * */
    fun MobEffect.instance(
        duration: Ticks = 0.tick,
        amplifier: Int = 0,
        ambient: Boolean = false,
        visible: Boolean = true,
        showIcon: Boolean = visible,
    ): MobEffectInstance {
        return MobEffectInstance(this, duration, amplifier, ambient, visible, showIcon)
    }

    fun GameRules.Key<GameRules.BooleanValue>.get(level: Level): Boolean = level.gameRules.getBoolean(this)
    fun GameRules.Key<GameRules.IntegerValue>.get(level: Level): Int = level.gameRules.getInt(this)
    fun <T : GameRules.Value<T>> GameRules.Key<T>.get(level: Level): T = level.gameRules.getRule(this)

    inline fun PoseStack.use(crossinline action: PoseStack.() -> Unit) {
        pushPose()
        action()
        popPose()
    }

    fun CompoundTag.putBlockPos(key: String, pos: BlockPos) = putIntArray(key, intArrayOf(pos.x, pos.y, pos.z))
    fun CompoundTag.getBlockPos(key: String): BlockPos? {
        return if (contains(key, Tag.TAG_INT_ARRAY.toInt())) {
            val array = getIntArray(key)
            BlockPos(array[0], array[1], array[2])
        } else null
    }

    fun CompoundTag.putDoubleArray(key: String, numbers: DoubleArray) {
        val list = ListTag()
        for (d0 in numbers) {
            list.add(DoubleTag.valueOf(d0))
        }
        put(key, list)
    }

    fun CompoundTag.getDoubleArray(key: String): DoubleArray? {
        if (contains(key, Tag.TAG_LIST.toInt())) {
            val list = getList(key, Tag.TAG_DOUBLE.toInt())
            return DoubleArray(list.size, list::getDouble)
        }
        return null
    }

    fun CompoundTag.putVec3(key: String, vec3: Vec3) {
        putDoubleArray(key, doubleArrayOf(vec3.x, vec3.y, vec3.z))
    }

    fun CompoundTag.getVec3(key: String): Vec3? {
        return getDoubleArray(key)?.takeIf { size() == 3 }?.let { Vec3(it[0], it[1], it[2]) }
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

    inline fun <T> ProfilerFiller.use(name: String, action: () -> T): T {
        push(name)
        return try {
            action()
        } finally {
            pop()
        }
    }
}
