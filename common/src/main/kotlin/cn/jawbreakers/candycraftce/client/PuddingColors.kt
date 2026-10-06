package cn.jawbreakers.candycraftce.client

import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.asItemEntry
import cn.jawbreakers.candycraftce.registry.CBlocks.custard_pudding_block
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.caramel_forest
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.chocolate_forest
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.enchanted_forest
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.ice_cream_plains
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.ice_cream_sky_mountains
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.pudding_hill
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.pudding_plains
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.sugar_forest
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.sugar_oceans
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.sugar_river
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.white_chocolate_forest
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.registerItemColor
import cn.jawbreakers.candycraftce.utils.ClientOnly
import cn.jawbreakers.candycraftce.utils.LinearGradient
import cn.jawbreakers.candycraftce.utils.LinearGradient.Companion.blue
import cn.jawbreakers.candycraftce.utils.LinearGradient.Companion.green
import cn.jawbreakers.candycraftce.utils.LinearGradient.Companion.red
import cn.jawbreakers.candycraftce.utils.LinearGradient.Companion.rgb
import cn.jawbreakers.candycraftce.utils.registry.MutableAccessor
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.util.RandomSource
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.synth.NormalNoise
import java.awt.Color
import kotlin.jvm.optionals.getOrNull


/**
 * Created by NiceCat on 2025/12/9.
 * Project: candycraftce
 * @author <a href="https://github.com/BreadNiceCat">Bread_NiceCat</a>
 *
 */
@ClientOnly
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
    const val DEFAULT_PUDDING_COLOR = 0xdd99aa//0xdd99aa
    const val DEFAULT_ENCHANT_COLOR = 0x8f8ac8//0xb0ecff
    const val DEFAULT_CHOCOLATE_BROWNIE_COLOR = 0x754424

    //    val radius: Int get() = Minecraft.getInstance().options.biomeBlendRadius().get()
    var blendRadius = 10


    val cacheEnchant: Cache<Long, Int> =
        CacheBuilder.newBuilder().maximumSize(16L * blendRadius * blendRadius * 9L).build()

    /**
     * @return #b0ecff 淡蓝色 #b0b0ff 淡紫色 #a376da 深紫色
     */
    fun getEnchantColor(pos: BlockPos): Int {
        return cacheEnchant.get(pos.asLong()) {
            val vec = pos.center
            val d0 = noise.getValue(vec.x, vec.y, vec.z)
            val r = d0 + 0.5 / (0.5 + 0.5)
            enchant_color.getColor(r.toFloat()).rgb
        }
    }


    private val cachedBlended: Cache<Long, Int> =
        CacheBuilder.newBuilder().maximumSize(32L * blendRadius * blendRadius * 9L).build()

    private var levelCache = 0

    private fun getPuddingColor(biome: Holder<Biome>, pos: BlockPos): Int {
        return when (biome.unwrapKey().getOrNull()) {
            enchanted_forest -> getEnchantColor(pos)
            pudding_plains, sugar_forest -> 0xEEAABB
            pudding_hill -> 0xEEBBCC
            white_chocolate_forest -> 0xFFDDEE
            ice_cream_plains, ice_cream_sky_mountains -> 0xFFFFFF
            sugar_oceans -> 0xB35EFF
            caramel_forest -> 0xB05C28
            chocolate_forest -> DEFAULT_CHOCOLATE_BROWNIE_COLOR
            sugar_river -> 0xFFBBCC
            else -> DEFAULT_PUDDING_COLOR
        }
    }


    fun getBlendedPuddingColor(reader: BlockAndTintGetter, pos: BlockPos, radius: Int): Int {
        val hashCode = Minecraft.getInstance().level.hashCode()
        if (levelCache != hashCode) {
            cachedBlended.invalidateAll()
            levelCache = hashCode
        }
        val key = pos.asLong()
        return cachedBlended.getIfPresent(key) ?: run {
            val flag = MutableAccessor.create(false)
            val color = computeColor(reader, pos, radius, flag)
            if (flag.get()) {
                cachedBlended.put(key, color)
            }
            color
        }
//        return computeColor(reader, pos, radius)
    }

    fun computeColor(reader: BlockAndTintGetter, pos: BlockPos, radius: Int, cacheFlag: MutableAccessor<Boolean>): Int {
        val mu = pos.mutable()
        var r = 0
        var g = 0
        var b = 0
        var count = 0
        for (x in -radius..radius) {
            mu.x = pos.x + x
            for (z in -radius..radius) {
                mu.z = pos.z + z
                val biome = reader.getBiome(mu)
                if (biome != null) {
                    val color = getPuddingColor(biome, mu)
                    r += color.red
                    g += color.green
                    b += color.blue
                    count++
                } else {
                    cacheFlag.set(false)
                }
            }
        }
        return if (count == 0) DEFAULT_PUDDING_COLOR else rgb(r / count, g / count, b / count)
    }

    @ClientOnly
    fun initColor() {
        clog.info("Initializing Dynamic Colors...")
        ifClient {
            registerBlockColor(custard_pudding_block) { _, level, pos, _ ->
                if (level != null && pos != null) {
                    val biome = level.getBiome(pos)
                    if (biome != null) {
                        return@registerBlockColor getBlendedPuddingColor(level, pos, blendRadius)
                    }
                }
                DEFAULT_PUDDING_COLOR
            }
            registerItemColor(
                custard_pudding_block.asItemEntry(),
                color = DEFAULT_PUDDING_COLOR
            )

            registerBlockColor(CBlocks.enchant_candy_leaves) { _, _, pos, _ ->
                if (pos != null) getEnchantColor(pos) else DEFAULT_ENCHANT_COLOR
            }
            registerItemColor(CBlocks.enchant_candy_leaves.asItemEntry(), color = DEFAULT_ENCHANT_COLOR)
        }
    }

}

@ClientOnly
private fun BlockAndTintGetter.getBiome(pos: BlockPos): Holder<Biome>? {
    if (this is LevelReader) return getBiome(pos)
    return Minecraft.getInstance().level?.getBiome(pos)
}