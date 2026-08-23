package cn.jawbreakers.candycraftce.forge

import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraftforge.registries.RegistryObject

class ForgeEntry<T>(
    val holder: RegistryObject<T>,
) : Entry<T>(holder.id) {
    companion object {
        fun <T> RegistryObject<T>.asEntry(): Entry<T> {
            return ForgeEntry(this)
        }
    }

    override val value: T get() = holder.get()
}