package cn.jawbreakers.candycraftce.misc

import cn.jawbreakers.candycraftce.registry.CBiomes.caramel_forest
import cn.jawbreakers.candycraftce.registry.CBiomes.chocolate_forest
import cn.jawbreakers.candycraftce.registry.CBiomes.cotton_candy_plains
import cn.jawbreakers.candycraftce.registry.CBiomes.gummy_swamp
import cn.jawbreakers.candycraftce.registry.CBiomes.hard_candy_plains
import cn.jawbreakers.candycraftce.registry.CBiomes.ice_cream_plains
import cn.jawbreakers.candycraftce.registry.CBiomes.ice_cream_sky_mountains
import cn.jawbreakers.candycraftce.registry.CBiomes.sugar_cold_forest
import cn.jawbreakers.candycraftce.registry.CBiomes.sugar_enchanted_forest
import cn.jawbreakers.candycraftce.registry.CBiomes.sugar_forest
import cn.jawbreakers.candycraftce.registry.CBiomes.sugar_hell_mountains
import cn.jawbreakers.candycraftce.registry.CBiomes.sugar_mountains
import cn.jawbreakers.candycraftce.registry.CBiomes.sugar_oceans
import cn.jawbreakers.candycraftce.registry.CBiomes.sugar_plains
import cn.jawbreakers.candycraftce.registry.CBiomes.sugar_river
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.asItemEntry
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.registerBlockAndItemColor
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.registerBlockColor
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.registerItemColor
import cn.jawbreakers.candycraftce.utils.LinearGradient
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.util.RandomSource
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.synth.NormalNoise
import net.minecraft.world.phys.Vec3
import java.awt.Color
import kotlin.jvm.optionals.getOrNull


/**
 * Created by NiceCat on 2025/12/9.
 * Project: candycraftce
 * @author <a href="https://github.com/BreadNiceCat">Bread_NiceCat</a>
 *
 */
object PuddingColor {
    private val noise: NormalNoise = NormalNoise.create(RandomSource.create(8526L), -7, 1.0)
    private val enchant_color = LinearGradient {
        Color(0xb0ecff) % 0f
        Color(0xa376da) % .5f
        Color(0xb0b0ff) % 1f
    }

    /**
     * @return #dd99aa 粉色
     */
    const val DEFAULT_PUDDING_COLOR = 0xdd99aa
    const val DEFAULT_ENCHANT_COLOR = 0xb0ecff

    /**
     * @return #b0ecff 淡蓝色 #b0b0ff 淡紫色 #a376da 深紫色
     */
    fun getEnchantColor(pos: Vec3): Int {
        val d0 = noise.getValue(pos.x, pos.y, pos.z)

        val r = d0 + 0.5 / (0.5 + 0.5)
        return enchant_color.getColor(r.toFloat()).rgb
    }

    private fun getPuddingColor(biome: Holder<Biome>, pos: Vec3): Int {
        return when (biome.unwrapKey().getOrNull()) {
            sugar_enchanted_forest -> getEnchantColor(pos)
            sugar_plains, hard_candy_plains, sugar_forest -> 0xEEAABB
            sugar_mountains -> 0xEEBBCC
            sugar_cold_forest -> 0xFFDDEE
            ice_cream_plains, ice_cream_sky_mountains, sugar_hell_mountains -> 0xFFFFFF
            sugar_oceans -> 0xB35EFF
            caramel_forest -> 0xB05C28
            cotton_candy_plains -> 0xFFC6E4
            gummy_swamp -> 0xFFFEB0
            chocolate_forest -> 0xF3DFA8
            sugar_river -> 0xFFBBCC
            else -> DEFAULT_PUDDING_COLOR
        }
    }


    fun initColor() {
        clog.info("Initializing Dynamic Colors...")
        registerBlockColor(CBlocks.custard_pudding_block, CBlocks.strawberry_filled_pudding) { _, level, pos, _ ->
            if (level != null && pos != null) {
                val biome = level.getBiome(pos)
                if (biome != null) {
                    return@registerBlockColor getPuddingColor(biome, Vec3.atCenterOf(pos))
                }
            }
            DEFAULT_PUDDING_COLOR
        }
        registerItemColor(
            CBlocks.custard_pudding_block.asItemEntry(),
            CBlocks.strawberry_filled_pudding.asItemEntry(),
            color = DEFAULT_PUDDING_COLOR
        )

        registerBlockColor(CBlocks.enchant_candy_leaves) { _, _, pos, _ ->
            if (pos != null) getEnchantColor(Vec3.atCenterOf(pos)) else DEFAULT_ENCHANT_COLOR
        }
        registerItemColor(CBlocks.enchant_candy_leaves.asItemEntry(), color = DEFAULT_ENCHANT_COLOR)

        registerBlockAndItemColor(*CBlocks.red_gummy_family.toTypedArray(), color = 0xff4530)
        registerBlockAndItemColor(*CBlocks.orange_gummy_family.toTypedArray(), color = 0xff9b4f)
        registerBlockAndItemColor(*CBlocks.yellow_gummy_family.toTypedArray(), color = 0xffe563)
        registerBlockAndItemColor(*CBlocks.white_gummy_family.toTypedArray(), color = 0xfffeb0)
        registerBlockAndItemColor(*CBlocks.green_gummy_family.toTypedArray(), color = 0x80e22b)
    }

}

private fun BlockAndTintGetter.getBiome(pos: BlockPos): Holder<Biome>? {
    if (this is LevelReader) return getBiome(pos)

    val minecraft = Minecraft.getInstance()
    return if (minecraft.level != null) minecraft.level!!.getBiome(pos) else null
}