package cn.jawbreakers.candycraftce.block

import cn.jawbreakers.candycraftce.registry.CBlockEntities.advanced_sugar_factory_be
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

/**
 * Created in 2024/2/4
 * Project: candycraftce
 * 
 * Author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * 
 * 
 */
class AdvancedSugarFactoryBlock(properties: Properties) : SugarFactoryBlock(properties) {
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return advanced_sugar_factory_be.get().create(pos, state)
    }
}
