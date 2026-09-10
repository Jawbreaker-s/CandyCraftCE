package cn.jawbreakers.candycraftce.fluid

import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.material.Fluid

data class CFluidProperties(
    val type: CFluidType,
    val source: () -> Entry<out Fluid>,
    val flowing: () -> Entry<out Fluid>,
    val block: (() -> Entry<out LiquidBlock>),
    val bucket: (() -> Entry<out Item>)? = null,
    val slopeFindDistance: Int = 4,
    val levelDecreasePerBlock: Int = 1,
    val explosionResistance: Float = 100f,
    val tickRate: Int = 5,
)
