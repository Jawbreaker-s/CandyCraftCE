package cn.jawbreakers.candycraftce.utils.registry

import net.minecraft.resources.ResourceLocation
import java.util.function.Supplier

abstract class Entry<T>(val id: ResourceLocation) : Supplier<T> {
    abstract val value: T
    override fun get(): T = value
}