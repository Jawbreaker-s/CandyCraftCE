package cn.jawbreakers.candycraftce.client

import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.asItemEntry
import cn.jawbreakers.candycraftce.registry.CBlocks.custard_pudding_block
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.caramel_forest
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.enchanted_forest
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.ice_cream_plains
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.ice_cream_sky_mountains
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.pudding_hill
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.pudding_plains
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.sugar_forest
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.sugar_oceans
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.sugar_river
import cn.jawbreakers.candycraftce.registry.worldgen.CBiomes.white_chocolate_forest
import cn.jawbreakers.candycraftce.utils.CLevelUtils.component1
import cn.jawbreakers.candycraftce.utils.CLevelUtils.component2
import cn.jawbreakers.candycraftce.utils.CLevelUtils.component3
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit


/**
 * Created by NiceCat on 2025/12/9.
 * Project: candycraftce
 * @author <a href="https://github.com/BreadNiceCat">Bread_NiceCat</a>
 *
 */
@ClientOnly
object PuddingColor {
    //x ~ N(0, 1/6)
    private val noise: NormalNoise = NormalNoise.create(RandomSource.create(8526L), -7, 1.0)
    private val enchant_color = LinearGradient {
        Color(0xb0ecff) % 0f
        Color(0xa376da) % 0.5f
        Color(0xb0b0ff) % 1f
    }

    const val DEFAULT_PUDDING_COLOR = 0xdd99aa
    const val DEFAULT_ENCHANT_COLOR = 0x8f8ac8

    private var blendRadius = 10
    private var visRadius: Int? = null

    private lateinit var cacheEnchant: Cache<Long, Int>

    private lateinit var cachedBlended: Cache<Long, Int>

    /**
     * @return #b0ecff 淡蓝色 #b0b0ff 淡紫色 #a376da 深紫色
     */
    fun getEnchantColor(pos: BlockPos): Int {
        val (x, y, z) = pos
        return cacheEnchant.get(pos.asLong()) {
            val x = 0.5f + (noise.getValue(x + 0.5, y + 0.5, z + 0.5).toFloat())
            val r = x * 2
            enchant_color.getColor(r).rgb
        }
    }

    private fun getPuddingColorRaw(biome: Holder<Biome>, pos: BlockPos): Int {
        return when (biome.unwrapKey().getOrNull()) {
            enchanted_forest -> getEnchantColor(pos)
            pudding_plains, sugar_forest -> 0xEEAABB
            pudding_hill -> 0xEEBBCC
            white_chocolate_forest -> 0xFFDDEE
            ice_cream_plains, ice_cream_sky_mountains -> 0xFFFFFF
            sugar_oceans -> 0xB35EFF
            caramel_forest -> 0xB05C28
            sugar_river -> 0xFFBBCC
            else -> DEFAULT_PUDDING_COLOR
        }
    }

    private var cacheKey: Any? = null
    fun getBlendedPuddingColor(reader: BlockAndTintGetter, pos: BlockPos): Int {
        val level = Minecraft.getInstance().level
        if (cacheKey != level) {
            invalidateAll()
            cacheKey = level
        }
        //
        val startTime = System.nanoTime()
        totalCount++
        val key = pos.asLong()
        var cacheUsed = true
        val result = cachedBlended.getIfPresent(key) ?: run {
            computeCount++
            cacheUsed = false
            val flag = MutableAccessor.create(true)
            computeColor(reader, pos, blendRadius, flag).also {
                if (flag.get()) {
                    cachedBlended.put(key, it)
                }
            }
        }
        val endTime = System.nanoTime()
        val duration = endTime - startTime
        totalTimeNano += duration
        if (cacheUsed) hitTimeNano += duration else computeTimeNano += duration
        return result
    }

