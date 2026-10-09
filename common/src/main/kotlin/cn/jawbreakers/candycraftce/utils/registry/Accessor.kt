package cn.jawbreakers.candycraftce.utils.registry

import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Accessor<T> : Supplier<T>, ReadOnlyProperty<Any?, T> {
    companion object {
        fun <T> of(value: T): Accessor<T> = AccessorImpl(value)
        fun <T> lambda(getter: () -> T) = object : Accessor<T> {
            override val value: T = getter()
        }
    }

    val value: T
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    override fun get(): T = value
    fun <R> xmap(mapping: (T) -> R): Accessor<R> {
        return lambda { mapping(value) }
    }
}

interface MutableAccessor<T> : Accessor<T>, Consumer<T>, ReadWriteProperty<Any?, T> {
    companion object {
        fun <T> create(initialValue: T): MutableAccessor<T> = MutableAccessorImpl(initialValue)
        fun <T> lambda(getter: () -> T, setter: (T) -> Unit) = object : MutableAccessor<T> {
            override var value: T
                get() = getter()
                set(value) = setter(value)
        }
    }

    override var value: T

    fun set(value: T) {
        this.value = value
    }

    fun strict(condition: (T) -> T) = xmap(condition, condition)
    fun <R> xmap(mapping: (T) -> R, remapping: (R) -> T): MutableAccessor<R> {
        return lambda({ mapping(value) }, { set(remapping(it)) })
    }

    @Deprecated("Use set(value) instead", replaceWith = ReplaceWith("set(value)"))
    override fun accept(value: T) = set(value)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

class AccessorImpl<T>(override val value: T) : Accessor<T>
class MutableAccessorImpl<T>(override var value: T) : MutableAccessor<T>
class LateInitAccessor<T> : MutableAccessor<T> {
    private var valueInternal: T? = null
    override var value: T
        get() = valueInternal ?: throw UninitializedPropertyAccessException("Value has not been initialized")
        set(value) {
            valueInternal = value
        }
}