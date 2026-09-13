@file:Suppress("DuplicatedCode", "PrivatePropertyName")

package cn.jawbreakers.candycraftce.level

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_NAME
import cn.jawbreakers.candycraftce.level.noise.LegacyPerlinOctaveNoise
import cn.jawbreakers.candycraftce.mixin.level.NoiseRouterDataAccessor
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
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import cn.jawbreakers.candycraftce.registry.CFluidTags
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.data.worldgen.SurfaceRuleData
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.WorldGenRegion
import net.minecraft.tags.FluidTags
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.level.*
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.OverworldBiomeBuilder
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.*
import net.minecraft.world.level.levelgen.blending.Blender
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import kotlin.jvm.optionals.getOrDefault
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** 世界最小 Y 坐标（糖果世界从 Y=0 开始）。  */
private const val MIN_Y = 0

/** 世界总高度（256 格）。  */
private const val HEIGHT = 256

/** 海平面高度（Y=63）。  */
private const val SEA_LEVEL = 63

/** 岩浆/液态糖果层高度（Y=10）。  */
private const val LAVA_LEVEL = 10

/** 洞穴雕刻时检查的周围区块范围（±8 区块）。  */
private const val CARVER_RANGE = 8

/** 噪声采样网格尺寸（XZ 方向 5 个采样点）。  */
private const val NOISE_SIZE_XZ = 5

/** 噪声采样网格尺寸（Y 方向 33 个采样点）。  */
private const val NOISE_SIZE_Y = 33

/** 巧克力池塘扫描时最多访问的列数上限，防止性能退化。  */
private const val MAX_POND_SCAN_COLUMNS = 32768

/** 插值单元格宽度（XZ 方向，4 块）。  */
private const val CELL_WIDTH = 4

/** 插值单元格高度（Y 方向，8 块）。  */
private const val CELL_HEIGHT = 8

// 噪声缩放相关常量（与旧版 1.12 地形生成参数保持一致）
private const val COORDINATE_SCALE = 684.412
private const val HEIGHT_SCALE = 684.412
private const val MAIN_NOISE_SCALE_XZ = 80.0
private const val MAIN_NOISE_SCALE_Y = 160.0
private const val LOWER_LIMIT_SCALE = 512.0
private const val UPPER_LIMIT_SCALE = 512.0
private const val DEPTH_NOISE_SCALE_XZ = 200.0

/** 生物群系深度权重。  */
private const val BIOME_DEPTH_WEIGHT = 1.0

/** 生物群系缩放权重。  */
private const val BIOME_SCALE_WEIGHT = 1.0
private const val BIOME_DEPTH_OFFSET = 0.0
private const val BIOME_SCALE_OFFSET = 0.0

/** 基础地形尺寸。  */
private const val BASE_SIZE = 8.5

/** Y 轴拉伸系数。  */
private const val STRETCH_Y = 12.0

/** 抛物线权重场，用于生物群系地形过渡时的平滑插值。  */
private val PARABOLIC_FIELD = FloatArray(25).also {
    for (x in -2..2) {
        for (z in -2..2) {
            it[x + 2 + (z + 2) * 5] = 10.0f / Mth.sqrt(x * x + z * z + 0.2f)
        }
    }
}

//private const val SPAWN_LAND_RADIUS_BLOCKS = 100
//private const val SPAWN_LAND_MAX_RADIUS_BLOCKS = 148

