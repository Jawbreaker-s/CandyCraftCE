package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.utils.registry.Entry

interface IEntrySet<T> : List<Entry<out T>> {
    fun entries(): List<Entry<out T>>

    override val size: Int get() = entries().size
    override fun contains(element: Entry<out T>) = entries().contains(element)
    override fun containsAll(elements: Collection<Entry<out T>>) = entries().containsAll(elements)
    override fun get(index: Int) = entries()[index]
    override fun indexOf(element: Entry<out T>) = entries().indexOf(element)
    override fun isEmpty() = entries().isEmpty()
    override fun iterator() = entries().iterator()
    override fun lastIndexOf(element: Entry<out T>) = entries().lastIndexOf(element)
    override fun listIterator() = entries().listIterator()
    override fun listIterator(index: Int) = entries().listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int) = entries().subList(fromIndex, toIndex)


}