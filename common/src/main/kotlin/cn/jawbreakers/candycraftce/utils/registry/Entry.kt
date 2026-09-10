package cn.jawbreakers.candycraftce.utils.registry

import net.minecraft.resources.ResourceLocation
import java.util.function.Supplier

abstract class Entry<T>(val id: ResourceLocation) : Supplier<T> {
    abstract val value: T
    override fun get(): T = value
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entry<*>) return false

        if (id != other.id) return false
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = id.toString()

}

class EntryWrapper<T>(id: ResourceLocation, override val value: T) : Entry<T>(id)

class LazyEntry<T>(id: ResourceLocation, factory: () -> T) : Entry<T>(id) {
    override val value: T by lazy(factory)
}