class CandyChunkGenerator(
    val source: CandyBiomeSource,
    val settings: Holder<NoiseGeneratorSettings>,
) : ChunkGenerator(source) {
    companion object {
        val candyland_noise_settings: ResourceKey<NoiseGeneratorSettings> =
            ResourceKey.create(Registries.NOISE_SETTINGS, "candyland_noise_settings".modLoc());

        val codec: Codec<CandyChunkGenerator> = RecordCodecBuilder.create { instance ->
            instance.group(
                CandyBiomeSource.codec.fieldOf("source").forGetter { it.source },
                NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter { it.settings }
            ).apply(instance, ::CandyChunkGenerator)
        }
        val caveRandom = "cave".modLoc()
        val worldRandom = "chunk_generator".modLoc()
        val terrainRandom = "terrain".modLoc()

        private val terrainNoiseCache = ConcurrentHashMap<Long, TerrainNoiseSet>()

        private val air: BlockState = Blocks.AIR.defaultBlockState()
        private val water: BlockState = Blocks.WATER.defaultBlockState()
        private val flat_bottom: BlockState = CBlocks.jawbreaker_block.defaultBlockState()
        private val base_stone: BlockState = CBlocks.crystallized_sugar.defaultBlockState()
        private val top_pudding_block: BlockState = CBlocks.custard_pudding_block.defaultBlockState()
        private val pudding_block = CBlocks.pudding_block.defaultBlockState()
        private val ice_cream = CBlocks.ice_cream.defaultBlockState()
        private val grenadine: BlockState = CBlocks.grenadine.defaultBlockState()

        //岩浆
        private val liquid_candy: BlockState = CBlocks.liquid_candy.defaultBlockState()
        private val liquid_chocolate: BlockState = CBlocks.liquid_chocolate.defaultBlockState()
        fun bootstrap(context: BootstapContext<NoiseGeneratorSettings>) {
            context.register(
                candyland_noise_settings, NoiseGeneratorSettings(
                    NoiseSettings(MIN_Y, HEIGHT, 1, 2),
                    base_stone,
                    Blocks.WATER.defaultBlockState(),
                    NoiseRouterDataAccessor.overworld(
                        context.lookup(Registries.DENSITY_FUNCTION),
                        context.lookup(Registries.NOISE),
                        false,
                        false
                    ),
                    SurfaceRuleData.air(),
                    OverworldBiomeBuilder().spawnTarget(),
                    SEA_LEVEL,
                    false,
                    false,
                    false,
                    false
                )
            )

        }
    }

    override fun codec() = codec

    private fun worldSeed(randomState: RandomState): RandomSource {
        return randomState.getOrCreateRandomFactory(worldRandom).fromHashOf("world")
    }


    private fun scheduleFluidTick(region: WorldGenRegion, pos: BlockPos, state: BlockState) {
        val fluidState = state.fluidState
        if (!fluidState.isEmpty) {
            region.scheduleTick(pos.immutable(), fluidState.type, fluidState.type.getTickDelay(region))
        }
    }

    operator fun ChunkPos.contains(pos: BlockPos): Boolean {
        return pos.x in minBlockX..maxBlockX && pos.z in minBlockZ..maxBlockZ
    }

    /**
     * 唤醒 [changedPos] 六个方向上已有的流体。
     *
     * 世界生成期间直接写入 ChunkAccess 不会触发任何邻居方块更新，因此被我们挖开/替换的位置
     * 旁边那些“本来就存在”的水（或液态糖果、岩浆）会一直保持静止，即便侧边已经变成空气也不会流动。
     * 这里为它们补排一次流体刻，等区块真正开始 ticking 时就会正常扩散，
     * 作用等价于原版 SpringFeature 放完泉水后重新 setBlock 唤醒邻居液体的做法。
     *
     * @param woken 本次生成中已唤醒过的坐标（[BlockPos.asLong]），用于去重，避免同一个水源排队多次
     */
    private fun wakeUpNeighborFluids(
        region: WorldGenRegion,
        chunk: ChunkAccess,
        changedPos: BlockPos,
        woken: MutableSet<Long>,
    ) {
        val cursor = BlockPos.MutableBlockPos()
        for (direction in Direction.entries) {
            val neighbor = cursor.set(
                changedPos.x + direction.stepX,
                changedPos.y + direction.stepY,
                changedPos.z + direction.stepZ,
            )
            // 本区块走 chunk（可读到本次刚写入的状态），越界一格走 region（生成区域含一圈邻接区块）
            val state =
                if (chunk.pos.contains(neighbor)) chunk.getBlockState(neighbor) else region.getBlockState(neighbor)
            val fluidState = state.fluidState
            if (fluidState.isEmpty) {
                continue
            }
            val immutable = neighbor.immutable()
            if (!woken.add(immutable.asLong())) {
                continue
            }
            region.scheduleTick(immutable, fluidState.type, fluidState.type.getTickDelay(region))
        }
    }

    private fun terrainNoiseSet(
        randomState: RandomState,
    ): TerrainNoiseSet {
        val seed = randomState.getOrCreateRandomFactory(terrainRandom)
            .fromHashOf("terrain")
            .nextLong()
        return terrainNoiseCache.computeIfAbsent(seed, ::TerrainNoiseSet)
    }

    private fun sampleDepthNoise(noise: TerrainNoiseSet, noiseX: Double, noiseZ: Double): Double {
        return noise.depth.sampleXZWrapped(noiseX, noiseZ, DEPTH_NOISE_SCALE_XZ, DEPTH_NOISE_SCALE_XZ)
    }

    private fun sampleDensity(
        noise: TerrainNoiseSet,
        noiseX: Double, noiseY: Double, noiseZ: Double,
        heightConfig: HeightConfig, rawDepthNoise: Double = sampleDepthNoise(noise, noiseX, noiseZ),
    ): Double {
        var depthNoise = rawDepthNoise
        depthNoise /= 8000.0

        if (depthNoise < 0.0) {
            depthNoise = -depthNoise * 0.3
        }

        depthNoise = depthNoise * 3.0 - 2.0

        if (depthNoise < 0.0) {
            depthNoise /= 2.0
            depthNoise = max(depthNoise, -1.0)
            depthNoise /= 1.4
            depthNoise /= 2.0
        } else {
            depthNoise = min(depthNoise, 1.0)
            depthNoise /= 8.0
        }

        var depth = heightConfig.depth + depthNoise * 0.2
        depth *= BASE_SIZE / 8.0
        depth = BASE_SIZE + depth * 4.0

        var densityOffset: Double = (noiseY - depth) * STRETCH_Y / heightConfig.scale
        if (densityOffset < 0.0) {
            densityOffset *= 4.0
        }

        val mainNoise: Double = (noise.main.sampleWrapped(
            noiseX,
            noiseY,
            noiseZ,
            COORDINATE_SCALE / MAIN_NOISE_SCALE_XZ,
            HEIGHT_SCALE / MAIN_NOISE_SCALE_Y,
            COORDINATE_SCALE / MAIN_NOISE_SCALE_XZ
        ) / 10.0 + 1.0) / 2.0

        var density: Double
        if (mainNoise < 0.0) {
            density = noise.minLimit.sampleWrapped(
                noiseX,
                noiseY,
                noiseZ,
                COORDINATE_SCALE,
                HEIGHT_SCALE,
                COORDINATE_SCALE
            ) / LOWER_LIMIT_SCALE
        } else if (mainNoise > 1.0) {
            density = noise.maxLimit.sampleWrapped(
                noiseX,
                noiseY,
                noiseZ,
                COORDINATE_SCALE,
                HEIGHT_SCALE,
                COORDINATE_SCALE
            ) / UPPER_LIMIT_SCALE
        } else {
            val minLimitNoise: Double = noise.minLimit.sampleWrapped(
                noiseX,
                noiseY,
                noiseZ,
                COORDINATE_SCALE,
                HEIGHT_SCALE,
                COORDINATE_SCALE
            ) / LOWER_LIMIT_SCALE
            val maxLimitNoise: Double = noise.maxLimit.sampleWrapped(
                noiseX,
                noiseY,
                noiseZ,
                COORDINATE_SCALE,
                HEIGHT_SCALE,
                COORDINATE_SCALE
            ) / UPPER_LIMIT_SCALE
            density = Mth.lerp(mainNoise, minLimitNoise, maxLimitNoise)
        }

        density -= densityOffset

        if (noiseY > 29.0) {
            val topFade = (noiseY - 29.0) / 3.0
            density = Mth.lerp(topFade, density, -10.0)
        }

        return density
    }


    private fun isBaseStone(state: BlockState): Boolean {
        return state.`is`(base_stone.block)
    }

    private fun surfaceMaterials(
        biomeId: ResourceKey<Biome>,
        worldX: Int,
        worldZ: Int,
        randomState: RandomState,
    ): SurfaceMaterials {
        return when (biomeId) {
            cotton_candy_plains ->
                SurfaceMaterials(/*TODO CBlocks.candy_grass_block.defaultBlockState()*/
                    Blocks.GRASS_BLOCK.defaultBlockState(),
                    CBlocks.milk_brownie_block.defaultBlockState()
                )

            chocolate_forest ->
                SurfaceMaterials(
                    CBlocks.chocolate_covered_white_brownie.defaultBlockState(),
                    CBlocks.white_brownie_block.defaultBlockState()
                )

            gummy_swamp -> gummySurfaceMaterials(worldX, worldZ, randomState)

            ice_cream_sky_mountains ->
                SurfaceMaterials(
                    ice_cream,
                    pudding_block
                )

            else -> SurfaceMaterials(top_pudding_block, pudding_block, pudding_block)
        }
    }

    private fun gummySurfaceMaterials(worldX: Int, worldZ: Int, randomState: RandomState): SurfaceMaterials {
        val noise: Double = octaveNoise2D(
            worldX * 0.0075,
            worldZ * 0.0075,
            4,
            worldSeed(randomState).nextLong()
        ) * 12.0
        var index = (noise * 1.6).toInt() % 10
        if (index < 0) {
            index += 10
        }
        return when (index) {
            1, 8 -> CBlocks.orange_gummy_family
            2, 5, 7 -> CBlocks.yellow_gummy_family
            3, 4 -> CBlocks.green_gummy_family
            6 -> CBlocks.white_gummy_family
            else -> CBlocks.red_gummy_family
        }.run {
            SurfaceMaterials(
                block.defaultBlockState(), hardened.defaultBlockState(), block.defaultBlockState()
            )
        }
    }

    private fun underwaterMaterial(replaced: Int): BlockState {
        return if (replaced == 0)
            CBlocks.sugar_sand.defaultBlockState()
        else
            CBlocks.pudding_block.defaultBlockState()
    }


    /** 生物群系形状缓存（记录每个列的基础高度和起伏）。  */
    private val biomeShapeCache = ConcurrentHashMap<Long, BiomeShape>()

    /** 巧克力池塘扫描结果缓存（记录哪些水列应变为巧克力）。  */
    private val pondChocolateCache = ConcurrentHashMap<Long, Boolean>()

    /** 开放水域列缓存。  */
    private val openWaterColumnCache = ConcurrentHashMap<Long, Boolean>()

    override fun getBiomeSource() = super.biomeSource as CandyBiomeSource

    override fun fillFromNoise(
        executor: Executor, blender: Blender, randomState: RandomState,
        structureManager: StructureManager, chunk: ChunkAccess,
    ): CompletableFuture<ChunkAccess> {
        return CompletableFuture.supplyAsync({
            fillTerrain(chunk, randomState)
            Heightmap.primeHeightmaps(
                chunk, setOf<Heightmap.Types?>(
                    Heightmap.Types.WORLD_SURFACE_WG,
                    Heightmap.Types.OCEAN_FLOOR_WG,
                    Heightmap.Types.MOTION_BLOCKING,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
                )
            )
            chunk
        }, executor)
    }

    private fun fillTerrain(chunk: ChunkAccess, randomState: RandomState) {
        val pos = chunk.pos
        // 生成该区块的密度图（4×4 单元格对应 5×5 噪声采样点，Y 方向 33 个采样点）
        val heightMap = generateHeightMap(terrainNoiseSet(randomState), pos.x * 4, pos.z * 4, randomState)
        val mutable = BlockPos.MutableBlockPos()
        val worldXs = IntArray(16) { pos.getBlockX(it) }
        val worldZs = IntArray(16) { pos.getBlockZ(it) }
        // 预计算每个列的液体类型（水或巧克力），避免在每个 Y 层级重复查询生物群系
        val columnFluids = Array(256) {
            val localX = it / 16
            val localZ = it % 16
            fluidForColumn(worldXs[localX], worldZs[localZ], randomState)
        }
        for (localX in 0..15) {
            for (localZ in 0..15) {
                columnFluids[localX * 16 + localZ] = fluidForColumn(worldXs[localX], worldZs[localZ], randomState)
            }
        }
        // 遍历单元格（4×4 个，Y 方向 32 个单元格 = 256 格）
        for (cellX in 0..3) {
            val x0: Int = cellX * NOISE_SIZE_XZ
            val x1: Int = (cellX + 1) * NOISE_SIZE_XZ

            for (cellZ in 0..3) {
                // 对应四个角点的密度数组索引
                val z0: Int = (x0 + cellZ) * NOISE_SIZE_Y
                val z1: Int = (x0 + cellZ + 1) * NOISE_SIZE_Y
                val z2: Int = (x1 + cellZ) * NOISE_SIZE_Y
                val z3: Int = (x1 + cellZ + 1) * NOISE_SIZE_Y

                for (cellY in 0..31) {
                    // 读取单元格八个角点的密度值
                    var density000 = heightMap[z0 + cellY]
                    var density001 = heightMap[z1 + cellY]
                    var density100 = heightMap[z2 + cellY]
                    var density101 = heightMap[z3 + cellY]

                    // 计算 Y 方向步长（用于后续线性插值）
                    val step000: Double = (heightMap[z0 + cellY + 1] - density000) / CELL_HEIGHT
                    val step001: Double = (heightMap[z1 + cellY + 1] - density001) / CELL_HEIGHT
                    val step100: Double = (heightMap[z2 + cellY + 1] - density100) / CELL_HEIGHT
                    val step101: Double = (heightMap[z3 + cellY + 1] - density101) / CELL_HEIGHT

                    // 对单元格内部进行三线性插值
                    for (subY in 0..<CELL_HEIGHT) {
                        var yLerp0 = density000
                        var yLerp1 = density001
                        val xStep0: Double = (density100 - density000) / CELL_WIDTH
                        val xStep1: Double = (density101 - density001) / CELL_WIDTH

                        for (subX in 0..<CELL_WIDTH) {
                            var density = yLerp0
                            val zStep: Double = (yLerp1 - yLerp0) / CELL_WIDTH

                            for (subZ in 0..<CELL_WIDTH) {
                                val localX: Int = cellX * CELL_WIDTH + subX
                                val localZ: Int = cellZ * CELL_WIDTH + subZ
                                val worldX = worldXs[localX]
                                val worldZ = worldZs[localZ]
                                val y: Int = cellY * CELL_HEIGHT + subY

                                // 密度 > 0 → 固体（结晶糖）；否则海平面以下为液体，以上为空气
                                val state: BlockState = when {
                                    density > 0.0 -> base_stone
                                    y < SEA_LEVEL -> columnFluids[localX * 16 + localZ]
                                    else -> air
                                }
                                if (!state.isAir && y >= chunk.minBuildHeight && y < chunk.maxBuildHeight) {
                                    chunk.setBlockState(mutable.set(worldX, y, worldZ).immutable(), state, false)
                                }
                                density += zStep
                            }

                            yLerp0 += xStep0
                            yLerp1 += xStep1
                        }

                        density000 += step000
                        density001 += step001
                        density100 += step100
                        density101 += step101
                    }
                }
            }
        }
        // 生成底部平整层和洞穴
        applyBedrock(chunk)
        carveCaves(chunk, randomState)
    }

    /**
     * 生成 5×5×33 的密度图。
     *
     * @param baseNoiseX 区块对应的噪声起始 X 坐标（区块 X × 4）
     * @param baseNoiseZ 区块对应的噪声起始 Z 坐标（区块 Z × 4）
     */
    private fun generateHeightMap(
        noise: TerrainNoiseSet,
        baseNoiseX: Int,
        baseNoiseZ: Int,
        randomState: RandomState,
    ): DoubleArray {
        val heightMap = DoubleArray(NOISE_SIZE_XZ * NOISE_SIZE_Y * NOISE_SIZE_XZ)
        var index = 0

        for (x in 0..<NOISE_SIZE_XZ) {
            for (z in 0..<NOISE_SIZE_XZ) {
                // 获取该采样点的生物群系高度配置
                val heightConfig =
                    heightConfigAt((baseNoiseX + x).toDouble(), (baseNoiseZ + z).toDouble(), randomState)
                // 采样深度噪声
                val depthNoise = sampleDepthNoise(noise, (baseNoiseX + x).toDouble(), (baseNoiseZ + z).toDouble())

                for (y in 0..<NOISE_SIZE_Y) {
                    heightMap[index++] = sampleDensity(
                        noise,
                        (baseNoiseX + x).toDouble(),
                        y.toDouble(),
                        (baseNoiseZ + z).toDouble(),
                        heightConfig,
                        depthNoise
                    )
                }
            }
        }

        return heightMap
    }

    // ========================== 地表生成 ==========================
    /**
     * 构建地表（阶段：buildSurface）。
     * 覆盖在 fillFromNoise 生成的基础地形之上，根据生物群系放置地表方块。
     * 还会生成地表池塘、山脉糖果泉和粉色结晶糖装饰。
     */
    override fun buildSurface(
        region: WorldGenRegion, structureManager: StructureManager, randomState: RandomState,
        chunk: ChunkAccess,
    ) {
        val mutable = BlockPos.MutableBlockPos()
        val pos = chunk.pos

        // 遍历每个列，用群系对应的地表材质替换顶层方块
        for (localX in 0..15) {
            val worldX = pos.getBlockX(localX)
            for (localZ in 0..15) {
                val worldZ = pos.getBlockZ(localZ)
                val biomeId = biomeId(worldX, worldZ, randomState)
                val materials: SurfaceMaterials = surfaceMaterials(biomeId, worldX, worldZ, randomState)
                val top = findTopSolid(chunk, worldX, worldZ)
                // 判断是否为水下环境（海平面以下，或海洋/河流群系）
                val underwater = top < SEA_LEVEL - 1 || biomeId == sugar_oceans || biomeId == sugar_river
                // 地表替换深度：3~5 格，带随机变化
                val depth: Int = 3 + abs(hash(worldX, 0, worldZ)) % 3
                var replaced = 0

                var y = top
                while (y > MIN_Y && replaced <= depth) {
                    val state = chunk.getBlockState(mutable.set(worldX, y, worldZ))
                    if (!isBaseStone(state)) {
                        // 如果已经开始替换但遇到非基岩方块，停止向下
                        if (replaced > 0) {
                            break
                        }
                        --y
                        continue
                    }

                    // 水下使用水下材质，非水下则使用表层/次表层材质
                    val replacement =
                        (if (underwater) underwaterMaterial(replaced) else if (replaced > 0) materials.under else materials.top)!!
                    chunk.setBlockState(mutable, replacement, false)
                    replaced++
                    --y
                }
            }
        }

        // 生成地表池塘（石榴糖浆 / 液态糖果）
        generateSurfacePools(region, chunk, randomState)
        // 生成山脉糖果泉
        generateMountainCandySprings(region, chunk, randomState)
        // 生成粉色结晶糖装饰
        decoratePinkCrystallizedSugar(region, chunk, randomState)
    }

    /**
     * 在粉色糖浆（液态糖果）周围生成稀疏的粉色结晶糖边框。
     * 这个装饰是稀疏扫描，每列只有 1/8 的概率会被检查，以避免影响区块加载性能。
     */
    private fun decoratePinkCrystallizedSugar(
        region: WorldGenRegion,
        chunk: ChunkAccess,
        randomState: RandomState,
    ) {
        val chunkPos = chunk.pos
        val seed: Long = worldSeed(randomState).nextLong()
        val maxY = min(chunk.maxBuildHeight - 1, HEIGHT - 1)
        val liquidPos = BlockPos.MutableBlockPos()
        val targetPos = BlockPos.MutableBlockPos()

        for (localX in 0..15) {
            val worldX = chunkPos.getBlockX(localX)
            for (localZ in 0..15) {
                val worldZ = chunkPos.getBlockZ(localZ)
                // 稀疏扫描：仅处理哈希值低 3 位为 0 的列（概率 1/8）
                if (positiveHash(worldX, 0, worldZ, seed) and 7L != 0L) {
                    continue
                }
                for (worldY in MIN_Y + 1..maxY) {
                    val liquid = chunk.getBlockState(liquidPos.set(worldX, worldY, worldZ)).fluidState
                    // 找到液态糖果方块
                    if (!liquid.`is`(CFluidTags.liquid_candy)) {
                        continue
                    }

                    // 检查液态糖果的六个相邻方向
                    for (direction in Direction.entries) {
                        val targetX = worldX + direction.stepX
                        val targetY = worldY + direction.stepY
                        val targetZ = worldZ + direction.stepZ
                        // 确保目标位置在区块范围内
                        if (targetY !in 1..maxY || targetX < chunkPos.minBlockX || targetX > chunkPos.maxBlockX || targetZ < chunkPos.minBlockZ || targetZ > chunkPos.maxBlockZ) {
                            continue
                        }

                        val target = chunk.getBlockState(targetPos.set(targetX, targetY, targetZ))
                        // 跳过空气、液体、基岩和已存在的粉色结晶糖
                        if (target.isAir || !target.fluidState.isEmpty
                            || target.`is`(Blocks.BEDROCK)
                            || target.`is`(
                                CBlocks.pink_crystallized_sugar.get()
                            )
                        ) {
                            continue
                        }

                        // 判断目标是否靠近地表（影响生成概率）
                        val surface = targetY >= SEA_LEVEL - 4
                        val validTarget = isBaseStone(target)
                                || surface
                                && target.isCollisionShapeFullBlock(region, targetPos)
                        if (!validTarget) {
                            continue
                        }

                        // 生成概率：朝下概率更高，地表概率更高
                        val chance =
                            if (direction == Direction.DOWN) (if (surface) 10 else 22) else if (surface) 18 else 32
                        if (positiveHash(targetX, targetY, targetZ, seed) % chance == 0L) {
                            chunk.setBlockState(
                                targetPos.immutable(),
                                CBlocks.pink_crystallized_sugar.defaultBlockState(),
                                false
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 生成地表池塘。
     * 每个区块有低概率生成石榴糖浆池塘（1/1200）或液态糖果池塘（1/1400）。
     */
    private fun generateSurfacePools(region: WorldGenRegion, chunk: ChunkAccess, randomState: RandomState) {
        val pos = chunk.pos
        val seed: Long = worldSeed(randomState).nextLong()
        // 石榴糖浆池塘判定
        val poolRoll: Long = positiveHash(pos.x, pos.z, 0, seed xor 0x4752454E4144494EL) // 'GRENADIE'
        if (poolRoll % 1200L == 0L) {
            generateSurfacePool(region, chunk, randomState, grenadine, seed xor 0x6C616B655F677265L)
        }

        // 液态糖果池塘判定
        val candyRoll: Long = positiveHash(pos.x, pos.z, 0, seed xor 0x4C49515549444341L) // 'LIQUIDCA'
        if (candyRoll % 1400L == 0L) {
            generateSurfacePool(region, chunk, randomState, liquid_candy, seed xor 0x6C616B655F63616EL)
        }
    }

    /**
     * 生成单个地表池塘（由多个椭球体拼接而成）。
     * 池塘中心位于区块中心，深度约 3 格，使用流体填充。
     *
     * @param fluid 池塘使用的流体
     * @param salt  随机种子盐
     */
    private fun generateSurfacePool(
        region: WorldGenRegion,
        chunk: ChunkAccess,
        randomState: RandomState,
        fluid: BlockState,
        salt: Long,
    ) {
        val pos = chunk.pos
        val bits: Long = positiveHash(pos.x, pos.z, 0, salt)
        val centerX = pos.getBlockX(8)
        val centerZ = pos.getBlockZ(8)
        val biomeId = biomeId(centerX, centerZ, randomState)
        // 海洋/河流群系不生成池塘
        if (biomeId == sugar_oceans || biomeId == sugar_river) {
            return
        }

        val centerTop = findTopTerrain(chunk, centerX, centerZ)
        // 限制生成高度范围
        if (centerTop <= SEA_LEVEL - 4 || centerTop >= HEIGHT - 3) {
            return
        }

        val random = Random(bits)
        // 使用多个随机椭球体构建池塘形状
        val lake = BooleanArray(16 * 16 * 8)
        val ellipsoids = 4 + random.nextInt(4)
        repeat(ellipsoids) {
            val sizeX = random.nextDouble() * 6.0 + 3.0
            val sizeY = random.nextDouble() * 4.0 + 2.0
            val sizeZ = random.nextDouble() * 6.0 + 3.0
            val ellipsoidX = random.nextDouble() * (16.0 - sizeX - 2.0) + 1.0 + sizeX / 2.0
            val ellipsoidY = random.nextDouble() * (8.0 - sizeY - 4.0) + 2.0 + sizeY / 2.0
            val ellipsoidZ = random.nextDouble() * (16.0 - sizeZ - 2.0) + 1.0 + sizeZ / 2.0
            // 填充椭圆体内部
            for (x in 1..14) {
                val dx = (x - ellipsoidX) / (sizeX / 2.0)
                for (z in 1..14) {
                    val dz = (z - ellipsoidZ) / (sizeZ / 2.0)
                    for (y in 1..6) {
                        val dy = (y - ellipsoidY) / (sizeY / 2.0)
                        if (dx * dx + dy * dy + dz * dz < 1.0) {
                            lake[(x * 16 + z) * 8 + y] = true
                        }
                    }
                }
            }
        }

        val mutable = BlockPos.MutableBlockPos()
        val waterline = centerTop - 1
        val originY = waterline - 3
        val woken = HashSet<Long>()
        // 填充池塘流体
        for (x in 0..15) {
            val worldX = pos.minBlockX + x
            for (z in 0..15) {
                val worldZ = pos.minBlockZ + z
                // 跳过地形高度变化过大的列
                if (abs(findTopTerrain(chunk, worldX, worldZ) - centerTop) > 4) {
                    continue
                }
                for (y in 0..7) {
                    if (!lake[(x * 16 + z) * 8 + y]) {
                        continue
                    }
                    val worldY = originY + y
                    if (worldY <= chunk.minBuildHeight || worldY >= chunk.maxBuildHeight) {
                        continue
                    }
                    // 池塘底部 4 层填流体，上方填空气
                    val state = if (y < 4) fluid else air
                    chunk.setBlockState(mutable.set(worldX, worldY, worldZ), state, false)
                    scheduleFluidTick(region, mutable, state)
                    // 被替换掉的地形原本可能正挡着附近的水，必须一并唤醒，否则那些水会永远静止
                    wakeUpNeighborFluids(region, chunk, mutable, woken)
                }
            }
        }

        // 在池塘底部铺设地表下层材质
        for (x in 0..15) {
            val worldX = pos.minBlockX + x
            for (z in 0..15) {
                val worldZ = pos.minBlockZ + z
                val localBiome = biomeId(worldX, worldZ, randomState)
                val rim: BlockState = surfaceMaterials(localBiome, worldX, worldZ, randomState).under
                for (y in 0..7) {
                    if (!lake[(x * 16 + z) * 8 + y] || y >= 4) {
                        continue
                    }
                    val worldY = originY + y
                    val floor: BlockPos = mutable.set(worldX, worldY - 1, worldZ)
                    if (worldY > chunk.minBuildHeight && chunk.getBlockState(floor).isAir) {
                        chunk.setBlockState(floor, rim, false)
                    }
                }
            }
        }
    }

    /**
     * 在山脉生物群系中生成糖果泉。
     * 每个区块有 1/90 概率尝试，仅在糖山脉、地狱糖果山脉或冰淇淋天空山脉中生成。
     */
    private fun generateMountainCandySprings(region: WorldGenRegion, chunk: ChunkAccess, randomState: RandomState) {
        val pos = chunk.pos
        val seed = worldSeed(randomState).nextLong()
        val roll = positiveHash(pos.x, pos.z, 0, seed xor 0x4D544E5F53595250L) // 'MTN_SYRP'
        if (roll % 90L != 0L) return

        // 从区块内随机选择一个位置
        val worldX = pos.getBlockX(2 + (roll ushr 8 and 11L).toInt())
        val worldZ = pos.getBlockZ(2 + (roll ushr 12 and 11L).toInt())
        val biomeId = biomeId(worldX, worldZ, randomState)
        if (biomeId != sugar_mountains && biomeId != sugar_hell_mountains && biomeId != ice_cream_sky_mountains) {
            return
        }

        val top = findTopTerrain(chunk, worldX, worldZ)
        if (top < SEA_LEVEL + 8 || top > HEIGHT - 8) {
            return
        }

        val mutable = BlockPos.MutableBlockPos()
        for (attempt in 0..5) {
            val y = top - 2 - attempt * 2
            if (y <= SEA_LEVEL/* || y >= HEIGHT - 2*/) {
                continue
            }
            if (tryPlaceMountainCandySpring(
                    region, chunk, mutable, worldX, y, worldZ,
                    if (biomeId == sugar_hell_mountains) liquid_candy else grenadine
                )
            ) return
        }
    }

    private fun tryPlaceMountainCandySpring(
        region: WorldGenRegion,
        chunk: ChunkAccess,
        mutable: BlockPos.MutableBlockPos,
        worldX: Int,
        y: Int,
        worldZ: Int,
        fluid: BlockState,
    ): Boolean {
        if (!isBaseStone(chunk.getBlockState(mutable.set(worldX, y, worldZ)))) {
            return false
        }

        var airSides = 0
        var solidSides = 0
        var outX = worldX
        var outZ = worldZ
        val directions = arrayOf<IntArray>(intArrayOf(1, 0), intArrayOf(-1, 0), intArrayOf(0, 1), intArrayOf(0, -1))
        for (direction in directions) {
            val neighbor = chunk.getBlockState(mutable.set(worldX + direction[0], y, worldZ + direction[1]))
            if (neighbor.isAir) {
                airSides++
                outX = worldX + direction[0]
                outZ = worldZ + direction[1]
            } else if (isBaseStone(neighbor)) {
                solidSides++
            }
        }

        if (airSides != 1 || solidSides < 3 || !isBaseStone(
                chunk.getBlockState(
                    mutable.set(
                        worldX,
                        y - 1,
                        worldZ
                    )
                )
            )
        ) {
            return false
        }

        val woken = HashSet<Long>(12)
        chunk.setBlockState(mutable.set(worldX, y, worldZ), fluid, false)
        scheduleFluidTick(region, mutable, fluid)
        wakeUpNeighborFluids(region, chunk, mutable, woken)
        chunk.setBlockState(mutable.set(outX, y, outZ), fluid, false)
        scheduleFluidTick(region, mutable, fluid)
        wakeUpNeighborFluids(region, chunk, mutable, woken)
        return true
    }

    override fun applyCarvers(
        region: WorldGenRegion, seed: Long, randomState: RandomState, biomeManager: BiomeManager,
        structureManager: StructureManager, chunk: ChunkAccess, carvingStep: GenerationStep.Carving,
    ) {
    }

    override fun spawnOriginalMobs(region: WorldGenRegion) {
        val chunkPos = region.center
        val center = BlockPos(chunkPos.minBlockX + 8, SEA_LEVEL, chunkPos.minBlockZ + 8)
        val biome = region.getBiome(center)
        val random = RandomSource.create(region.seed)
        NaturalSpawner.spawnMobsForChunkGeneration(region, biome, chunkPos, random)
    }

    override fun getGenDepth(): Int {
        return HEIGHT
    }

    override fun getSeaLevel(): Int {
        return SEA_LEVEL
    }

    override fun getMinY(): Int {
        return MIN_Y
    }

    override fun getBaseHeight(
        x: Int, z: Int, type: Heightmap.Types, heightAccessor: LevelHeightAccessor,
        randomState: RandomState,
    ): Int {
        val column = buildColumn(x, z, heightAccessor, randomState).reversed()
        for (i in column.indices.reversed()) {
            val state = column[i]
            if (type.isOpaque().test(state) || type == Heightmap.Types.WORLD_SURFACE_WG && !state.isAir) {
                return heightAccessor.minBuildHeight + i + 1
            }
        }
        return heightAccessor.minBuildHeight
    }

    override fun getBaseColumn(
        x: Int,
        z: Int,
        heightAccessor: LevelHeightAccessor,
        randomState: RandomState,
    ): NoiseColumn {
        return NoiseColumn(heightAccessor.minBuildHeight, buildColumn(x, z, heightAccessor, randomState))
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun addDebugScreenInfo(info: MutableList<String?>, randomState: RandomState, pos: BlockPos) {
        info.add("$MOD_NAME Chunk Generator v1.0: S=${worldSeed(randomState).nextLong().toHexString()}")
        info.add("Candy Biome Shape: " + biomeShape(pos.x, pos.z, randomState))
    }

    private fun buildColumn(
        x: Int,
        z: Int,
        heightAccessor: LevelHeightAccessor,
        randomState: RandomState,
    ): Array<BlockState> {
        val minY = heightAccessor.minBuildHeight
        val height = heightAccessor.height
        val noiseX = x / CELL_WIDTH.toDouble()
        val noiseZ = z / CELL_WIDTH.toDouble()
        val noise = terrainNoiseSet(randomState)
        val heightConfig = heightConfigAt(Mth.floor(noiseX).toDouble(), Mth.floor(noiseZ).toDouble(), randomState)
        val columnFluid = fluidForColumn(x, z, randomState)
        val states: Array<BlockState> = Array(height) { i ->
            val y = minY + i
            val noiseY = y / CELL_HEIGHT.toDouble()
            val density = sampleDensity(noise, noiseX, noiseY, noiseZ, heightConfig)
            when {
                y <= MIN_Y + 1 -> flat_bottom
                density > 0.0 -> base_stone
                y < SEA_LEVEL -> columnFluid
                else -> air
            }
        }

        return states
    }

    private fun heightConfigAt(noiseX: Double, noiseZ: Double, randomState: RandomState): HeightConfig {
//        val random = worldSeed(randomState)
        val center = biomeShape(Mth.floor(noiseX * CELL_WIDTH), Mth.floor(noiseZ * CELL_WIDTH), randomState)
        var scale = 0.0
        var depth = 0.0
        var totalWeight = 0.0

        for (offX in -2..2) {
            for (offZ in -2..2) {
                val nearby = biomeShape(
                    Mth.floor((noiseX + offX) * CELL_WIDTH),
                    Mth.floor((noiseZ + offZ) * CELL_WIDTH),
                    randomState
                )
                val nearbyScale: Double = BIOME_SCALE_OFFSET + nearby.variation * BIOME_SCALE_WEIGHT
                val nearbyDepth: Double = BIOME_DEPTH_OFFSET + nearby.baseHeight * BIOME_DEPTH_WEIGHT
                var weight: Double = PARABOLIC_FIELD[offX + 2 + (offZ + 2) * 5] / max(nearbyDepth + 2.0, 0.01)

                if (nearby.baseHeight > center.baseHeight) {
                    weight *= 0.5
                }

                scale += nearbyScale * weight
                depth += nearbyDepth * weight
                totalWeight += weight
            }
        }

        scale /= totalWeight
        depth /= totalWeight
        scale = scale * 0.9 + 0.1
        depth = (depth * 4.0 - 1.0) / 8.0

//        val blockX: Double = noiseX * CELL_WIDTH
//        val blockZ: Double = noiseZ * CELL_WIDTH
//        val spawnIsland: Double = spawnIslandInfluence(blockX, blockZ, random)
//        if (spawnIsland > 0.0) {
//            val islandNoise: Double = octaveNoise2D(blockX * 0.018, blockZ * 0.018, 4, random.nextLong())
//            val detailNoise: Double = octaveNoise2D(blockX * 0.055, blockZ * 0.055, 2, random.nextLong())
//            depth += spawnIsland * (0.045 + islandNoise * 0.035 + detailNoise * 0.012)
//            scale += spawnIsland * (0.035 + abs(islandNoise) * 0.025)
//        }

        return HeightConfig(depth, scale)
    }

    private fun applyBedrock(chunk: ChunkAccess) {
        val pos = chunk.pos
        val mutable = BlockPos.MutableBlockPos()

        for (localX in 0..15) {
            val worldX = pos.getBlockX(localX)
            for (localZ in 0..15) {
                val worldZ = pos.getBlockZ(localZ)
                chunk.setBlockState(mutable.set(worldX, MIN_Y, worldZ).immutable(), flat_bottom, false)
                chunk.setBlockState(mutable.set(worldX, MIN_Y + 1, worldZ).immutable(), flat_bottom, false)
            }
        }
    }

    private fun carveCaves(chunk: ChunkAccess, randomState: RandomState) {
        val pos = chunk.pos
        val factory = randomState.getOrCreateRandomFactory(caveRandom)
        for (sourceChunkX in pos.x - CARVER_RANGE..pos.x + CARVER_RANGE) {
            for (sourceChunkZ in pos.z - CARVER_RANGE..pos.z + CARVER_RANGE) {
                val random = factory.at(sourceChunkX, 0, sourceChunkZ)
                carveCaveStarts(chunk, sourceChunkX, sourceChunkZ, random)
                carveRavineStart(chunk, sourceChunkX, sourceChunkZ, random)
            }
        }
    }

    private fun carveCaveStarts(chunk: ChunkAccess, sourceChunkX: Int, sourceChunkZ: Int, random: RandomSource) {
        var caveCount = random.nextInt(random.nextInt(random.nextInt(40) + 1) + 1)
        if (random.nextInt(15) != 0) {
            caveCount = 0
        }
        repeat(caveCount) {
            val x = (sourceChunkX * 16 + random.nextInt(16)).toDouble()
            val y = random.nextInt(random.nextInt(120) + 8).toDouble()
            val z = (sourceChunkZ * 16 + random.nextInt(16)).toDouble()
            var tunnelCount = 1

            if (random.nextInt(4) == 0) {
//                carveCaveRoom(chunk, random, x, y, z)
                carveTunnel(chunk, random, x, y, z, 1.0f + random.nextFloat() * 6.0f, 0.0f, 0.0f, -1, -1, 0.5)
                tunnelCount += random.nextInt(4)
            }

            repeat(tunnelCount) {
                val yaw = random.nextFloat() * Mth.PI * 2.0f
                val pitch = (random.nextFloat() - 0.5f) * 0.25f
                var width = random.nextFloat() * 2.0f + random.nextFloat()
                if (random.nextInt(10) == 0) {
                    width *= random.nextFloat() * random.nextFloat() * 3.0f + 1.0f
                }

                carveTunnel(chunk, random, x, y, z, width, yaw, pitch, 0, 0, 1.0)
            }
        }
    }


    private fun carveTunnel(
        chunk: ChunkAccess, random: RandomSource, x: Double, y: Double, z: Double, width: Float,
        yaw: Float, pitch: Float, branch: Int, branchCount: Int, yawPitchRatio: Double,
    ) {
        var x = x
        var y = y
        var z = z
        var yaw = yaw
        var pitch = pitch
        var branch = branch
        var branchCount = branchCount
        val pos = chunk.pos
        val centerX = (pos.x * 16 + 8).toDouble()
        val centerZ = (pos.z * 16 + 8).toDouble()
        var yawVelocity = 0.0f
        var pitchVelocity = 0.0f

        if (branchCount <= 0) {
            val maxBranchCount: Int = CARVER_RANGE * 16 - 16
            branchCount = maxBranchCount - random.nextInt(maxBranchCount / 4)
        }

        var room = false
        if (branch == -1) {
            branch = branchCount / 2
            room = true
        }

        val splitBranch = random.nextInt(branchCount / 2) + branchCount / 4
        val slowPitch = random.nextInt(6) == 0

        while (branch < branchCount) {
            val horizontalScale = 1.5 + Mth.sin(branch.toFloat() * Mth.PI / branchCount.toFloat()) * width
            val verticalScale = horizontalScale * yawPitchRatio
            val pitchCos = Mth.cos(pitch)

            x += (Mth.cos(yaw) * pitchCos).toDouble()
            y += Mth.sin(pitch).toDouble()
            z += (Mth.sin(yaw) * pitchCos).toDouble()
            pitch *= if (slowPitch) 0.92f else 0.7f
            pitch += pitchVelocity * 0.1f
            yaw += yawVelocity * 0.1f
            pitchVelocity *= 0.9f
            yawVelocity *= 0.75f
            pitchVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0f
            yawVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0f

            if (!room && branch == splitBranch && width > 1.0f) {
                carveTunnel(
                    chunk, random, x, y, z, random.nextFloat() * 0.5f + 0.5f,
                    yaw - Mth.HALF_PI, pitch / 3.0f, branch, branchCount, 1.0
                )
                carveTunnel(
                    chunk, random, x, y, z, random.nextFloat() * 0.5f + 0.5f,
                    yaw + Mth.HALF_PI, pitch / 3.0f, branch, branchCount, 1.0
                )
                return
            }

            if (!room && random.nextInt(4) == 0) {
                ++branch
                continue
            }

            val dx = x - centerX
            val dz = z - centerZ
            val remaining = (branchCount - branch).toDouble()
            val maxDistance = (width + 18.0f).toDouble()
            if (dx * dx + dz * dz - remaining * remaining > maxDistance * maxDistance) {
                return
            }

            carveRegion(chunk, x, y, z, horizontalScale, verticalScale)
            if (room) break
            ++branch
        }
    }

    private fun carveRavineStart(chunk: ChunkAccess, sourceChunkX: Int, sourceChunkZ: Int, random: RandomSource) {
        if (random.nextInt(50) != 0) {
            return
        }

        val x = (sourceChunkX * 16 + random.nextInt(16)).toDouble()
        val y = (random.nextInt(random.nextInt(40) + 8) + 20).toDouble()
        val z = (sourceChunkZ * 16 + random.nextInt(16)).toDouble()
        val yaw = random.nextFloat() * Mth.PI * 2.0f
        val pitch = (random.nextFloat() - 0.5f) * 0.25f
        val width = (random.nextFloat() * 2.0f + random.nextFloat()) * 2.0f
        carveRavine(chunk, random, x, y, z, width, yaw, pitch, 0, 0)
    }

    private fun carveRavine(
        chunk: ChunkAccess, random: RandomSource, x: Double, y: Double, z: Double, width: Float,
        yaw: Float, pitch: Float, branch: Int, branchCount: Int,
    ) {
        var x = x
        var y = y
        var z = z
        var yaw = yaw
        var pitch = pitch
        var branch = branch
        var branchCount = branchCount
        val pos = chunk.pos
        val centerX = (pos.x * 16 + 8).toDouble()
        val centerZ = (pos.z * 16 + 8).toDouble()
        var yawVelocity = 0.0f
        var pitchVelocity = 0.0f
        var factor = 1.0f
        val verticalFactors = FloatArray(HEIGHT) { i ->
            if (i == 0 || random.nextInt(3) == 0) {
                factor = 1.0f + random.nextFloat() * random.nextFloat()
            }
            factor * factor
        }

        if (branchCount <= 0) {
            val maxBranchCount: Int = CARVER_RANGE * 16 - 16
            branchCount = maxBranchCount - random.nextInt(maxBranchCount / 4)
        }

        while (branch < branchCount) {
            val horizontalScale = 1.5 + Mth.sin(branch.toFloat() * Mth.PI / branchCount.toFloat()) * width
            val verticalScale = horizontalScale * 3.0
            val pitchCos = Mth.cos(pitch)

            x += (Mth.cos(yaw) * pitchCos).toDouble()
            y += Mth.sin(pitch).toDouble()
            z += (Mth.sin(yaw) * pitchCos).toDouble()
            pitch *= 0.7f
            pitch += pitchVelocity * 0.05f
            yaw += yawVelocity * 0.05f
            pitchVelocity *= 0.8f
            yawVelocity *= 0.5f
            pitchVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0f
            yawVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0f

            if (random.nextInt(4) == 0) {
                ++branch
                continue
            }

            val dx = x - centerX
            val dz = z - centerZ
            val remaining = (branchCount - branch).toDouble()
            val maxDistance = (width + 18.0f).toDouble()
            if (dx * dx + dz * dz - remaining * remaining > maxDistance * maxDistance) {
                return
            }

            carveRavineRegion(chunk, x, y, z, horizontalScale, verticalScale, verticalFactors)
            ++branch
        }
    }

    private fun carveRegion(
        chunk: ChunkAccess,
        x: Double,
        y: Double,
        z: Double,
        horizontalScale: Double,
        verticalScale: Double,
    ) {
        carveEllipsoid(chunk, x, y, z, horizontalScale, verticalScale, null)
    }

    private fun carveRavineRegion(
        chunk: ChunkAccess, x: Double, y: Double, z: Double, horizontalScale: Double,
        verticalScale: Double, verticalFactors: FloatArray?,
    ) {
        carveEllipsoid(chunk, x, y, z, horizontalScale, verticalScale, verticalFactors)
    }

    private fun carveEllipsoid(
        chunk: ChunkAccess, x: Double, y: Double, z: Double, horizontalScale: Double,
        verticalScale: Double, verticalFactors: FloatArray?,
    ) {
        val pos = chunk.pos
        val chunkStartX = pos.x * 16
        val chunkStartZ = pos.z * 16
        var minX = Mth.floor(x - horizontalScale) - chunkStartX - 1
        var maxX = Mth.floor(x + horizontalScale) - chunkStartX + 1
        var minY = Mth.floor(y - verticalScale) - 1
        var maxY = Mth.floor(y + verticalScale) + 1
        var minZ = Mth.floor(z - horizontalScale) - chunkStartZ - 1
        var maxZ = Mth.floor(z + horizontalScale) - chunkStartZ + 1

        minX = Mth.clamp(minX, 0, 16)
        maxX = Mth.clamp(maxX, 0, 16)
        minY = Mth.clamp(minY, MIN_Y + 1, HEIGHT - 8)
        maxY = Mth.clamp(maxY, MIN_Y + 1, HEIGHT - 8)
        minZ = Mth.clamp(minZ, 0, 16)
        maxZ = Mth.clamp(maxZ, 0, 16)

        if (isRegionBlockedByFluid(chunk, minX, maxX, minY, maxY, minZ, maxZ)) {
            return
        }

        val mutable = BlockPos.MutableBlockPos()
        for (localX in minX..<maxX) {
            val scaledX = (localX + chunkStartX + 0.5 - x) / horizontalScale
            for (localZ in minZ..<maxZ) {
                val scaledZ = (localZ + chunkStartZ + 0.5 - z) / horizontalScale
                if (scaledX * scaledX + scaledZ * scaledZ >= 1.0) {
                    continue
                }

                for (localY in maxY downTo minY + 1) {
                    val scaledY = (localY - 0.5 - y) / verticalScale
                    var shape = scaledX * scaledX + scaledZ * scaledZ
                    if (verticalFactors != null && localY >= 0 && localY < verticalFactors.size) {
                        shape *= verticalFactors[localY].toDouble()
                    }

                    val verticalShape = if (verticalFactors == null) scaledY * scaledY else scaledY * scaledY / 6.0
                    if (shape + verticalShape >= 1.0) {
                        continue
                    }

                    val worldX = chunkStartX + localX
                    val worldZ = chunkStartZ + localZ
                    val state = chunk.getBlockState(mutable.set(worldX, localY, worldZ))
                    if (isBaseStone(state)) {
                        chunk.setBlockState(mutable, if (localY <= LAVA_LEVEL) liquid_candy else air, false)
                    }
                }
            }
        }
    }

    private fun isRegionBlockedByFluid(
        chunk: ChunkAccess,
        minX: Int, maxX: Int,
        minY: Int, maxY: Int,
        minZ: Int, maxZ: Int,
    ): Boolean {
        val pos = chunk.pos
        val mutable = BlockPos.MutableBlockPos()
        for (localX in minX..<maxX) {
            val worldX = pos.getBlockX(localX)
            for (localZ in minZ..<maxZ) {
                val worldZ = pos.getBlockZ(localZ)
                var y = maxY + 1
                while (y >= minY - 1) {
                    if (y !in MIN_Y..<HEIGHT) {
                        --y
                        continue
                    }
                    val state = chunk.getBlockState(mutable.set(worldX, y, worldZ)).fluidState
                    if (y > LAVA_LEVEL && !state.isEmpty
                        && (state.`is`(FluidTags.WATER)
                                || state.`is`(CFluidTags.liquid_chocolate)
                                || state.`is`(CFluidTags.liquid_candy))
                    ) return true

                    if (y != minY - 1 && localX != minX && localX != maxX - 1 && localZ != minZ && localZ != maxZ - 1) {
                        y = minY
                    }
                    --y
                }
            }
        }
        return false
    }

    private fun findTopSolid(chunk: ChunkAccess, worldX: Int, worldZ: Int): Int {
        val mutable = BlockPos.MutableBlockPos()
        for (y in min(chunk.maxBuildHeight - 1, HEIGHT - 1) downTo MIN_Y) {
            val state = chunk.getBlockState(mutable.set(worldX, y, worldZ))
            if (isBaseStone(state)) {
                return y
            }
        }
        return MIN_Y
    }

    private fun findTopTerrain(chunk: ChunkAccess, worldX: Int, worldZ: Int): Int {
        val mutable = BlockPos.MutableBlockPos()
        for (y in min(chunk.maxBuildHeight - 1, HEIGHT - 1) downTo MIN_Y) {
            val state = chunk.getBlockState(mutable.set(worldX, y, worldZ))
            if (!state.isAir && state.fluidState.isEmpty) {
                return y
            }
        }
        return MIN_Y
    }

    private fun fluidForColumn(worldX: Int, worldZ: Int, randomState: RandomState): BlockState {
        if (biomeId(worldX, worldZ, randomState) == chocolate_forest) {
            val key: Long = packColumnPos(worldX, worldZ)
            var chocolate = pondChocolateCache[key]
            if (chocolate == null) {
                scanChocolatePond(worldX, worldZ, randomState)
                chocolate = pondChocolateCache[key] ?: false
            }
            if (chocolate) return liquid_chocolate
        }
        return water
    }

    /**
     * Chocolate liquid only appears in water bodies that are fully enclosed by chocolate-forest
     * surface (chocolate-covered white brownie top blocks). Any connection to foreign-biome water
     * (ocean/river/other) or a non-forest shore keeps the whole body as plain water. The result is
     * shared with every visited water column so each connected body is scanned only once.
     */
    private fun scanChocolatePond(startX: Int, startZ: Int, randomState: RandomState) {
        val queue = ArrayDeque<Long>()
        val visited = HashSet<Long>()
        queue.add(packColumnPos(startX, startZ))
        var enclosed = true

        while (!queue.isEmpty()) {
            if (visited.size >= MAX_POND_SCAN_COLUMNS) {
                enclosed = false
                break
            }
            val packed = queue.poll()!!
            if (!visited.add(packed)) {
                continue
            }
            val x = (packed shr 32).toInt()
            val z = packed.toInt()
            if (chocolate_forest != biomeId(x, z, randomState)) {
                enclosed = false
                break
            }
            if (isOpenWaterColumn(x, z, randomState)) {
                queue.add(packColumnPos(x + 1, z))
                queue.add(packColumnPos(x - 1, z))
                queue.add(packColumnPos(x, z + 1))
                queue.add(packColumnPos(x, z - 1))
            }
        }

        if (pondChocolateCache.size + visited.size > 131072) {
            pondChocolateCache.clear()
        }
        for (packed in visited) {
            pondChocolateCache[packed] = enclosed
        }
    }

    /** Open water means no solid terrain at the two topmost sea-level layers of this column.  */
    private fun isOpenWaterColumn(x: Int, z: Int, randomState: RandomState): Boolean {
        val key: Long = packColumnPos(x, z)
        val cached = openWaterColumnCache[key]
        if (cached != null) {
            return cached
        }
        val noise: TerrainNoiseSet = terrainNoiseSet(randomState)
        val noiseX = x / CELL_WIDTH.toDouble()
        val noiseZ = z / CELL_WIDTH.toDouble()
        val heightConfig = heightConfigAt(Mth.floor(noiseX).toDouble(), Mth.floor(noiseZ).toDouble(), randomState)
        val depthNoise: Double = sampleDepthNoise(noise, noiseX, noiseZ)
        val water = sampleDensity(
            noise,
            noiseX,
            (SEA_LEVEL - 1) / CELL_HEIGHT.toDouble(),
            noiseZ,
            heightConfig,
            depthNoise
        ) <= 0.0
                && sampleDensity(
            noise,
            noiseX,
            SEA_LEVEL / CELL_HEIGHT.toDouble(),
            noiseZ,
            heightConfig,
            depthNoise
        ) <= 0.0
        if (openWaterColumnCache.size >= 131072) {
            openWaterColumnCache.clear()
        }
        openWaterColumnCache.putIfAbsent(key, water)
        return water
    }

    private val biomeShapes: Map<ResourceKey<Biome>, BiomeShape> = mapOf(
        sugar_plains to BiomeShape(0.05f, 0.1f),
        sugar_forest to BiomeShape(0.1f, 0.15f),
        chocolate_forest to BiomeShape(0.1f, 0.15f),
        sugar_cold_forest to BiomeShape(0.1f, 0.3f),
        sugar_river to BiomeShape(-0.5f, 0.0f),
        sugar_oceans to BiomeShape(-1.0f, 0.1f),
        sugar_enchanted_forest to BiomeShape(0.23f, 0.25f),
        caramel_forest to BiomeShape(0.05f, 0.1f),
        cotton_candy_plains to BiomeShape(0.05f, 0.1f),
        gummy_swamp to BiomeShape(-0.1f, 0.1f),
        ice_cream_plains to BiomeShape(0.05f, 0.1f),
        hard_candy_plains to BiomeShape(0.05f, 0.1f),
        sugar_mountains to BiomeShape(0.5f, 0.8f),
        sugar_hell_mountains to BiomeShape(1.9f, 2.0f),
        ice_cream_sky_mountains to BiomeShape(3.55f, 2.9f)
    )

    private fun biomeShape(x: Int, z: Int, randomState: RandomState): BiomeShape {
        val key = x.toLong() shl 32 xor (z.toLong() and 0xFFFFFFFFL)
        val cached = biomeShapeCache[key]
        if (cached != null) {
            return cached
        }
        val location = biomeId(x, z, randomState)
        val result = biomeShapes[location] ?: biomeShapes[sugar_plains]!!
        if (biomeShapeCache.size >= 131072) {
            biomeShapeCache.clear()
        }
        val previous = biomeShapeCache.putIfAbsent(key, result)
        return previous ?: result
    }

    private fun biomeId(x: Int, z: Int, random: RandomState): ResourceKey<Biome> {
        val biome = getBiomeSource().getNoiseBiome(
            Mth.floorDiv(x, 4),
            16,
            Mth.floorDiv(z, 4),
            random.sampler()
        )
        return biome.unwrapKey().getOrDefault(sugar_plains)
    }

//    private fun isSpawnLandRadius(quartX: Int, quartZ: Int, random: RandomSource): Boolean {
//        val blockX = quartX.toLong() shl 2
//        val blockZ = quartZ.toLong() shl 2
//        return isWithinSpawnIsland(blockX.toDouble(), blockZ.toDouble(), random)
//    }
//
//    fun isWithinSpawnIsland(blockX: Double, blockZ: Double, random: RandomSource): Boolean {
//        val distance = sqrt(blockX * blockX + blockZ * blockZ)
//        if (distance <= SPAWN_LAND_RADIUS_BLOCKS) {
//            return true
//        }
//        if (distance > SPAWN_LAND_MAX_RADIUS_BLOCKS) {
//            return false
//        }
//        return distance <= spawnIslandRadius(blockX, blockZ, random)
//    }
//
//    fun spawnIslandInfluence(blockX: Double, blockZ: Double, random: RandomSource): Double {
//        val distance = sqrt(blockX * blockX + blockZ * blockZ)
//        if (distance <= SPAWN_LAND_RADIUS_BLOCKS) {
//            return 1.0
//        }
//
//        val radius = spawnIslandRadius(blockX, blockZ, random)
//        if (distance >= radius) {
//            return 0.0
//        }
//
//        var blend = (radius - distance) / max(radius - SPAWN_LAND_RADIUS_BLOCKS, 1.0)
//        blend = max(0.0, min(1.0, blend))
//        return blend * blend * (3.0 - 2.0 * blend)
//    }
//
//    private fun spawnIslandRadius(blockX: Double, blockZ: Double, random: RandomSource): Double {
//        val broad = octaveNoise2D(blockX * 0.016, blockZ * 0.016, 4, random.nextLong())
//        val detail = octaveNoise2D(blockX * 0.045, blockZ * 0.045, 2, random.nextLong())
//        val radius = 124.0 + broad * 22.0 + detail * 7.0
//        return max(SPAWN_LAND_RADIUS_BLOCKS + 2.0, min(SPAWN_LAND_MAX_RADIUS_BLOCKS.toDouble(), radius))
//    }

}


private data class BiomeShape(val baseHeight: Float, val variation: Float)

private data class HeightConfig(val depth: Double, val scale: Double)

private data class SurfaceMaterials(val top: BlockState?, val under: BlockState, val underwater: BlockState? = under)

private class TerrainNoiseSet(random: RandomSource) {
    constructor(seed: Long) : this(RandomSource.create(seed))

    val minLimit = LegacyPerlinOctaveNoise(random, 16, true)
    val maxLimit = LegacyPerlinOctaveNoise(random, 16, true)
    val main = LegacyPerlinOctaveNoise(random, 8, true)
    val depth = LegacyPerlinOctaveNoise(random, 16, true)
}

private fun packColumnPos(x: Int, z: Int): Long {
    return x.toLong() shl 32 xor (z.toLong() and 0xFFFFFFFFL)
}


private fun octaveNoise2D(x: Double, z: Double, octaves: Int, salt: Long): Double {
    var value = 0.0
    var amplitude = 1.0
    var frequency = 1.0
    var total = 0.0

    for (i in 0..<octaves) {
        value += smoothNoise2D(x * frequency, z * frequency, salt + i * 0x632BE59BD9B4E019L) * amplitude
        total += amplitude
        amplitude *= 0.5
        frequency *= 2.0
    }

    return value / total
}


private fun smoothNoise2D(x: Double, z: Double, salt: Long): Double {
    val x0 = Mth.floor(x)
    val z0 = Mth.floor(z)
    val tx: Double = fade(x - x0)
    val tz: Double = fade(z - z0)
    val a: Double = randomUnit(x0, 0, z0, salt)
    val b: Double = randomUnit(x0 + 1, 0, z0, salt)
    val c: Double = randomUnit(x0, 0, z0 + 1, salt)
    val d: Double = randomUnit(x0 + 1, 0, z0 + 1, salt)
    return Mth.lerp(tz, Mth.lerp(tx, a, b), Mth.lerp(tx, c, d))
}

//private fun smoothNoise3D(x: Double, y: Double, z: Double, salt: Long): Double {
//    val x0 = Mth.floor(x)
//    val y0 = Mth.floor(y)
//    val z0 = Mth.floor(z)
//    val tx: Double = fade(x - x0)
//    val ty: Double = fade(y - y0)
//    val tz: Double = fade(z - z0)
//    val a = Mth.lerp(tx, randomUnit(x0, y0, z0, salt), randomUnit(x0 + 1, y0, z0, salt))
//    val b = Mth.lerp(tx, randomUnit(x0, y0, z0 + 1, salt), randomUnit(x0 + 1, y0, z0 + 1, salt))
//    val c = Mth.lerp(tx, randomUnit(x0, y0 + 1, z0, salt), randomUnit(x0 + 1, y0 + 1, z0, salt))
//    val d = Mth.lerp(tx, randomUnit(x0, y0 + 1, z0 + 1, salt), randomUnit(x0 + 1, y0 + 1, z0 + 1, salt))
//    return Mth.lerp(tz, Mth.lerp(ty, a, c), Mth.lerp(ty, b, d))
//}

private fun fade(value: Double): Double {
    return value * value * value * (value * (value * 6.0 - 15.0) + 10.0)
}

private fun randomUnit(x: Int, y: Int, z: Int, salt: Long): Double {
    val bits: Long = hash(x, y, z, salt)
    return (bits ushr 11) * 1.1102230246251565E-16 * 2.0 - 1.0
}

private fun hash(x: Int, y: Int, z: Int): Int {
    return hash(x, y, z, -0x340d631b7bdddcdbL).toInt()
}

private fun positiveHash(x: Int, y: Int, z: Int, salt: Long): Long {
    return hash(x, y, z, salt) and Long.MAX_VALUE
}

private fun hash(x: Int, y: Int, z: Int, salt: Long): Long {
    var h = salt
    h = h xor x * -0x61c8864680b583ebL
    h = h.rotateLeft(27) * -0x6b2fb644ecceee15L
    h = h xor y * -0x3d4d51c2d82b14b1L
    h = h.rotateLeft(31) * 0x2545F4914F6CDD1DL
    h = h xor z * 0x165667B19E3779F9L
    h = h xor (h ushr 33)
    h *= -0xae502812aa7333L
    h = h xor (h ushr 33)
    h *= -0x3b314601e57a13adL
    h = h xor (h ushr 33)
    return h
}

fun PositionalRandomFactory.at(chunk: ChunkPos): RandomSource = at(chunk.x shl 4, 0, chunk.z shl 4)