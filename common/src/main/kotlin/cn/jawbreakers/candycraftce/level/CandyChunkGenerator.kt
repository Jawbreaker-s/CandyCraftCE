@file:Suppress("DuplicatedCode", "PrivatePropertyName")

package cn.jawbreakers.candycraftce.level

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_NAME
import cn.jawbreakers.candycraftce.level.noise.LegacyPerlinOctaveNoise
import cn.jawbreakers.candycraftce.mixin.level.NoiseRouterDataAccessor
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
import cn.jawbreakers.candycraftce.registry.CFluidTags
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
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.data.worldgen.SurfaceRuleData
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.WorldGenRegion
import net.minecraft.tags.FluidTags
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.level.NaturalSpawner
import net.minecraft.world.level.NoiseColumn
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.OverworldBiomeBuilder
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.*
import net.minecraft.world.level.levelgen.blending.Blender
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import kotlin.jvm.optionals.getOrDefault
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class CandyChunkGenerator(
    val source: CandyBiomeSource,
    val settings: Holder<NoiseGeneratorSettings>,
) : ChunkGenerator(source) {
    companion object {
        val candyland_noise_settings: ResourceKey<NoiseGeneratorSettings> =
            ResourceKey.create(Registries.NOISE_SETTINGS, "candyland_noise_settings".modLoc())

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
        private val base_stone: BlockState = CBlocks.white_chocolate_stone.defaultBlockState()
        private val deepslate_stone: BlockState = CBlocks.chocolate_stone.defaultBlockState()
        private val custard_pudding = CBlocks.custard_pudding_block.defaultBlockState()
        private val pudding = CBlocks.pudding_block.defaultBlockState()
        private val ice_cream = CBlocks.ice_cream.defaultBlockState()
        private val sand = CBlocks.sugar_sand.defaultBlockState()

        //岩浆
        private val hot_liquid: BlockState = CBlocks.caramel.defaultBlockState()
        fun bootstrap(context: BootstapContext<NoiseGeneratorSettings>) {
            context.register(
                candyland_noise_settings, NoiseGeneratorSettings(
                    NoiseSettings(MIN_Y, HEIGHT, 1, 2),
                    base_stone,
                    water,
                    NoiseRouterDataAccessor.getOverworld(
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

        // 接近世界顶端时逐步压低开洞密度，避免地形顶到建筑上限
        if (noiseY > TOP_FADE_Y) {
            val topFade = (noiseY - TOP_FADE_Y) / 3.0
            density = Mth.lerp(topFade, density, -10.0)
        }

        return density
    }


    private fun isStone(state: BlockState): Boolean {
        return state.`is`(base_stone.block) || state.`is`(deepslate_stone.block)
    }

    /**
     * 由插值密度与绝对世界 Y 决定方块类型（fillTerrain / buildColumn 共用，消除重复）：
     * 密度 > 0 为实心石头（低于分界线用 deepslate_stone，否则 base_stone）；
     * 否则海平面以下为水，以上为空气。
     */
    private fun stateFor(density: Double, worldY: Int): BlockState = when {
        density > 0.0 -> if (worldY < DEEPSLATE_BOUNDARY) deepslate_stone else base_stone
        worldY < SEA_LEVEL -> water
        else -> air
    }

    val defaultSurfaceMaterials = SurfaceMaterials(custard_pudding, pudding)
    private val surfaceMaterials = mapOf(
        ice_cream_sky_mountains to SurfaceMaterials(ice_cream, pudding)
    )

    fun surfaceMaterials(
        biomeId: ResourceKey<Biome>,
        worldX: Int,
        worldZ: Int,
        randomState: RandomState,
    ): SurfaceMaterials {
        return when (biomeId) {
            in surfaceMaterials -> surfaceMaterials[biomeId]!!
            else -> defaultSurfaceMaterials
        }
    }

    private fun underwaterMaterial(replaced: Int): BlockState {
        return if (replaced == 0) sand else pudding
    }


    private val biomeShapeCache: Cache<Long, BiomeShape> = CacheBuilder.newBuilder()
        .maximumSize(65536)
        .build()

    override fun getBiomeSource() = super.biomeSource as CandyBiomeSource

    override fun fillFromNoise(
        executor: Executor, blender: Blender, randomState: RandomState,
        structureManager: StructureManager, chunk: ChunkAccess,
    ): CompletableFuture<ChunkAccess> {
        return CompletableFuture.supplyAsync({
            fillTerrain(chunk, randomState)
            Heightmap.primeHeightmaps(
                chunk, setOf(
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
        // 生成该区块的密度图（4×4 单元格对应 5×5 噪声采样点，Y 方向 49 个采样点）
        val heightMap = generateHeightMap(terrainNoiseSet(randomState), pos.x * 4, pos.z * 4, randomState)
        val mutable = BlockPos.MutableBlockPos()
        val worldXs = IntArray(16) { pos.getBlockX(it) }
        val worldZs = IntArray(16) { pos.getBlockZ(it) }
        // 遍历单元格（4×4 个，Y 方向 48 个单元格 = 384 格）
        for (cellX in 0..3) {
            val x0: Int = cellX * NOISE_SIZE_XZ
            val x1: Int = (cellX + 1) * NOISE_SIZE_XZ

            for (cellZ in 0..3) {
                // 对应四个角点的密度数组索引
                val z0: Int = (x0 + cellZ) * NOISE_SIZE_Y
                val z1: Int = (x0 + cellZ + 1) * NOISE_SIZE_Y
                val z2: Int = (x1 + cellZ) * NOISE_SIZE_Y
                val z3: Int = (x1 + cellZ + 1) * NOISE_SIZE_Y

                for (cellY in 0 until CELLS_Y) {
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
                                // 网格单元格换算为绝对世界 Y（世界底为 MIN_Y，这是旧版 0 起点遗留的修正点）
                                val y: Int = MIN_Y + cellY * CELL_HEIGHT + subY

                                val state: BlockState = stateFor(density, y)
                                // 热路径：跳过空气写入；setBlockState 不持有 pos，无需 .immutable() 分配
                                if (!state.isAir && y >= chunk.minBuildHeight && y < chunk.maxBuildHeight) {
                                    chunk.setBlockState(mutable.set(worldX, y, worldZ), state, false)
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
        // 洞穴雕刻后处理深板岩交界与底部基岩
        carveCaves(chunk, randomState)
        applyDeepslate(chunk, randomState)
        applyBedrock(chunk, randomState)
    }

    /**
     * 生成 5×5×49 的密度图。
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
                        // 网格索引换算为绝对世界高度对应的噪声 Y，保证地形落在正确的海拔区间
                        (y + NOISE_Y_OFFSET).toDouble(),
                        (baseNoiseZ + z).toDouble(),
                        heightConfig,
                        depthNoise
                    )
                }
            }
        }

        return heightMap
    }

    override fun buildSurface(
        region: WorldGenRegion, structureManager: StructureManager, randomState: RandomState,
        chunk: ChunkAccess,
    ) {
        val mutable = BlockPos.MutableBlockPos()
        val pos = chunk.pos

        // 预计算当前区块 + 周围 2 格的生物群系网格
        val blendRadius = 2
        val biomeGrid = Array(16 + 2 * blendRadius) { gx ->
            Array(16 + 2 * blendRadius) { gz ->
                biomeId(pos.getBlockX(gx - blendRadius), pos.getBlockZ(gz - blendRadius), randomState)
            }
        }

        for (localX in 0..15) {
            val worldX = pos.getBlockX(localX)
            for (localZ in 0..15) {
                val worldZ = pos.getBlockZ(localZ)
                // 直接从预计算网格取当前列生物群系
                val biomeId = biomeGrid[localX + blendRadius][localZ + blendRadius]
                val materials: SurfaceMaterials = surfaceMaterials(biomeId, worldX, worldZ, randomState)
                val top = findTopSolid(chunk, worldX, worldZ)
                val underwater = top < SEA_LEVEL - 1 || biomeId == sugar_oceans || biomeId == sugar_river
                val depth: Int = 3 + abs(hash(worldX, 0, worldZ)) % 3
                var replaced = 0
                var y = top
                while (y > MIN_Y && replaced <= depth) {
                    val state = chunk.getBlockState(mutable.set(worldX, y, worldZ))
                    if (!isStone(state)) {
                        if (replaced > 0) break
                        --y
                        continue
                    }
                    val replacement = when {
                        underwater -> underwaterMaterial(replaced)
                        replaced > 0 -> materials.under
                        else -> materials.top
                    }

                    chunk.setBlockState(mutable, replacement, false)
                    replaced++
                    --y
                }
            }
        }
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
        info.add("$MOD_NAME Chunk Generator v1.0: WS=${worldSeed(randomState).nextLong().toHexString()}")
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
        val states: Array<BlockState> = Array(height) { i ->
            val y = minY + i
            // 噪声 Y 基于绝对世界 Y（与 generateHeightMap 的 gy + NOISE_Y_OFFSET 等价），
            // 这样地表才落在海平面之上；若改用相对索引会使地形整体下移 MIN_Y 而出现全图是水
            val noiseY = y / CELL_HEIGHT.toDouble()
            val density = sampleDensity(noise, noiseX, noiseY, noiseZ, heightConfig)
            // 最底一格为基岩，其余交由统一的 stateFor 判定（含 deepslate 分界）
            if (i == 0) flat_bottom else stateFor(density, y)
        }

        return states
    }

    private fun heightConfigAt(noiseX: Double, noiseZ: Double, randomState: RandomState): HeightConfig {
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

        return HeightConfig(depth, scale)
    }

    /**
     * 基岩式参差层：从 [startY] 起本列随机生成厚度 1~3 的一层。
     * 首格（偏移 0）必出，其后每格 50% 概率继续，随机消耗顺序与原版保持一致。
     * 具体放置交由 [place] 回调按绝对世界 Y 执行。
     */
    private inline fun jaggedLayer(random: RandomSource, startY: Int, place: (worldY: Int) -> Unit) {
        val total = random.nextInt(3) + 1 // [1,3]
        repeat(total) { i ->
            if (i == 0 || random.nextBoolean()) place(startY + i)
        }
    }

    /** 世界底部 1~3 格基岩（参差随机）。 */
    private fun applyBedrock(chunk: ChunkAccess, randomState: RandomState) {
        val random = randomState.getOrCreateRandomFactory(caveRandom).at(chunk.pos.x, 0, chunk.pos.z)
        val pos = chunk.pos
        val mutable = BlockPos.MutableBlockPos()

        for (localX in 0..15) {
            val worldX = pos.getBlockX(localX)
            for (localZ in 0..15) {
                val worldZ = pos.getBlockZ(localZ)
                jaggedLayer(random, MIN_Y) { y ->
                    chunk.setBlockState(mutable.set(worldX, y, worldZ), flat_bottom, false)
                }
            }
        }
    }

    /**
     * 石头/深板岩交界处理：
     * y < DEEPSLATE_BOUNDARY 的实心石头已在 fillTerrain 中直接写成 deepslate_stone；
     * 此处用与基岩相同的随机把 DEEPSLATE_BOUNDARY 上方 1~3 格内仍为石头的位置并入深板岩，
     * 使分界呈参差状而非平直一条线。仅替换实心石头，避免在洞穴/空气中凭空生成深板岩。
     */
    private fun applyDeepslate(chunk: ChunkAccess, randomState: RandomState) {
        val random = randomState.getOrCreateRandomFactory(terrainRandom).at(chunk.pos.x, 0, chunk.pos.z)
        val pos = chunk.pos
        val mutable = BlockPos.MutableBlockPos()

        for (localX in 0..15) {
            val worldX = pos.getBlockX(localX)
            for (localZ in 0..15) {
                val worldZ = pos.getBlockZ(localZ)
                jaggedLayer(random, DEEPSLATE_BOUNDARY) { y ->
                    if (chunk.getBlockState(mutable.set(worldX, y, worldZ)).`is`(base_stone.block)) {
                        chunk.setBlockState(mutable, deepslate_stone, false)
                    }
                }
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
            // 洞穴起点纵向覆盖整段地下（MIN_Y 之上到海平面附近），使深层深板岩区也能生成洞穴
            val y = (MIN_Y + 8 + random.nextInt(random.nextInt(150) + 8)).toDouble()
            val z = (sourceChunkZ * 16 + random.nextInt(16)).toDouble()
            var tunnelCount = 1

            if (random.nextInt(4) == 0) {
                carveTunnel(chunk, random.fork(), x, y, z, 1.0f + random.nextFloat() * 6.0f, 0.0f, 0.0f, -1, -1, 0.5)
                tunnelCount += random.nextInt(4)
            }

            repeat(tunnelCount) {
                val yaw = random.nextFloat() * Mth.PI * 2.0f
                val pitch = (random.nextFloat() - 0.5f) * 0.25f
                var width = random.nextFloat() * 2.0f + random.nextFloat()
                if (random.nextInt(10) == 0) {
                    width *= random.nextFloat() * random.nextFloat() * 3.0f + 1.0f
                }

                carveTunnel(chunk, random.fork(), x, y, z, width, yaw, pitch, 0, 0, 1.0)
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
                    chunk, random.fork(), x, y, z, random.nextFloat() * 0.5f + 0.5f,
                    yaw - Mth.HALF_PI, pitch / 3.0f, branch, branchCount, 1.0
                )
                carveTunnel(
                    chunk, random.fork(), x, y, z, random.nextFloat() * 0.5f + 0.5f,
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

            carveEllipsoid(chunk, x, y, z, horizontalScale, verticalScale, null)
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
        carveRavine(chunk, random.fork(), x, y, z, width, yaw, pitch, 0, 0)
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

            carveEllipsoid(chunk, x, y, z, horizontalScale, verticalScale, verticalFactors)
            ++branch
        }
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
        minY = Mth.clamp(minY, MIN_Y + 1, MAX_Y - 8)
        maxY = Mth.clamp(maxY, MIN_Y + 1, MAX_Y - 8)
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
                    // verticalFactors 以相对世界底部的网格索引存放，故用 localY - MIN_Y 取值
                    if (verticalFactors != null) {
                        val fy = localY - MIN_Y
                        if (fy in verticalFactors.indices) shape *= verticalFactors[fy].toDouble()
                    }

                    val verticalShape = if (verticalFactors == null) scaledY * scaledY else scaledY * scaledY / 6.0
                    if (shape + verticalShape >= 1.0) {
                        continue
                    }

                    val worldX = chunkStartX + localX
                    val worldZ = chunkStartZ + localZ
                    val state = chunk.getBlockState(mutable.set(worldX, localY, worldZ))
                    if (isStone(state)) {
                        chunk.setBlockState(mutable, if (localY <= LAVA_LEVEL) hot_liquid else air, false)
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
                    // 有效世界高度区间为 [MIN_Y, MAX_Y)，HEIGHT 是总高度不能当作世界 Y 使用
                    if (y !in MIN_Y..<MAX_Y) {
                        --y
                        continue
                    }
                    val state = chunk.getBlockState(mutable.set(worldX, y, worldZ)).fluidState
                    if (y > LAVA_LEVEL
                        && !state.isEmpty
                        && (state.`is`(FluidTags.WATER) || state.`is`(CFluidTags.caramel))
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
        // 从区块顶端向下扫描（chunk 边界已隐含 MAX_Y）
        for (y in (chunk.maxBuildHeight - 1) downTo MIN_Y) {
            val state = chunk.getBlockState(mutable.set(worldX, y, worldZ))
            if (isStone(state)) {
                return y
            }
        }
        return MIN_Y
    }

    private val biomeShapes: Map<ResourceKey<Biome>, BiomeShape> = mapOf(
        pudding_plains to BiomeShape(0.05f, 0.1f),
        sugar_forest to BiomeShape(0.1f, 0.15f),
        chocolate_forest to BiomeShape(0.1f, 0.15f),
        white_chocolate_forest to BiomeShape(0.1f, 0.3f),
        sugar_river to BiomeShape(-0.5f, 0.0f),
        sugar_oceans to BiomeShape(-1.0f, 0.1f),
        enchanted_forest to BiomeShape(0.23f, 0.25f),
        caramel_forest to BiomeShape(0.05f, 0.1f),
        ice_cream_plains to BiomeShape(0.05f, 0.1f),
        pudding_hill to BiomeShape(1f, 1f),
        ice_cream_sky_mountains to BiomeShape(3.55f, 2.9f)
    )

    private fun biomeShape(x: Int, z: Int, randomState: RandomState): BiomeShape {
        val key = x.toLong() shl 32 xor (z.toLong() and 0xFFFFFFFFL)
        return biomeShapeCache.get(key) {
            val location = biomeId(x, z, randomState)
            biomeShapes[location] ?: biomeShapes[pudding_plains]!!
        }

    }

    private fun biomeId(x: Int, z: Int, random: RandomState): ResourceKey<Biome> {
        val biome = getBiomeSource().getNoiseBiome(
            Mth.floorDiv(x, 4),
            16,
            Mth.floorDiv(z, 4),
            random.sampler()
        )
        return biome.unwrapKey().getOrDefault(pudding_plains)
    }
}


data class BiomeShape(val baseHeight: Float, val variation: Float)

data class HeightConfig(val depth: Double, val scale: Double)

data class SurfaceMaterials(val top: BlockState, val under: BlockState, val underwater: BlockState? = under)

class TerrainNoiseSet(random: RandomSource) {
    constructor(seed: Long) : this(RandomSource.create(seed))

    val minLimit = LegacyPerlinOctaveNoise(random, 16, true)
    val maxLimit = LegacyPerlinOctaveNoise(random, 16, true)
    val main = LegacyPerlinOctaveNoise(random, 8, true)
    val depth = LegacyPerlinOctaveNoise(random, 16, true)
}
