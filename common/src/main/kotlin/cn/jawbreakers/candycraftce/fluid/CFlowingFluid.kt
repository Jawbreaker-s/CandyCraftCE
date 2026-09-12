package cn.jawbreakers.candycraftce.fluid

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.utils.IEntrySet
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.material.FlowingFluid
import net.minecraft.world.level.material.MapColor

// should use tag version
//fun FluidState.`is`(ref: CFluidReferences): Boolean {
//    return this.type == ref.source.get() || this.type == ref.flowing.get()
//}

data class CFluidReferences(
    val presets: CFluidPresets,
    val source: Entry<out FlowingFluid>,
    val flowing: Entry<out FlowingFluid>,
    val block: Entry<out LiquidBlock>,
) : IEntrySet<FlowingFluid> {
    fun getBucket(): Item = presets.bucket?.invoke()?.get() ?: Items.AIR
    override fun entries(): List<Entry<out FlowingFluid>> = listOf(source, flowing)

    init {
        require(presets.refInternal == null) { "References already initialized" }
        presets.refInternal = this
    }
}

data class CFluidPresets(
    val name: String,
    val descriptionId: String = "fluid.$MOD_ID.$name",
    val stillTexture: ResourceLocation? = null,
    val flowingTexture: ResourceLocation? = null,
    val overlayTexture: ResourceLocation? = null,//透过透明方块
    val underwaterTexture: ResourceLocation? = null,//在其中
    val isTransparent: Boolean = false,
    val fogColor: Int? = null,
    val tintColor: Int = -0x1,
    val mapColor: MapColor,
    val bucket: (() -> Entry<out Item>)? = null,
    val tickRate: Int = 5,//water=5,lava=20
    val override: IFluidBehaviourOverrides? = CandyFluidOverrides,
) {
    internal var refInternal: CFluidReferences? = null

    val references: CFluidReferences get() = refInternal ?: throw IllegalStateException("References not initialized")
}
