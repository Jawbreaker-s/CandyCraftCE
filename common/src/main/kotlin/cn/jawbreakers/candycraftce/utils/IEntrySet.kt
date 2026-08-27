package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.utils.registry.Entry

interface IEntrySet<T> {
    fun entries(): List<Entry<out T>>
}