    private fun computeColor(
        reader: BlockAndTintGetter,
        pos: BlockPos,
        radius: Int,
        cacheFlag: MutableAccessor<Boolean>,
    ): Int {
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
                    val color = getPuddingColorRaw(biome, mu)
                    r += color.red
                    g += color.green
                    b += color.blue
                    count++
                } else {
                    cacheFlag.set(false)
                }
            }
        }
        return if (count == 0) DEFAULT_PUDDING_COLOR
        else rgb(
            (r + count / 2) / count,
            (g + count / 2) / count,
            (b + count / 2) / count
        )
    }

    @ClientOnly
    fun initColor() {
        clog.info("Initializing Dynamic Colors...")
        ifClient {
            registerBlockColor(custard_pudding_block) { _, level, pos, _ ->
                if (level != null && pos != null) {
                    val biome = level.getBiome(pos)
                    if (biome != null) {
                        return@registerBlockColor getBlendedPuddingColor(level, pos)
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

    @ClientOnly
    private fun BlockAndTintGetter.getBiome(pos: BlockPos): Holder<Biome>? {
        if (this is LevelReader) return getBiome(pos)
        return Minecraft.getInstance().level?.getBiome(pos)
    }

    fun invalidateAll() {
        clog.info("Invalidating Pudding Color Cache")
        if (visRadius != null) {
            cachedBlended.invalidateAll()
            cacheEnchant.invalidateAll()
        }
        val visR = Minecraft.getInstance().options.renderDistance().get() * 16
        if (visRadius != visR) {
            visRadius = visR
            val vsq = visR * visR
            cachedBlended = CacheBuilder.newBuilder().maximumSize(vsq * 4L).build()
            cacheEnchant = CacheBuilder.newBuilder().maximumSize(vsq * 2L).build()
        }
        resetDebugCount()
    }

    //debugs
    @Volatile
    internal var computeTimeNano = 0L

    @Volatile
    internal var totalTimeNano = 0L

    @Volatile
    internal var hitTimeNano = 0L

    @Volatile
    internal var computeCount = 0

    @Volatile
    internal var totalCount = 0
    fun resetDebugCount() {
        computeTimeNano = 0L
        totalTimeNano = 0L
        hitTimeNano = 0L
        computeCount = 0
        totalCount = 0
        prevCalcTime = 0L
        prevTotal = 0
        prevCompute = 0
    }

    var prevCalcTime = 0L
    var prevTotal = 0
    var prevCompute = 0
    var totalOneSec = 0
    var computeOneSec = 0
    fun addDebugScreenInfo(info: MutableList<String>) {
        fun Duration.formatAuto(): String {
            val units = listOf(
                DurationUnit.SECONDS,
                DurationUnit.MILLISECONDS,
                DurationUnit.MICROSECONDS,
                DurationUnit.NANOSECONDS
            )
            val unit = units.firstOrNull { toDouble(it) >= 1.0 } ?: DurationUnit.NANOSECONDS
            return toString(unit, 1)
        }

        val hitCount = totalCount - computeCount
        val totalAvg = if (totalCount == 0) "N/A" else (totalTimeNano / totalCount).nanoseconds.formatAuto()
        val computeAvg = if (computeCount == 0) "N/A" else (computeTimeNano / computeCount).nanoseconds.formatAuto()
        val hitAvg = if (hitCount == 0) "N/A" else (hitTimeNano / hitCount).nanoseconds.formatAuto()
        val computeTimePct = if (totalTimeNano == 0L) 0 else 100 * computeTimeNano / totalTimeNano
        val hitTimePct = if (totalTimeNano == 0L) 0 else 100 * hitTimeNano / totalTimeNano
        val computePct = if (totalCount == 0) 0 else 100 * computeCount / totalCount
        val hitPct = if (totalCount == 0) 0 else 100 * hitCount / totalCount
        val now = System.currentTimeMillis()
        if (now - prevCalcTime > 1000) {
            totalOneSec = totalCount - prevTotal
            computeOneSec = computeCount - prevCompute
            prevTotal = totalCount
            prevCompute = computeCount
            prevCalcTime = now
        }
        val hitOneSec = totalOneSec - computeOneSec
        val vsq = visRadius?.let { it * it } ?: 0
        info.add("§a[PuddingColor-v1 Dashboard]")
        info.add("T:$totalCount, TAvg:$totalAvg, T1s:$totalOneSec")
        info.add("C:$computeCount($computePct%), CAvg:$computeAvg($computeTimePct%), C1s:$computeOneSec")
        info.add("H:$hitCount($hitPct%), HAvg:$hitAvg($hitTimePct%), H1s:$hitOneSec")
        info.add("CBZ:${cachedBlended.size()}/${vsq * 4}, CEZ:${cacheEnchant.size()}/${vsq * 2}")
        info.add("[Press F3+A to reset]")
    }
}
