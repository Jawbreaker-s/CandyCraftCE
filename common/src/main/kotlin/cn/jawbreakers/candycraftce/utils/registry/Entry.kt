package cn.jawbreakers.candycraftce.utils.registry

import net.minecraft.resources.ResourceLocation

abstract class Entry<T>(val id: ResourceLocation) {
    abstract val value: T
}