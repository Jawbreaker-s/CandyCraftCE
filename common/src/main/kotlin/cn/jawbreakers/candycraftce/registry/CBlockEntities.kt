package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.blockentity.AdvancedFactoryBE
import cn.jawbreakers.candycraftce.blockentity.ChocolateFurnaceBE
import cn.jawbreakers.candycraftce.blockentity.LicoriceFurnaceBE
import cn.jawbreakers.candycraftce.blockentity.SugarFactoryBE
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.util.datafix.fixes.References
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.entity.BlockEntityType.Builder.of
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Supplier

object CBlockEntities {
    init {
        CLogUtils.sign()
    }

    val sugar_factory_be = register(CBlocks.sugar_block.name) {
        of(::SugarFactoryBE, CBlocks.sugar_factory.get())
    }
    val advanced_sugar_factory_be = register(CBlocks.advanced_sugar_factory.name) {
        of(::AdvancedFactoryBE, CBlocks.advanced_sugar_factory.get())
    }
    val licorice_furnace_be = register(CBlocks.licorice_furnace.name) {
        of(::LicoriceFurnaceBE, CBlocks.licorice_furnace.get())
    }
    val chocolate_furnace_be = register(CBlocks.chocolate_furnace.name) {
        of(::ChocolateFurnaceBE, CBlocks.chocolate_furnace.get())
    }

    fun <T : BlockEntity> Entry<BlockEntityType<T>>.create(pos: BlockPos, state: BlockState) = get().create(pos, state)
    fun <T : BlockEntity> register(
        key: String,
        builder: Supplier<BlockEntityType.Builder<T>>,
    ): Entry<BlockEntityType<T>> {
        val type = Util.fetchChoiceType(References.BLOCK_ENTITY, key.modLoc().toString())
        return CPlatformUtils.registerBlockEntity(key, builder, type)
    }
}