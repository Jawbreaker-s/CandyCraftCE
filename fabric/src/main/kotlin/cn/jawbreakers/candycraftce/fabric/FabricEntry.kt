package cn.jawbreakers.candycraftce.fabric

import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.resources.ResourceLocation

class FabricEntry<T>(id: ResourceLocation) : Entry<T>(id) {
    private var valueInternal: T? = null
    override val value: T get() = valueInternal ?: throw IllegalStateException("Entry $id has not been initialized")

    internal fun set(value: T) {
        valueInternal = value
    }
}