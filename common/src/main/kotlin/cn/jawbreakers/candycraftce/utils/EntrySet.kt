package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.utils.registry.Entry

open class EntrySet<T>(val entries: Set<Entry<out T>>) : Set<T> {
    constructor(vararg entries: Entry<out T>) : this(entries.toSet())

    val unwrap by lazy { entries.map { it.value }.toSet() }
    override val size: Int get() = entries.size
    override fun contains(element: T): Boolean = element in unwrap

    override fun containsAll(elements: Collection<T>) = unwrap.containsAll(elements)

    override fun isEmpty() = unwrap.isEmpty()
    override fun iterator(): Iterator<T> = unwrap.iterator()

}

fun <T> entrySetOf(vararg entries: Entry<out T>) = EntrySet(*entries)

