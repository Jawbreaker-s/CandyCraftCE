package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.Util
import net.minecraft.util.datafix.fixes.References
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.entity.BlockEntityType.Builder
import java.util.function.Supplier

object CBlockEntities {
    init {
        CLogUtils.sign()
    }

    fun <T : BlockEntity> register(key: String, builder: Supplier<Builder<T>>): Entry<BlockEntityType<T>> {
        val type = Util.fetchChoiceType(References.BLOCK_ENTITY, key.modLoc().toString())
        return CPlatformUtils.registerBlockEntity(key, builder, type)
    }
}