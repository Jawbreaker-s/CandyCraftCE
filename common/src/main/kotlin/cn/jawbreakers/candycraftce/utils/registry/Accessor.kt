package cn.jawbreakers.candycraftce.utils.registry

import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Accessor<T> : Supplier<T>, ReadOnlyProperty<Any?, T> {
    val value: T
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    override fun get(): T = value
}

interface MutableAccessor<T> : Accessor<T>, Consumer<T>, ReadWriteProperty<Any?, T> {
    override var value: T

    fun set(value: T) {
        this.value = value
    }

    @Deprecated("Use set(value) instead", replaceWith = ReplaceWith("set(value)"))
    override fun accept(value: T) = set(value)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

class MutableAccessorImpl<T>(override var value: T) : MutableAccessor<T>
class AccessorImpl<T>(override val value: T) : Accessor<T>
class LateInitAccessor<T> : MutableAccessor<T> {
    private var valueInternal: T? = null
    override var value: T
        get() = valueInternal ?: throw UninitializedPropertyAccessException("Value has not been initialized")
        set(value) {
            valueInternal = value
        }
}