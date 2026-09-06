//package cn.jawbreakers.candycraftce.level
//
//import cn.jawbreakers.candycraftce.CandyCraftCE
//import cn.jawbreakers.candycraftce.block.CandyLiquidBlock
//import cn.jawbreakers.candycraftce.registry.CBlocks
//import cn.jawbreakers.candycraftce.registry.CBlocks.defaultBlockState
//import com.mojang.serialization.Codec
//import com.mojang.serialization.codecs.RecordCodecBuilder
//import net.minecraft.core.BlockPos
//import net.minecraft.core.Direction
//import net.minecraft.core.RegistryAccess
//import net.minecraft.resources.ResourceKey
//import net.minecraft.resources.ResourceLocation
//import net.minecraft.server.level.WorldGenRegion
//import net.minecraft.util.Mth
//import net.minecraft.util.RandomSource
//import net.minecraft.world.level.LevelHeightAccessor
//import net.minecraft.world.level.NaturalSpawner
//import net.minecraft.world.level.NoiseColumn
//import net.minecraft.world.level.StructureManager
//import net.minecraft.world.level.biome.Biome
//import net.minecraft.world.level.biome.BiomeManager
//import net.minecraft.world.level.block.Blocks
//import net.minecraft.world.level.block.state.BlockState
//import net.minecraft.world.level.chunk.ChunkAccess
//import net.minecraft.world.level.chunk.ChunkGenerator
//import net.minecraft.world.level.chunk.ChunkGeneratorStructureState
//import net.minecraft.world.level.levelgen.GenerationStep
//import net.minecraft.world.level.levelgen.Heightmap
//import net.minecraft.world.level.levelgen.RandomState
//import net.minecraft.world.level.levelgen.blending.Blender
//import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
//import java.util.*
//import java.util.concurrent.CompletableFuture
//import java.util.concurrent.ConcurrentHashMap
//import java.util.concurrent.Executor
//import java.util.function.Function
//import kotlin.collections.plus
//import kotlin.concurrent.Volatile
//import kotlin.math.abs
//import kotlin.math.max
//import kotlin.math.min
//import kotlin.plus
//import kotlin.sequences.plus
//
///**
// * CandyWorld 的自定义区块生成器 (ChunkGenerator)。
// *
// *
// * 这个生成器重现了 Minecraft 1.12 时代的“旧版”地形生成风格，主要特点包括：
// *
// *  * 使用密度场 (density field) 与双线性/三线性插值，而不是新版的多噪声系统。
// *  * 拥有独立的糖果世界生物群系，如棉花糖平原、巧克力森林、软糖沼泽等。
// *  * 旧式洞穴与峡谷雕刻算法，以及旧式地表生成逻辑。
// *  * 带有巧克力池塘、糖果液体、山泉、粉色结晶糖等特殊装饰。
// *
// */
//class CandyWorldChunkGenerator(val source: CandyBiomeSource) : ChunkGenerator(source) {
//    companion object {
//        val codec: Codec<CandyWorldChunkGenerator> = RecordCodecBuilder.create { instance ->
//            instance.group(
//                CandyBiomeSource.codec.fieldOf("source").forGetter { it.source }
//            ).apply(instance, ::CandyWorldChunkGenerator)
//        }
//
//        // ========================== 世界生成常量 ==========================
//        /** 世界最小 Y 坐标（糖果世界从 Y=0 开始）。  */
//        private const val MIN_Y = 0
//
//        /** 世界总高度（256 格）。  */
//        private const val HEIGHT = 256
//
//        /** 海平面高度（Y=63）。  */
//        private const val SEA_LEVEL = 63
//
//        /** 岩浆/液态糖果层高度（Y=10）。  */
//        private const val LAVA_LEVEL = 10
//
//        /** 洞穴雕刻时检查的周围区块范围（±8 区块）。  */
//        private const val CARVER_RANGE = 8
//
//        /** 噪声采样网格尺寸（XZ 方向 5 个采样点）。  */
//        private const val NOISE_SIZE_XZ = 5
//
//        /** 噪声采样网格尺寸（Y 方向 33 个采样点）。  */
//        private const val NOISE_SIZE_Y = 33
//
//        /** 巧克力池塘扫描时最多访问的列数上限，防止性能退化。  */
//        private const val MAX_POND_SCAN_COLUMNS = 32768
//
//        /** 插值单元格宽度（XZ 方向，4 块）。  */
//        private const val CELL_WIDTH = 4
//
//        /** 插值单元格高度（Y 方向，8 块）。  */
//        private const val CELL_HEIGHT = 8
//
//        // 噪声缩放相关常量（与旧版 1.12 地形生成参数保持一致）
//        private const val COORDINATE_SCALE = 684.412
//        private const val HEIGHT_SCALE = 684.412
//        private const val MAIN_NOISE_SCALE_XZ = 80.0
//        private const val MAIN_NOISE_SCALE_Y = 160.0
//        private const val LOWER_LIMIT_SCALE = 512.0
//        private const val UPPER_LIMIT_SCALE = 512.0
//        private const val DEPTH_NOISE_SCALE_XZ = 200.0
//
//        /** 生物群系深度权重。  */
//        private const val BIOME_DEPTH_WEIGHT = 1.0
//
//        /** 生物群系缩放权重。  */
//        private const val BIOME_SCALE_WEIGHT = 1.0
//        private const val BIOME_DEPTH_OFFSET = 0.0
//        private const val BIOME_SCALE_OFFSET = 0.0
//
//        /** 基础地形尺寸。  */
//        private const val BASE_SIZE = 8.5
//
//        /** Y 轴拉伸系数。  */
//        private const val STRETCH_Y = 12.0
//
//        // 生物群系 ID 常量
//        private val PLAINS = ResourceLocation("minecraft", "plains")
//        private val SUGAR_OCEANS = ResourceLocation(CandyCraftCE.MOD_ID, "sugar_oceans")
//        private val SUGAR_RIVER = ResourceLocation(CandyCraftCE.MOD_ID, "sugar_river")
//        private val COTTON_CANDY_PLAINS = ResourceLocation(CandyCraftCE.MOD_ID, "cotton_candy_plains")
//        private val CHOCOLATE_FOREST = ResourceLocation(CandyCraftCE.MOD_ID, "chocolate_forest")
//        private val GUMMY_SWAMP = ResourceLocation(CandyCraftCE.MOD_ID, "gummy_swamp")
//        private val SUGAR_MOUNTAINS = ResourceLocation(CandyCraftCE.MOD_ID, "sugar_mountains")
//        private val SUGAR_HELL_MOUNTAINS = ResourceLocation(CandyCraftCE.MOD_ID, "sugar_hell_mountains")
//        private val ICE_CREAM_SKY_MOUNTAINS = ResourceLocation(CandyCraftCE.MOD_ID, "ice_cream_sky_mountains")
//
//        // 方块状态常量
//        private val AIR: BlockState = Blocks.AIR.defaultBlockState()
//        private val WATER: BlockState = Blocks.WATER.defaultBlockState()
//
//        /** 世界底部的平坦底部方块（冰糖）。  */
//        private val FLAT_BOTTOM: BlockState = CBlocks.jawbreaker_block.defaultBlockState()
//
//        /** 糖果世界的主岩石（结晶糖）。  */
//        private val BASE_STONE: BlockState = CBlocks.crystallized_sugar.get().defaultBlockState()
//
//        /** 液态糖果（类似岩浆的液体）。  */
//        private val LIQUID_CANDY: BlockState? = CBlocks.LIQUID_CANDY.get().defaultBlockState()
//
//        /** 液态巧克力。  */
//        private val LIQUID_CHOCOLATE: BlockState? = CBlocks.LIQUID_CHOCOLATE.get().defaultBlockState()
//
//        /** 抛物线权重场，用于生物群系地形过渡时的平滑插值。  */
//        private val PARABOLIC_FIELD: FloatArray = makeParabolicField()
//
//        // ========================== 缓存字段 ==========================
//        /** 地形噪声缓存，按种子缓存（线程安全）。  */
//        private val TERRAIN_NOISE: MutableMap<Long?, LegacyTerrainNoise> =
//            ConcurrentHashMap<Long?, LegacyTerrainNoise>()
//
//        /**
//         * 从 RandomState 中提取世界种子。
//         * 使用固定命名空间 "legacy_major_release" 生成，保证跨版本一致。
//         */
//        private fun worldSeed(randomState: RandomState): Long {
//            return randomState.getOrCreateRandomFactory(ResourceLocation(CandyCraftCE.MOD_ID, "legacy_major_release"))
//                .fromHashOf("world")
//                .nextLong()
//        }
//
//        /** 返回区块的最小可生成 Y 坐标。  */
//        private fun levelMin(chunk: ChunkAccess): Int {
//            return chunk.minBuildHeight
//        }
//
//        private fun scheduleFluidTick(region: WorldGenRegion, pos: BlockPos, state: BlockState) {
//            val fluidState = state.fluidState
//            if (!fluidState.isEmpty) {
//                region.scheduleTick(pos.immutable(), fluidState.type, fluidState.type.getTickDelay(region))
//            }
//        }
//
//        private fun terrainNoise(randomState: RandomState): LegacyTerrainNoise {
//            val seed = randomState.getOrCreateRandomFactory(ResourceLocation(CandyCraft.MODID, "legacy_major_release"))
//                .fromHashOf("terrain")
//                .nextLong()
//            return TERRAIN_NOISE.computeIfAbsent(seed) { seed: Long? -> LegacyTerrainNoise(seed!!) }
//        }
//
//        private fun sampleDepthNoise(noise: LegacyTerrainNoise, noiseX: Double, noiseZ: Double): Double {
//            return noise.depth.sampleXZWrapped(noiseX, noiseZ, DEPTH_NOISE_SCALE_XZ, DEPTH_NOISE_SCALE_XZ)
//        }
//
//        private fun sampleMajorReleaseDensity(
//            noise: LegacyTerrainNoise, noiseX: Double, noiseY: Double,
//            noiseZ: Double, heightConfig: HeightConfig, rawDepthNoise: Double = sampleDepthNoise(noise, noiseX, noiseZ),
//        ): Double {
//            var depthNoise = rawDepthNoise
//            depthNoise /= 8000.0
//
//            if (depthNoise < 0.0) {
//                depthNoise = -depthNoise * 0.3
//            }
//
//            depthNoise = depthNoise * 3.0 - 2.0
//
//            if (depthNoise < 0.0) {
//                depthNoise /= 2.0
//                depthNoise = max(depthNoise, -1.0)
//                depthNoise /= 1.4
//                depthNoise /= 2.0
//            } else {
//                depthNoise = min(depthNoise, 1.0)
//                depthNoise /= 8.0
//            }
//
//            var depth = heightConfig.depth + depthNoise * 0.2
//            depth *= BASE_SIZE / 8.0
//            depth = BASE_SIZE + depth * 4.0
//
//            var densityOffset: Double = ((noiseY - depth) * STRETCH_Y) / heightConfig.scale
//            if (densityOffset < 0.0) {
//                densityOffset *= 4.0
//            }
//
//            val mainNoise: Double = (noise.main.sampleWrapped(
//                noiseX,
//                noiseY,
//                noiseZ,
//                COORDINATE_SCALE / MAIN_NOISE_SCALE_XZ,
//                HEIGHT_SCALE / MAIN_NOISE_SCALE_Y,
//                COORDINATE_SCALE / MAIN_NOISE_SCALE_XZ
//            ) / 10.0 + 1.0) / 2.0
//
//            var density: Double
//            if (mainNoise < 0.0) {
//                density = noise.minLimit.sampleWrapped(
//                    noiseX,
//                    noiseY,
//                    noiseZ,
//                    COORDINATE_SCALE,
//                    HEIGHT_SCALE,
//                    COORDINATE_SCALE
//                ) / LOWER_LIMIT_SCALE
//            } else if (mainNoise > 1.0) {
//                density = noise.maxLimit.sampleWrapped(
//                    noiseX,
//                    noiseY,
//                    noiseZ,
//                    COORDINATE_SCALE,
//                    HEIGHT_SCALE,
//                    COORDINATE_SCALE
//                ) / UPPER_LIMIT_SCALE
//            } else {
//                val minLimitNoise: Double = noise.minLimit.sampleWrapped(
//                    noiseX,
//                    noiseY,
//                    noiseZ,
//                    COORDINATE_SCALE,
//                    HEIGHT_SCALE,
//                    COORDINATE_SCALE
//                ) / LOWER_LIMIT_SCALE
//                val maxLimitNoise: Double = noise.maxLimit.sampleWrapped(
//                    noiseX,
//                    noiseY,
//                    noiseZ,
//                    COORDINATE_SCALE,
//                    HEIGHT_SCALE,
//                    COORDINATE_SCALE
//                ) / UPPER_LIMIT_SCALE
//                density = Mth.lerp(mainNoise, minLimitNoise, maxLimitNoise)
//            }
//
//            density -= densityOffset
//
//            if (noiseY > 29.0) {
//                val topFade = (noiseY - 29.0) / 3.0
//                density = Mth.lerp(topFade, density, -10.0)
//            }
//
//            return density
//        }
//
//        private fun baseStone(): BlockState {
//            return BASE_STONE
//        }
//
//        private fun liquidCandy(): BlockState? {
//            return LIQUID_CANDY
//        }
//
//        private fun springFluidForMountain(biomeId: ResourceLocation): BlockState? {
//            return if (biomeId == SUGAR_HELL_MOUNTAINS)
//                liquidCandy()
//            else
//                CCBlocks.GRENADINE.get().defaultBlockState()
//        }
//
//        private fun isBaseStone(state: BlockState): Boolean {
//            return state.`is`(CCBlocks.CRYSTALLIZED_SUGAR.get())
//        }
//
//        private fun surfaceMaterials(
//            biomeId: ResourceLocation,
//            worldX: Int,
//            worldZ: Int,
//            randomState: RandomState,
//        ): SurfaceMaterials {
//            if (biomeId == COTTON_CANDY_PLAINS) {
//                val under: BlockState = CCBlocks.MILK_BROWNIE_BLOCK.get().defaultBlockState()
//                return SurfaceMaterials(CCBlocks.CANDY_GRASS_BLOCK.get().defaultBlockState(), under, under)
//            }
//            if (biomeId == CHOCOLATE_FOREST) {
//                val under: BlockState = CCBlocks.WHITE_BROWNIE_BLOCK.get().defaultBlockState()
//                return SurfaceMaterials(
//                    CCBlocks.CHOCOLATE_COVERED_WHITE_BROWNIE.get().defaultBlockState(),
//                    under,
//                    under
//                )
//            }
//            if (biomeId == GUMMY_SWAMP) {
//                return gummySurfaceMaterials(worldX, worldZ, randomState)
//            }
//            if (biomeId == ICE_CREAM_SKY_MOUNTAINS) {
//                return SurfaceMaterials(
//                    CCBlocks.ICE_CREAM.get().defaultBlockState(),
//                    CCBlocks.FLOUR.get().defaultBlockState(),
//                    CCBlocks.FLOUR.get().defaultBlockState()
//                )
//            }
//            val under: BlockState = CCBlocks.FLOUR.get().defaultBlockState()
//            return SurfaceMaterials(CCBlocks.PUDDING.get().defaultBlockState(), under, under)
//        }
//
//        private fun gummySurfaceMaterials(worldX: Int, worldZ: Int, randomState: RandomState): SurfaceMaterials {
//            val noise: Double = octaveNoise2D(
//                worldX * 0.0075,
//                worldZ * 0.0075,
//                4,
//                worldSeed(randomState) xor 0x6A6D6D7953555246L
//            ) * 12.0
//            var index = (noise * 1.6).toInt() % 10
//            if (index < 0) {
//                index += 10
//            }
//            return when (index) {
//                1, 8 -> SurfaceMaterials(
//                    CCBlocks.ORANGE_GUMMY_BLOCK.get().defaultBlockState(),
//                    CCBlocks.ORANGE_HARDENED_GUMMY_BLOCK.get().defaultBlockState(),
//                    CCBlocks.ORANGE_GUMMY_BLOCK.get().defaultBlockState()
//                )
//
//                2, 5, 7 -> SurfaceMaterials(
//                    CCBlocks.YELLOW_GUMMY_BLOCK.get().defaultBlockState(),
//                    CCBlocks.YELLOW_HARDENED_GUMMY_BLOCK.get().defaultBlockState(),
//                    CCBlocks.YELLOW_GUMMY_BLOCK.get().defaultBlockState()
//                )
//
//                3, 4 -> SurfaceMaterials(
//                    CCBlocks.GREEN_GUMMY_BLOCK.get().defaultBlockState(),
//                    CCBlocks.GREEN_HARDENED_GUMMY_BLOCK.get().defaultBlockState(),
//                    CCBlocks.GREEN_GUMMY_BLOCK.get().defaultBlockState()
//                )
//
//                6 -> SurfaceMaterials(
//                    CCBlocks.WHITE_GUMMY_BLOCK.get().defaultBlockState(),
//                    CCBlocks.WHITE_HARDENED_GUMMY_BLOCK.get().defaultBlockState(),
//                    CCBlocks.WHITE_GUMMY_BLOCK.get().defaultBlockState()
//                )
//
//                else -> SurfaceMaterials(
//                    CCBlocks.RED_GUMMY_BLOCK.get().defaultBlockState(),
//                    CCBlocks.RED_HARDENED_GUMMY_BLOCK.get().defaultBlockState(),
//                    CCBlocks.RED_GUMMY_BLOCK.get().defaultBlockState()
//                )
//            }
//        }
//
//        private fun packColumnPos(x: Int, z: Int): Long {
//            return (x.toLong() shl 32) xor (z.toLong() and 0xFFFFFFFFL)
//        }
//
//        private fun underwaterMaterial(replaced: Int): BlockState {
//            return if (replaced == 0)
//                CCBlocks.SUGAR_SAND.get().defaultBlockState()
//            else
//                CCBlocks.FLOUR.get().defaultBlockState()
//        }
//
//        private fun makeParabolicField(): FloatArray {
//            val field = FloatArray(25)
//            for (x in -2..2) {
//                for (z in -2..2) {
//                    field[x + 2 + (z + 2) * 5] = 10.0f / Mth.sqrt(x * x + z * z + 0.2f)
//                }
//            }
//            return field
//        }
//
//        private fun octaveNoise2D(x: Double, z: Double, octaves: Int, salt: Long): Double {
//            var value = 0.0
//            var amplitude = 1.0
//            var frequency = 1.0
//            var total = 0.0
//
//            for (i in 0..<octaves) {
//                value += smoothNoise2D(x * frequency, z * frequency, salt + i * 0x632BE59BD9B4E019L) * amplitude
//                total += amplitude
//                amplitude *= 0.5
//                frequency *= 2.0
//            }
//
//            return value / total
//        }
//
//        private fun octaveNoise3D(x: Double, y: Double, z: Double, octaves: Int, salt: Long): Double {
//            var value = 0.0
//            var amplitude = 1.0
//            var frequency = 1.0
//            var total = 0.0
//
//            for (i in 0..<octaves) {
//                value += smoothNoise3D(
//                    x * frequency,
//                    y * frequency,
//                    z * frequency,
//                    salt + i * 0x632BE59BD9B4E019L
//                ) * amplitude
//                total += amplitude
//                amplitude *= 0.5
//                frequency *= 2.0
//            }
//
//            return value / total
//        }
//
//        private fun smoothNoise2D(x: Double, z: Double, salt: Long): Double {
//            val x0 = Mth.floor(x)
//            val z0 = Mth.floor(z)
//            val tx: Double = fade(x - x0)
//            val tz: Double = fade(z - z0)
//            val a: Double = randomUnit(x0, 0, z0, salt)
//            val b: Double = randomUnit(x0 + 1, 0, z0, salt)
//            val c: Double = randomUnit(x0, 0, z0 + 1, salt)
//            val d: Double = randomUnit(x0 + 1, 0, z0 + 1, salt)
//            return Mth.lerp(tz, Mth.lerp(tx, a, b), Mth.lerp(tx, c, d))
//        }
//
//        private fun smoothNoise3D(x: Double, y: Double, z: Double, salt: Long): Double {
//            val x0 = Mth.floor(x)
//            val y0 = Mth.floor(y)
//            val z0 = Mth.floor(z)
//            val tx: Double = fade(x - x0)
//            val ty: Double = fade(y - y0)
//            val tz: Double = fade(z - z0)
//            val a = Mth.lerp(tx, randomUnit(x0, y0, z0, salt), randomUnit(x0 + 1, y0, z0, salt))
//            val b = Mth.lerp(tx, randomUnit(x0, y0, z0 + 1, salt), randomUnit(x0 + 1, y0, z0 + 1, salt))
//            val c = Mth.lerp(tx, randomUnit(x0, y0 + 1, z0, salt), randomUnit(x0 + 1, y0 + 1, z0, salt))
//            val d = Mth.lerp(tx, randomUnit(x0, y0 + 1, z0 + 1, salt), randomUnit(x0 + 1, y0 + 1, z0 + 1, salt))
//            return Mth.lerp(tz, Mth.lerp(ty, a, c), Mth.lerp(ty, b, d))
//        }
//
//        private fun fade(value: Double): Double {
//            return value * value * value * (value * (value * 6.0 - 15.0) + 10.0)
//        }
//
//        private fun randomUnit(x: Int, y: Int, z: Int, salt: Long): Double {
//            val bits: Long = hash(x, y, z, salt)
//            return ((bits ushr 11) * 1.1102230246251565E-16) * 2.0 - 1.0
//        }
//
//        private fun hash(x: Int, y: Int, z: Int): Int {
//            return hash(x, y, z, -0x340d631b7bdddcdbL).toInt()
//        }
//
//        private fun positiveHash(x: Int, y: Int, z: Int, salt: Long): Long {
//            return hash(x, y, z, salt) and Long.Companion.MAX_VALUE
//        }
//
//        private fun hash(x: Int, y: Int, z: Int, salt: Long): Long {
//            var h = salt
//            h = h xor x * -0x61c8864680b583ebL
//            h = Long.rotateLeft(h, 27) * -0x6b2fb644ecceee15L
//            h = h xor y * -0x3d4d51c2d82b14b1L
//            h = Long.rotateLeft(h, 31) * 0x2545F4914F6CDD1DL
//            h = h xor z * 0x165667B19E3779F9L
//            h = h xor (h ushr 33)
//            h *= -0xae502812aa7333L
//            h = h xor (h ushr 33)
//            h *= -0x3b314601e57a13adL
//            h = h xor (h ushr 33)
//            return h
//        }
//    }
//
//    /** 生物群系形状缓存（记录每个列的基础高度和起伏）。  */
//    private val biomeShapeCache = ConcurrentHashMap<Long, BiomeShape>()
//
//    /** 巧克力池塘扫描结果缓存（记录哪些水列应变为巧克力）。  */
//    private val pondChocolateCache = ConcurrentHashMap<Long, Boolean>()
//
//    /** 开放水域列缓存。  */
//    private val openWaterColumnCache = ConcurrentHashMap<Long, Boolean>()
//
//    /** 当前缓存的生物群系种子，用于失效判断。  */
//    @Volatile
//    private var biomeShapeSeed = Long.MIN_VALUE
//
//
//    override fun getBiomeSource() = super.biomeSource as CandyBiomeSource
//
//    override fun codec() = CODEC
//
//    /**
//     * 创建结构（阶段：createStructures）。
//     * 在结构生成的生物群系检查前，同步生物群系源的真实世界种子，
//     * 以确保最早生成区块时生物群系数据一致。
//     */
//    override fun createStructures(
//        registryAccess: RegistryAccess, structureState: ChunkGeneratorStructureState,
//        structureManager: StructureManager, chunk: ChunkAccess, templateManager: StructureTemplateManager,
//    ) {
//        syncBiomeSourceSeed(structureState.randomState())
//        super.createStructures(registryAccess, structureState, structureManager, chunk, templateManager)
//    }
//
//    /**
//     * 生成生物群系（阶段：createBiomes）。
//     * 区块的生物群系容器在此阶段被烘焙，因此在 fillFromNoise 之前必须先同步种子。
//     */
//    override fun createBiomes(
//        executor: Executor, randomState: RandomState, blender: Blender,
//        structureManager: StructureManager, chunk: ChunkAccess,
//    ): CompletableFuture<ChunkAccess?> {
//        syncBiomeSourceSeed(randomState)
//        return super.createBiomes(executor, randomState, blender, structureManager, chunk)
//    }
//
//    // ========================== 主要地形生成 ==========================
//    /**
//     * 使用旧版密度场算法填充区块地形（核心地形生成阶段）。
//     *
//     *
//     * 流程：
//     *
//     *  1. 同步生物群系种子。
//     *  1. 调用 fillLegacyTerrain 进行密度场地形生成。
//     *  1. 预计算高度图（WORLD_SURFACE_WG、OCEAN_FLOOR_WG 等）。
//     *
//     */
//    override fun fillFromNoise(
//        executor: Executor, blender: Blender, randomState: RandomState,
//        structureManager: StructureManager, chunk: ChunkAccess,
//    ): CompletableFuture<ChunkAccess> {
//        return CompletableFuture.supplyAsync({
//            syncBiomeSourceSeed(randomState)
//            fillLegacyTerrain(chunk, randomState)
//            Heightmap.primeHeightmaps(
//                chunk, setOf<Heightmap.Types?>(
//                    Heightmap.Types.WORLD_SURFACE_WG,
//                    Heightmap.Types.OCEAN_FLOOR_WG,
//                    Heightmap.Types.MOTION_BLOCKING,
//                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
//                )
//            )
//            chunk
//        }, executor)
//    }
//
//    /**
//     * 将生物群系源的世界种子与当前随机状态同步。
//     * 当世界种子发生变化时，会清空所有相关缓存。
//     */
//    private fun syncBiomeSourceSeed(randomState: RandomState) {
//        val seed = worldSeed(randomState)
//        if (biomeShapeSeed != seed) {
//            biomeShapeCache.clear()
//            pondChocolateCache.clear()
//            openWaterColumnCache.clear()
//            biomeShapeSeed = seed
//        }
//        getBiomeSource().seed = seed
//    }
//
//    /**
//     * 填充旧版地形的核心方法。
//     *
//     *
//     * 算法简述：
//     * 生成 5×5×33 的密度网格（NOISE_SIZE_XZ × NOISE_SIZE_Y × NOISE_SIZE_XZ），
//     * 然后对每个 4×4×8 的单元格进行三线性插值，逐块决定方块类型：
//     *
//     *  * 密度 &gt; 0 → 结晶糖（BASE_STONE）
//     *  * 密度 ≤ 0 且 y &lt; 海平面 → 对应列液体（水或巧克力）
//     *  * 其余 → 空气
//     *
//     * 最后生成基岩层和旧式洞穴。
//     */
//    private fun fillLegacyTerrain(chunk: ChunkAccess, randomState: RandomState) {
//        val pos = chunk.pos
//        // 生成该区块的密度图（4×4 单元格对应 5×5 噪声采样点，Y 方向 33 个采样点）
//        val heightMap = generateHeightMap(pos.x * 4, pos.z * 4, randomState)
//        val mutable = BlockPos.MutableBlockPos()
//        val worldXs = IntArray(16)
//        val worldZs = IntArray(16)
//        for (i in 0..15) {
//            worldXs[i] = pos.getBlockX(i)
//            worldZs[i] = pos.getBlockZ(i)
//        }
//
//        // 预计算每个列的液体类型（水或巧克力），避免在每个 Y 层级重复查询生物群系
//        val columnFluids = arrayOfNulls<BlockState>(256)
//        for (localX in 0..15) {
//            for (localZ in 0..15) {
//                columnFluids[localX * 16 + localZ] = fluidForColumn(worldXs[localX], worldZs[localZ], randomState)
//            }
//        }
//
//        // 遍历单元格（4×4 个，Y 方向 32 个单元格 = 256 格）
//        for (cellX in 0..3) {
//            val x0: Int = cellX * NOISE_SIZE_XZ
//            val x1: Int = (cellX + 1) * NOISE_SIZE_XZ
//
//            for (cellZ in 0..3) {
//                // 对应四个角点的密度数组索引
//                val z0: Int = (x0 + cellZ) * NOISE_SIZE_Y
//                val z1: Int = (x0 + cellZ + 1) * NOISE_SIZE_Y
//                val z2: Int = (x1 + cellZ) * NOISE_SIZE_Y
//                val z3: Int = (x1 + cellZ + 1) * NOISE_SIZE_Y
//
//                for (cellY in 0..31) {
//                    // 读取单元格八个角点的密度值
//                    var density000 = heightMap[z0 + cellY]
//                    var density001 = heightMap[z1 + cellY]
//                    var density100 = heightMap[z2 + cellY]
//                    var density101 = heightMap[z3 + cellY]
//
//                    // 计算 Y 方向步长（用于后续线性插值）
//                    val step000: Double = (heightMap[z0 + cellY + 1] - density000) / CELL_HEIGHT
//                    val step001: Double = (heightMap[z1 + cellY + 1] - density001) / CELL_HEIGHT
//                    val step100: Double = (heightMap[z2 + cellY + 1] - density100) / CELL_HEIGHT
//                    val step101: Double = (heightMap[z3 + cellY + 1] - density101) / CELL_HEIGHT
//
//                    // 对单元格内部进行三线性插值
//                    for (subY in 0..<CELL_HEIGHT) {
//                        var yLerp0 = density000
//                        var yLerp1 = density001
//                        val xStep0: Double = (density100 - density000) / CELL_WIDTH
//                        val xStep1: Double = (density101 - density001) / CELL_WIDTH
//
//                        for (subX in 0..<CELL_WIDTH) {
//                            var density = yLerp0
//                            val zStep: Double = (yLerp1 - yLerp0) / CELL_WIDTH
//
//                            for (subZ in 0..<CELL_WIDTH) {
//                                val localX: Int = cellX * CELL_WIDTH + subX
//                                val localZ: Int = cellZ * CELL_WIDTH + subZ
//                                val worldX = worldXs[localX]
//                                val worldZ = worldZs[localZ]
//                                val y: Int = cellY * CELL_HEIGHT + subY
//
//                                // 密度 > 0 → 固体（结晶糖）；否则海平面以下为液体，以上为空气
//                                val state: BlockState = (if (density > 0.0)
//                                    BASE_STONE
//                                else
//                                    if (y < SEA_LEVEL) columnFluids[localX * 16 + localZ] else AIR)!!
//                                if (!state.isAir && y >= chunk.minBuildHeight && y < chunk.maxBuildHeight) {
//                                    chunk.setBlockState(mutable.set(worldX, y, worldZ), state, false)
//                                }
//                                density += zStep
//                            }
//
//                            yLerp0 += xStep0
//                            yLerp1 += xStep1
//                        }
//
//                        density000 += step000
//                        density001 += step001
//                        density100 += step100
//                        density101 += step101
//                    }
//                }
//            }
//        }
//
//        // 生成底部平整层和洞穴
//        applyBedrock(chunk)
//        carveLegacyCaves(chunk, randomState)
//    }
//
//    /**
//     * 生成 5×5×33 的密度图。
//     *
//     * @param baseNoiseX 区块对应的噪声起始 X 坐标（区块 X × 4）
//     * @param baseNoiseZ 区块对应的噪声起始 Z 坐标（区块 Z × 4）
//     */
//    private fun generateHeightMap(baseNoiseX: Int, baseNoiseZ: Int, randomState: RandomState): DoubleArray {
//        val heightMap = DoubleArray(NOISE_SIZE_XZ * NOISE_SIZE_Y * NOISE_SIZE_XZ)
//        val noise: LegacyTerrainNoise = terrainNoise(randomState)
//        var index = 0
//
//        for (x in 0..<NOISE_SIZE_XZ) {
//            for (z in 0..<NOISE_SIZE_XZ) {
//                // 获取该采样点的生物群系高度配置
//                val heightConfig = heightConfigAt((baseNoiseX + x).toDouble(), (baseNoiseZ + z).toDouble(), randomState)
//                // 采样深度噪声
//                val depthNoise: Double =
//                    sampleDepthNoise(noise, (baseNoiseX + x).toDouble(), (baseNoiseZ + z).toDouble())
//
//                for (y in 0..<NOISE_SIZE_Y) {
//                    heightMap[index++] = sampleMajorReleaseDensity(
//                        noise,
//                        (baseNoiseX + x).toDouble(),
//                        y.toDouble(),
//                        (baseNoiseZ + z).toDouble(),
//                        heightConfig,
//                        depthNoise
//                    )
//                }
//            }
//        }
//
//        return heightMap
//    }
//
//    // ========================== 地表生成 ==========================
//    /**
//     * 构建地表（阶段：buildSurface）。
//     * 覆盖在 fillFromNoise 生成的基础地形之上，根据生物群系放置地表方块。
//     * 还会生成地表池塘、山脉糖果泉和粉色结晶糖装饰。
//     */
//    override fun buildSurface(
//        region: WorldGenRegion, structureManager: StructureManager, randomState: RandomState,
//        chunk: ChunkAccess,
//    ) {
//        syncBiomeSourceSeed(randomState)
//        val mutable = BlockPos.MutableBlockPos()
//        val pos = chunk.pos
//
//        // 遍历每个列，用群系对应的地表材质替换顶层方块
//        for (localX in 0..15) {
//            val worldX = pos.getBlockX(localX)
//            for (localZ in 0..15) {
//                val worldZ = pos.getBlockZ(localZ)
//                val biomeId = biomeId(worldX, worldZ, randomState)
//                val materials: SurfaceMaterials = surfaceMaterials(biomeId, worldX, worldZ, randomState)
//                val top = findTopSolid(chunk, worldX, worldZ)
//                // 判断是否为水下环境（海平面以下，或海洋/河流群系）
//                val underwater = top < SEA_LEVEL - 1 || biomeId == SUGAR_OCEANS || biomeId == SUGAR_RIVER
//                // 地表替换深度：3~5 格，带随机变化
//                val depth: Int = 3 + abs(hash(worldX, 0, worldZ)) % 3
//                var replaced = 0
//
//                var y = top
//                while (y > MIN_Y && replaced <= depth) {
//                    val state = chunk.getBlockState(mutable.set(worldX, y, worldZ))
//                    if (!isBaseStone(state)) {
//                        // 如果已经开始替换但遇到非基岩方块，停止向下
//                        if (replaced > 0) {
//                            break
//                        }
//                        --y
//                        continue
//                    }
//
//                    // 水下使用水下材质，非水下则使用表层/次表层材质
//                    val replacement =
//                        (if (underwater) underwaterMaterial(replaced) else if (replaced > 0) materials.under else materials.top)!!
//                    chunk.setBlockState(mutable, replacement, false)
//                    replaced++
//                    --y
//                }
//            }
//        }
//
//        // 生成地表池塘（石榴糖浆 / 液态糖果）
//        generateSurfacePools(region, chunk, randomState)
//        // 生成山脉糖果泉
//        generateMountainCandySprings(region, chunk, randomState)
//        // 生成粉色结晶糖装饰
//        decoratePinkCrystallizedSugar(region, chunk, randomState)
//    }
//
//    /**
//     * 在粉色糖浆（液态糖果）周围生成稀疏的粉色结晶糖边框。
//     * 这个装饰是稀疏扫描，每列只有 1/8 的概率会被检查，以避免影响区块加载性能。
//     */
//    private fun decoratePinkCrystallizedSugar(region: WorldGenRegion, chunk: ChunkAccess, randomState: RandomState) {
//        val chunkPos = chunk.pos
//        val seed: Long = worldSeed(randomState) xor 0x50494E4B5F435259L // 盐：'PINK_CRY'
//        val maxY = min(chunk.maxBuildHeight - 1, HEIGHT - 1)
//        val liquidPos = BlockPos.MutableBlockPos()
//        val targetPos = BlockPos.MutableBlockPos()
//
//        for (localX in 0..15) {
//            val worldX = chunkPos.getBlockX(localX)
//            for (localZ in 0..15) {
//                val worldZ = chunkPos.getBlockZ(localZ)
//                // 稀疏扫描：仅处理哈希值低 3 位为 0 的列（概率 1/8）
//                if ((positiveHash(worldX, 0, worldZ, seed) and 7L) != 0L) {
//                    continue
//                }
//                for (worldY in MIN_Y + 1..maxY) {
//                    val liquid = chunk.getBlockState(liquidPos.set(worldX, worldY, worldZ))
//                    // 找到液态糖果方块
//                    if (!liquid.`is`(CCBlocks.LIQUID_CANDY.get())) {
//                        continue
//                    }
//
//                    // 检查液态糖果的六个相邻方向
//                    for (direction in Direction.entries) {
//                        val targetX = worldX + direction.stepX
//                        val targetY = worldY + direction.stepY
//                        val targetZ = worldZ + direction.stepZ
//                        // 确保目标位置在区块范围内
//                        if (targetY <= MIN_Y || targetY > maxY || targetX < chunkPos.minBlockX || targetX > chunkPos.maxBlockX || targetZ < chunkPos.minBlockZ || targetZ > chunkPos.maxBlockZ) {
//                            continue
//                        }
//
//                        val target = chunk.getBlockState(targetPos.set(targetX, targetY, targetZ))
//                        // 跳过空气、液体、基岩和已存在的粉色结晶糖
//                        if (target.isAir || !target.fluidState
//                                .isEmpty || target.`is`(Blocks.BEDROCK) || target.`is`(CCBlocks.PINK_CRYSTALLIZED_SUGAR.get())
//                        ) {
//                            continue
//                        }
//
//                        // 判断目标是否靠近地表（影响生成概率）
//                        val surface = targetY >= SEA_LEVEL - 4
//                        val validTarget = isBaseStone(target)
//                                || (surface && target.isCollisionShapeFullBlock(region, targetPos))
//                        if (!validTarget) {
//                            continue
//                        }
//
//                        // 生成概率：朝下概率更高，地表概率更高
//                        val chance =
//                            if (direction == Direction.DOWN) (if (surface) 10 else 22) else (if (surface) 18 else 32)
//                        if (positiveHash(targetX, targetY, targetZ, seed) % chance == 0L) {
//                            chunk.setBlockState(
//                                targetPos,
//                                CCBlocks.PINK_CRYSTALLIZED_SUGAR.get().defaultBlockState(),
//                                false
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * 生成地表池塘。
//     * 每个区块有低概率生成石榴糖浆池塘（1/1200）或液态糖果池塘（1/1400）。
//     */
//    private fun generateSurfacePools(region: WorldGenRegion, chunk: ChunkAccess, randomState: RandomState) {
//        val pos = chunk.pos
//        val seed: Long = worldSeed(randomState)
//        // 石榴糖浆池塘判定
//        val poolRoll: Long = positiveHash(pos.x, pos.z, 0, seed xor 0x4752454E4144494EL) // 'GRENADIE'
//        if (poolRoll % 1200L == 0L) {
//            generateSurfacePool(
//                region,
//                chunk,
//                randomState,
//                CCBlocks.GRENADINE.get().defaultBlockState(),
//                seed xor 0x6C616B655F677265L
//            )
//        }
//
//        // 液态糖果池塘判定
//        val candyRoll: Long = positiveHash(pos.x, pos.z, 0, seed xor 0x4C49515549444341L) // 'LIQUIDCA'
//        if (candyRoll % 1400L == 0L) {
//            generateSurfacePool(region, chunk, randomState, liquidCandy()!!, seed xor 0x6C616B655F63616EL)
//        }
//    }
//
//    /**
//     * 生成单个地表池塘（由多个椭球体拼接而成）。
//     * 池塘中心位于区块中心，深度约 3 格，使用流体填充。
//     *
//     * @param fluid 池塘使用的流体
//     * @param salt  随机种子盐
//     */
//    private fun generateSurfacePool(
//        region: WorldGenRegion,
//        chunk: ChunkAccess,
//        randomState: RandomState,
//        fluid: BlockState,
//        salt: Long,
//    ) {
//        val pos = chunk.pos
//        val bits: Long = positiveHash(pos.x, pos.z, 0, salt)
//        val centerX = pos.getBlockX(8)
//        val centerZ = pos.getBlockZ(8)
//        val biomeId = biomeId(centerX, centerZ, randomState)
//        // 海洋/河流群系不生成池塘
//        if (biomeId == SUGAR_OCEANS || biomeId == SUGAR_RIVER) {
//            return
//        }
//
//        val centerTop = findTopTerrain(chunk, centerX, centerZ)
//        // 限制生成高度范围
//        if (centerTop <= SEA_LEVEL - 4 || centerTop >= HEIGHT - 3) {
//            return
//        }
//
//        val random = Random(bits)
//        // 使用多个随机椭球体构建池塘形状
//        val lake = BooleanArray(16 * 16 * 8)
//        val ellipsoids = 4 + random.nextInt(4)
//        for (i in 0..<ellipsoids) {
//            val sizeX = random.nextDouble() * 6.0 + 3.0
//            val sizeY = random.nextDouble() * 4.0 + 2.0
//            val sizeZ = random.nextDouble() * 6.0 + 3.0
//            val ellipsoidX = random.nextDouble() * (16.0 - sizeX - 2.0) + 1.0 + sizeX / 2.0
//            val ellipsoidY = random.nextDouble() * (8.0 - sizeY - 4.0) + 2.0 + sizeY / 2.0
//            val ellipsoidZ = random.nextDouble() * (16.0 - sizeZ - 2.0) + 1.0 + sizeZ / 2.0
//            // 填充椭圆体内部
//            for (x in 1..14) {
//                val dx = (x - ellipsoidX) / (sizeX / 2.0)
//                for (z in 1..14) {
//                    val dz = (z - ellipsoidZ) / (sizeZ / 2.0)
//                    for (y in 1..6) {
//                        val dy = (y - ellipsoidY) / (sizeY / 2.0)
//                        if (dx * dx + dy * dy + dz * dz < 1.0) {
//                            lake[(x * 16 + z) * 8 + y] = true
//                        }
//                    }
//                }
//            }
//        }
//
//        val mutable = BlockPos.MutableBlockPos()
//        val waterline = centerTop - 1
//        val originY = waterline - 3
//
//        // 填充池塘流体
//        for (x in 0..15) {
//            val worldX = pos.minBlockX + x
//            for (z in 0..15) {
//                val worldZ = pos.minBlockZ + z
//                // 跳过地形高度变化过大的列
//                if (abs(findTopTerrain(chunk, worldX, worldZ) - centerTop) > 4) {
//                    continue
//                }
//                for (y in 0..7) {
//                    if (!lake[(x * 16 + z) * 8 + y]) {
//                        continue
//                    }
//                    val worldY = originY + y
//                    if (worldY <= levelMin(chunk) || worldY >= chunk.maxBuildHeight) {
//                        continue
//                    }
//                    // 池塘底部 4 层填流体，上方填空气
//                    val state = if (y < 4) fluid else AIR
//                    chunk.setBlockState(mutable.set(worldX, worldY, worldZ), state, false)
//                    scheduleFluidTick(region, mutable, state)
//                }
//            }
//        }
//
//        // 在池塘底部铺设地表下层材质
//        for (x in 0..15) {
//            val worldX = pos.minBlockX + x
//            for (z in 0..15) {
//                val worldZ = pos.minBlockZ + z
//                val localBiome = biomeId(worldX, worldZ, randomState)
//                val rim: BlockState = surfaceMaterials(localBiome, worldX, worldZ, randomState).under
//                for (y in 0..7) {
//                    if (!lake[(x * 16 + z) * 8 + y] || y >= 4) {
//                        continue
//                    }
//                    val worldY = originY + y
//                    val floor: BlockPos = mutable.set(worldX, worldY - 1, worldZ)
//                    if (worldY > levelMin(chunk) && chunk.getBlockState(floor).isAir) {
//                        chunk.setBlockState(floor, rim, false)
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * 在山脉生物群系中生成糖果泉。
//     * 每个区块有 1/90 概率尝试，仅在糖山脉、地狱糖果山脉或冰淇淋天空山脉中生成。
//     */
//    private fun generateMountainCandySprings(region: WorldGenRegion, chunk: ChunkAccess, randomState: RandomState) {
//        val pos = chunk.pos
//        val seed: Long = worldSeed(randomState)
//        val roll: Long = positiveHash(pos.x, pos.z, 0, seed xor 0x4D544E5F53595250L) // 'MTN_SYRP'
//        if (roll % 90L != 0L) {
//            return
//        }
//
//        // 从区块内随机选择一个位置
//        val worldX = pos.getBlockX(2 + ((roll ushr 8) and 11L).toInt())
//        val worldZ = pos.getBlockZ(2 + ((roll ushr 12) and 11L).toInt())
//        val biomeId = biomeId(worldX, worldZ, randomState)
//        if ((biomeId != SUGAR_MOUNTAINS) && (biomeId != SUGAR_HELL_MOUNTAINS) && (biomeId != ICE_CREAM_SKY_MOUNTAINS)) {
//            return
//        }
//
//        val top = findTopTerrain(chunk, worldX, worldZ)
//        if (top < SEA_LEVEL + 8 || top > HEIGHT - 8) {
//            return
//        }
//
//        val mutable = BlockPos.MutableBlockPos()
//        for (attempt in 0..5) {
//            val y = top - 2 - attempt * 2
//            if (y <= SEA_LEVEL || y >= HEIGHT - 2) {
//                continue
//            }
//            if (tryPlaceMountainCandySpring(
//                    region,
//                    chunk,
//                    mutable,
//                    worldX,
//                    y,
//                    worldZ,
//                    springFluidForMountain(biomeId)!!
//                )
//            ) {
//                return
//            }
//        }
//    }
//
//    private fun tryPlaceMountainCandySpring(
//        region: WorldGenRegion,
//        chunk: ChunkAccess,
//        mutable: BlockPos.MutableBlockPos,
//        worldX: Int,
//        y: Int,
//        worldZ: Int,
//        fluid: BlockState,
//    ): Boolean {
//        if (!isBaseStone(chunk.getBlockState(mutable.set(worldX, y, worldZ)))) {
//            return false
//        }
//
//        var airSides = 0
//        var solidSides = 0
//        var outX = worldX
//        var outZ = worldZ
//        val directions = arrayOf<IntArray>(intArrayOf(1, 0), intArrayOf(-1, 0), intArrayOf(0, 1), intArrayOf(0, -1))
//        for (direction in directions) {
//            val neighbor = chunk.getBlockState(mutable.set(worldX + direction[0], y, worldZ + direction[1]))
//            if (neighbor.isAir) {
//                airSides++
//                outX = worldX + direction[0]
//                outZ = worldZ + direction[1]
//            } else if (isBaseStone(neighbor)) {
//                solidSides++
//            }
//        }
//
//        if (airSides != 1 || solidSides < 3 || !isBaseStone(chunk.getBlockState(mutable.set(worldX, y - 1, worldZ)))) {
//            return false
//        }
//
//        chunk.setBlockState(mutable.set(worldX, y, worldZ), fluid, false)
//        scheduleFluidTick(region, mutable, fluid)
//        chunk.setBlockState(mutable.set(outX, y, outZ), fluid, false)
//        scheduleFluidTick(region, mutable, fluid)
//        return true
//    }
//
//    override fun applyCarvers(
//        region: WorldGenRegion, seed: Long, randomState: RandomState, biomeManager: BiomeManager,
//        structureManager: StructureManager, chunk: ChunkAccess, carvingStep: GenerationStep.Carving,
//    ) {
//    }
//
//    override fun spawnOriginalMobs(region: WorldGenRegion) {
//        val chunkPos = region.center
//        val center = BlockPos(chunkPos.minBlockX + 8, SEA_LEVEL, chunkPos.minBlockZ + 8)
//        val biome = region.getBiome(center)
//        val random = RandomSource.create(region.seed xor Mth.getSeed(center))
//        NaturalSpawner.spawnMobsForChunkGeneration(region, biome, chunkPos, random)
//    }
//
//    override fun getGenDepth(): Int {
//        return HEIGHT
//    }
//
//    override fun getSeaLevel(): Int {
//        return SEA_LEVEL
//    }
//
//    override fun getMinY(): Int {
//        return MIN_Y
//    }
//
//    override fun getBaseHeight(
//        x: Int, z: Int, type: Heightmap.Types, heightAccessor: LevelHeightAccessor,
//        randomState: RandomState,
//    ): Int {
//        syncBiomeSourceSeed(randomState)
//        val column = buildColumn(x, z, heightAccessor, randomState)
//        for (i in column.indices.reversed()) {
//            val state = column[i]
//            if (type.isOpaque().test(state) || type == Heightmap.Types.WORLD_SURFACE_WG && !state.isAir) {
//                return heightAccessor.minBuildHeight + i + 1
//            }
//        }
//        return heightAccessor.minBuildHeight
//    }
//
//    override fun getBaseColumn(
//        x: Int,
//        z: Int,
//        heightAccessor: LevelHeightAccessor,
//        randomState: RandomState,
//    ): NoiseColumn {
//        syncBiomeSourceSeed(randomState)
//        return NoiseColumn(heightAccessor.minBuildHeight, buildColumn(x, z, heightAccessor, randomState))
//    }
//
//    override fun addDebugScreenInfo(info: MutableList<String?>, randomState: RandomState, pos: BlockPos) {
//        syncBiomeSourceSeed(randomState)
//        info.add("CandyCraft legacy 1.12-style density terrain")
//        info.add("Candy biome shape: " + biomeShape(pos.x, pos.z, randomState))
//    }
//
//    private fun buildColumn(
//        x: Int,
//        z: Int,
//        heightAccessor: LevelHeightAccessor,
//        randomState: RandomState,
//    ): Array<BlockState> {
//        val minY = heightAccessor.minBuildHeight
//        val height = heightAccessor.height
//        val states: Array<BlockState> = arrayOfNulls<BlockState>(height)
//        val noise: LegacyTerrainNoise = terrainNoise(randomState)
//        val noiseX = x / CELL_WIDTH.toDouble()
//        val noiseZ = z / CELL_WIDTH.toDouble()
//        val heightConfig = heightConfigAt(Mth.floor(noiseX).toDouble(), Mth.floor(noiseZ).toDouble(), randomState)
//        val columnFluid = fluidForColumn(x, z, randomState)
//
//        for (i in 0..<height) {
//            val y = minY + i
//            val noiseY = y / CELL_HEIGHT.toDouble()
//            val density = sampleMajorReleaseDensity(noise, noiseX, noiseY, noiseZ, heightConfig)
//            if (y <= MIN_Y + 1) {
//                states[i] = FLAT_BOTTOM
//            } else if (density > 0.0) {
//                states[i] = baseStone()
//            } else if (y < SEA_LEVEL) {
//                states[i] = columnFluid!!
//            } else {
//                states[i] = AIR
//            }
//        }
//
//        return states
//    }
//
//    private fun heightConfigAt(noiseX: Double, noiseZ: Double, randomState: RandomState): HeightConfig {
//        val seed: Long = worldSeed(randomState)
//        val center = biomeShape(Mth.floor(noiseX * CELL_WIDTH), Mth.floor(noiseZ * CELL_WIDTH), randomState)
//        var scale = 0.0
//        var depth = 0.0
//        var totalWeight = 0.0
//
//        for (offX in -2..2) {
//            for (offZ in -2..2) {
//                val nearby = biomeShape(
//                    Mth.floor((noiseX + offX) * CELL_WIDTH),
//                    Mth.floor((noiseZ + offZ) * CELL_WIDTH),
//                    randomState
//                )
//                val nearbyScale: Double = BIOME_SCALE_OFFSET + nearby.variation * BIOME_SCALE_WEIGHT
//                val nearbyDepth: Double = BIOME_DEPTH_OFFSET + nearby.baseHeight * BIOME_DEPTH_WEIGHT
//                var weight: Double = PARABOLIC_FIELD[offX + 2 + (offZ + 2) * 5] / max(nearbyDepth + 2.0, 0.01)
//
//                if (nearby.baseHeight > center.baseHeight) {
//                    weight *= 0.5
//                }
//
//                scale += nearbyScale * weight
//                depth += nearbyDepth * weight
//                totalWeight += weight
//            }
//        }
//
//        scale /= totalWeight
//        depth /= totalWeight
//        scale = scale * 0.9 + 0.1
//        depth = (depth * 4.0 - 1.0) / 8.0
//
//        val blockX: Double = noiseX * CELL_WIDTH
//        val blockZ: Double = noiseZ * CELL_WIDTH
//        val spawnIsland: Double = CandyBiomeSource.spawnIslandInfluence(blockX, blockZ, seed)
//        if (spawnIsland > 0.0) {
//            val islandNoise: Double = octaveNoise2D(blockX * 0.018, blockZ * 0.018, 4, seed xor 0x5A51A7D15A4EL)
//            val detailNoise: Double = octaveNoise2D(blockX * 0.055, blockZ * 0.055, 2, seed xor 0x51A7D15A4E1CL)
//            depth += spawnIsland * (0.045 + islandNoise * 0.035 + detailNoise * 0.012)
//            scale += spawnIsland * (0.035 + abs(islandNoise) * 0.025)
//        }
//
//        return HeightConfig(depth, scale)
//    }
//
//    private fun applyBedrock(chunk: ChunkAccess) {
//        val pos = chunk.pos
//        val mutable = BlockPos.MutableBlockPos()
//
//        for (localX in 0..15) {
//            val worldX = pos.getBlockX(localX)
//            for (localZ in 0..15) {
//                val worldZ = pos.getBlockZ(localZ)
//                chunk.setBlockState(mutable.set(worldX, MIN_Y, worldZ), FLAT_BOTTOM, false)
//                chunk.setBlockState(mutable.set(worldX, MIN_Y + 1, worldZ), FLAT_BOTTOM, false)
//            }
//        }
//    }
//
//    private fun carveLegacyCaves(chunk: ChunkAccess, randomState: RandomState) {
//        val seed = randomState.getOrCreateRandomFactory(ResourceLocation(CandyCraft.MODID, "legacy_major_release"))
//            .fromHashOf("caves")
//            .nextLong()
//        val seedRandom = Random(seed)
//        val xSeed = (seedRandom.nextLong() / 2L) * 2L + 1L
//        val zSeed = (seedRandom.nextLong() / 2L) * 2L + 1L
//        val pos = chunk.pos
//
//        for (sourceChunkX in pos.x - CARVER_RANGE..pos.x + CARVER_RANGE) {
//            for (sourceChunkZ in pos.z - CARVER_RANGE..pos.z + CARVER_RANGE) {
//                val random = Random(sourceChunkX.toLong() * xSeed + sourceChunkZ.toLong() * zSeed xor seed)
//                carveLegacyCaveStarts(chunk, sourceChunkX, sourceChunkZ, random)
//                carveLegacyRavineStart(chunk, sourceChunkX, sourceChunkZ, random)
//            }
//        }
//    }
//
//    private fun carveLegacyCaveStarts(chunk: ChunkAccess, sourceChunkX: Int, sourceChunkZ: Int, random: Random) {
//        var caveCount = random.nextInt(random.nextInt(random.nextInt(40) + 1) + 1)
//        if (random.nextInt(15) != 0) {
//            caveCount = 0
//        }
//
//        for (i in 0..<caveCount) {
//            val x = (sourceChunkX * 16 + random.nextInt(16)).toDouble()
//            val y = random.nextInt(random.nextInt(120) + 8).toDouble()
//            val z = (sourceChunkZ * 16 + random.nextInt(16)).toDouble()
//            var tunnelCount = 1
//
//            if (random.nextInt(4) == 0) {
//                carveLegacyCaveRoom(chunk, random, x, y, z)
//                tunnelCount += random.nextInt(4)
//            }
//
//            for (tunnel in 0..<tunnelCount) {
//                val yaw = random.nextFloat() * Mth.PI * 2.0f
//                val pitch = (random.nextFloat() - 0.5f) * 0.25f
//                var width = random.nextFloat() * 2.0f + random.nextFloat()
//                if (random.nextInt(10) == 0) {
//                    width *= random.nextFloat() * random.nextFloat() * 3.0f + 1.0f
//                }
//
//                carveLegacyTunnel(chunk, random.nextLong(), x, y, z, width, yaw, pitch, 0, 0, 1.0)
//            }
//        }
//    }
//
//    private fun carveLegacyCaveRoom(chunk: ChunkAccess, random: Random, x: Double, y: Double, z: Double) {
//        carveLegacyTunnel(chunk, random.nextLong(), x, y, z, 1.0f + random.nextFloat() * 6.0f, 0.0f, 0.0f, -1, -1, 0.5)
//    }
//
//    private fun carveLegacyTunnel(
//        chunk: ChunkAccess, seed: Long, x: Double, y: Double, z: Double, width: Float,
//        yaw: Float, pitch: Float, branch: Int, branchCount: Int, yawPitchRatio: Double,
//    ) {
//        var x = x
//        var y = y
//        var z = z
//        var yaw = yaw
//        var pitch = pitch
//        var branch = branch
//        var branchCount = branchCount
//        val pos = chunk.pos
//        val centerX = (pos.x * 16 + 8).toDouble()
//        val centerZ = (pos.z * 16 + 8).toDouble()
//        var yawVelocity = 0.0f
//        var pitchVelocity = 0.0f
//        val random = Random(seed)
//
//        if (branchCount <= 0) {
//            val maxBranchCount: Int = CARVER_RANGE * 16 - 16
//            branchCount = maxBranchCount - random.nextInt(maxBranchCount / 4)
//        }
//
//        var room = false
//        if (branch == -1) {
//            branch = branchCount / 2
//            room = true
//        }
//
//        val splitBranch = random.nextInt(branchCount / 2) + branchCount / 4
//        val slowPitch = random.nextInt(6) == 0
//
//        while (branch < branchCount) {
//            val horizontalScale = 1.5 + Mth.sin(branch.toFloat() * Mth.PI / branchCount.toFloat()) * width
//            val verticalScale = horizontalScale * yawPitchRatio
//            val pitchCos = Mth.cos(pitch)
//
//            x += (Mth.cos(yaw) * pitchCos).toDouble()
//            y += Mth.sin(pitch).toDouble()
//            z += (Mth.sin(yaw) * pitchCos).toDouble()
//            pitch *= if (slowPitch) 0.92f else 0.7f
//            pitch += pitchVelocity * 0.1f
//            yaw += yawVelocity * 0.1f
//            pitchVelocity *= 0.9f
//            yawVelocity *= 0.75f
//            pitchVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0f
//            yawVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0f
//
//            if (!room && branch == splitBranch && width > 1.0f) {
//                carveLegacyTunnel(
//                    chunk, random.nextLong(), x, y, z, random.nextFloat() * 0.5f + 0.5f,
//                    yaw - Mth.HALF_PI, pitch / 3.0f, branch, branchCount, 1.0
//                )
//                carveLegacyTunnel(
//                    chunk, random.nextLong(), x, y, z, random.nextFloat() * 0.5f + 0.5f,
//                    yaw + Mth.HALF_PI, pitch / 3.0f, branch, branchCount, 1.0
//                )
//                return
//            }
//
//            if (!room && random.nextInt(4) == 0) {
//                ++branch
//                continue
//            }
//
//            val dx = x - centerX
//            val dz = z - centerZ
//            val remaining = (branchCount - branch).toDouble()
//            val maxDistance = (width + 18.0f).toDouble()
//            if (dx * dx + dz * dz - remaining * remaining > maxDistance * maxDistance) {
//                return
//            }
//
//            carveLegacyRegion(chunk, x, y, z, horizontalScale, verticalScale)
//            if (room) {
//                break
//            }
//            ++branch
//        }
//    }
//
//    private fun carveLegacyRavineStart(chunk: ChunkAccess, sourceChunkX: Int, sourceChunkZ: Int, random: Random) {
//        if (random.nextInt(50) != 0) {
//            return
//        }
//
//        val x = (sourceChunkX * 16 + random.nextInt(16)).toDouble()
//        val y = (random.nextInt(random.nextInt(40) + 8) + 20).toDouble()
//        val z = (sourceChunkZ * 16 + random.nextInt(16)).toDouble()
//        val yaw = random.nextFloat() * Mth.PI * 2.0f
//        val pitch = (random.nextFloat() - 0.5f) * 0.25f
//        val width = (random.nextFloat() * 2.0f + random.nextFloat()) * 2.0f
//        carveLegacyRavine(chunk, random.nextLong(), x, y, z, width, yaw, pitch, 0, 0)
//    }
//
//    private fun carveLegacyRavine(
//        chunk: ChunkAccess, seed: Long, x: Double, y: Double, z: Double, width: Float,
//        yaw: Float, pitch: Float, branch: Int, branchCount: Int,
//    ) {
//        var x = x
//        var y = y
//        var z = z
//        var yaw = yaw
//        var pitch = pitch
//        var branch = branch
//        var branchCount = branchCount
//        val pos = chunk.pos
//        val centerX = (pos.x * 16 + 8).toDouble()
//        val centerZ = (pos.z * 16 + 8).toDouble()
//        val random = Random(seed)
//        var yawVelocity = 0.0f
//        var pitchVelocity = 0.0f
//        val verticalFactors = FloatArray(HEIGHT)
//        var factor = 1.0f
//
//        for (i in verticalFactors.indices) {
//            if (i == 0 || random.nextInt(3) == 0) {
//                factor = 1.0f + random.nextFloat() * random.nextFloat()
//            }
//            verticalFactors[i] = factor * factor
//        }
//
//        if (branchCount <= 0) {
//            val maxBranchCount: Int = CARVER_RANGE * 16 - 16
//            branchCount = maxBranchCount - random.nextInt(maxBranchCount / 4)
//        }
//
//        while (branch < branchCount) {
//            val horizontalScale = 1.5 + Mth.sin(branch.toFloat() * Mth.PI / branchCount.toFloat()) * width
//            val verticalScale = horizontalScale * 3.0
//            val pitchCos = Mth.cos(pitch)
//
//            x += (Mth.cos(yaw) * pitchCos).toDouble()
//            y += Mth.sin(pitch).toDouble()
//            z += (Mth.sin(yaw) * pitchCos).toDouble()
//            pitch *= 0.7f
//            pitch += pitchVelocity * 0.05f
//            yaw += yawVelocity * 0.05f
//            pitchVelocity *= 0.8f
//            yawVelocity *= 0.5f
//            pitchVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0f
//            yawVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0f
//
//            if (random.nextInt(4) == 0) {
//                ++branch
//                continue
//            }
//
//            val dx = x - centerX
//            val dz = z - centerZ
//            val remaining = (branchCount - branch).toDouble()
//            val maxDistance = (width + 18.0f).toDouble()
//            if (dx * dx + dz * dz - remaining * remaining > maxDistance * maxDistance) {
//                return
//            }
//
//            carveLegacyRavineRegion(chunk, x, y, z, horizontalScale, verticalScale, verticalFactors)
//            ++branch
//        }
//    }
//
//    private fun carveLegacyRegion(
//        chunk: ChunkAccess,
//        x: Double,
//        y: Double,
//        z: Double,
//        horizontalScale: Double,
//        verticalScale: Double,
//    ) {
//        carveEllipsoid(chunk, x, y, z, horizontalScale, verticalScale, null)
//    }
//
//    private fun carveLegacyRavineRegion(
//        chunk: ChunkAccess, x: Double, y: Double, z: Double, horizontalScale: Double,
//        verticalScale: Double, verticalFactors: FloatArray?,
//    ) {
//        carveEllipsoid(chunk, x, y, z, horizontalScale, verticalScale, verticalFactors)
//    }
//
//    private fun carveEllipsoid(
//        chunk: ChunkAccess, x: Double, y: Double, z: Double, horizontalScale: Double,
//        verticalScale: Double, verticalFactors: FloatArray?,
//    ) {
//        val pos = chunk.pos
//        val chunkStartX = pos.x * 16
//        val chunkStartZ = pos.z * 16
//        var minX = Mth.floor(x - horizontalScale) - chunkStartX - 1
//        var maxX = Mth.floor(x + horizontalScale) - chunkStartX + 1
//        var minY = Mth.floor(y - verticalScale) - 1
//        var maxY = Mth.floor(y + verticalScale) + 1
//        var minZ = Mth.floor(z - horizontalScale) - chunkStartZ - 1
//        var maxZ = Mth.floor(z + horizontalScale) - chunkStartZ + 1
//
//        minX = Mth.clamp(minX, 0, 16)
//        maxX = Mth.clamp(maxX, 0, 16)
//        minY = Mth.clamp(minY, MIN_Y + 1, HEIGHT - 8)
//        maxY = Mth.clamp(maxY, MIN_Y + 1, HEIGHT - 8)
//        minZ = Mth.clamp(minZ, 0, 16)
//        maxZ = Mth.clamp(maxZ, 0, 16)
//
//        if (isLegacyRegionBlockedByFluid(chunk, minX, maxX, minY, maxY, minZ, maxZ)) {
//            return
//        }
//
//        val mutable = BlockPos.MutableBlockPos()
//        for (localX in minX..<maxX) {
//            val scaledX = ((localX + chunkStartX) + 0.5 - x) / horizontalScale
//            for (localZ in minZ..<maxZ) {
//                val scaledZ = ((localZ + chunkStartZ) + 0.5 - z) / horizontalScale
//                if (scaledX * scaledX + scaledZ * scaledZ >= 1.0) {
//                    continue
//                }
//
//                for (localY in maxY downTo minY + 1) {
//                    val scaledY = (localY - 0.5 - y) / verticalScale
//                    var shape = scaledX * scaledX + scaledZ * scaledZ
//                    if (verticalFactors != null && localY >= 0 && localY < verticalFactors.size) {
//                        shape *= verticalFactors[localY].toDouble()
//                    }
//
//                    val verticalShape = if (verticalFactors == null) scaledY * scaledY else scaledY * scaledY / 6.0
//                    if (shape + verticalShape >= 1.0) {
//                        continue
//                    }
//
//                    val worldX = chunkStartX + localX
//                    val worldZ = chunkStartZ + localZ
//                    val state = chunk.getBlockState(mutable.set(worldX, localY, worldZ))
//                    if (isBaseStone(state)) {
//                        chunk.setBlockState(mutable, if (localY <= LAVA_LEVEL) liquidCandy() else AIR, false)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun isLegacyRegionBlockedByFluid(
//        chunk: ChunkAccess,
//        minX: Int,
//        maxX: Int,
//        minY: Int,
//        maxY: Int,
//        minZ: Int,
//        maxZ: Int,
//    ): Boolean {
//        val pos = chunk.pos
//        val mutable = BlockPos.MutableBlockPos()
//        for (localX in minX..<maxX) {
//            val worldX = pos.getBlockX(localX)
//            for (localZ in minZ..<maxZ) {
//                val worldZ = pos.getBlockZ(localZ)
//                var y = maxY + 1
//                while (y >= minY - 1) {
//                    if (y < MIN_Y || y >= HEIGHT) {
//                        --y
//                        continue
//                    }
//                    val state = chunk.getBlockState(mutable.set(worldX, y, worldZ))
//                    if (state.`is`(Blocks.WATER)
//                        || state.`is`(CCBlocks.LIQUID_CHOCOLATE.get())
//                        || state.`is`(CCBlocks.LIQUID_CANDY.get()) && y > LAVA_LEVEL
//                    ) {
//                        return true
//                    }
//                    if (y != minY - 1 && localX != minX && localX != maxX - 1 && localZ != minZ && localZ != maxZ - 1) {
//                        y = minY
//                    }
//                    --y
//                }
//            }
//        }
//        return false
//    }
//
//    private fun findTopSolid(chunk: ChunkAccess, worldX: Int, worldZ: Int): Int {
//        val mutable = BlockPos.MutableBlockPos()
//        for (y in min(chunk.maxBuildHeight - 1, HEIGHT - 1) downTo MIN_Y) {
//            val state = chunk.getBlockState(mutable.set(worldX, y, worldZ))
//            if (isBaseStone(state)) {
//                return y
//            }
//        }
//        return MIN_Y
//    }
//
//    private fun findTopTerrain(chunk: ChunkAccess, worldX: Int, worldZ: Int): Int {
//        val mutable = BlockPos.MutableBlockPos()
//        for (y in min(chunk.maxBuildHeight - 1, HEIGHT - 1) downTo MIN_Y) {
//            val state = chunk.getBlockState(mutable.set(worldX, y, worldZ))
//            if (!state.isAir && state.fluidState.isEmpty) {
//                return y
//            }
//        }
//        return MIN_Y
//    }
//
//    private fun fluidForColumn(worldX: Int, worldZ: Int, randomState: RandomState): BlockState? {
//        if (CHOCOLATE_FOREST != biomeId(worldX, worldZ, randomState)) {
//            return WATER
//        }
//        val key: Long = packColumnPos(worldX, worldZ)
//        var chocolate = pondChocolateCache.get(key)
//        if (chocolate == null) {
//            scanChocolatePond(worldX, worldZ, randomState)
//            chocolate = pondChocolateCache.getOrDefault(key, Boolean.FALSE)
//        }
//        return if (chocolate) CandyLiquidBlock.Kind.LIQUID_CHOCOLATE else WATER
//    }
//
//    /**
//     * Chocolate liquid only appears in water bodies that are fully enclosed by chocolate-forest
//     * surface (chocolate-covered white brownie top blocks). Any connection to foreign-biome water
//     * (ocean/river/other) or a non-forest shore keeps the whole body as plain water. The result is
//     * shared with every visited water column so each connected body is scanned only once.
//     */
//    private fun scanChocolatePond(startX: Int, startZ: Int, randomState: RandomState) {
//        val queue = ArrayDeque<Long?>()
//        val visited: MutableSet<Long?> = HashSet<Long?>()
//        queue.add(packColumnPos(startX, startZ))
//        var enclosed = true
//
//        while (!queue.isEmpty()) {
//            if (visited.size >= MAX_POND_SCAN_COLUMNS) {
//                enclosed = false
//                break
//            }
//            val packed: Long = queue.poll()!!
//            if (!visited.add(packed)) {
//                continue
//            }
//            val x = (packed shr 32).toInt()
//            val z = packed.toInt()
//            if (CHOCOLATE_FOREST != biomeId(x, z, randomState)) {
//                enclosed = false
//                break
//            }
//            if (isOpenWaterColumn(x, z, randomState)) {
//                queue.add(packColumnPos(x + 1, z))
//                queue.add(packColumnPos(x - 1, z))
//                queue.add(packColumnPos(x, z + 1))
//                queue.add(packColumnPos(x, z - 1))
//            }
//        }
//
//        if (pondChocolateCache.size + visited.size > 131072) {
//            pondChocolateCache.clear()
//        }
//        for (packed in visited) {
//            pondChocolateCache.put(packed, enclosed)
//        }
//    }
//
//    /** Open water means no solid terrain at the two topmost sea-level layers of this column.  */
//    private fun isOpenWaterColumn(x: Int, z: Int, randomState: RandomState): Boolean {
//        val key: Long = packColumnPos(x, z)
//        val cached = openWaterColumnCache.get(key)
//        if (cached != null) {
//            return cached
//        }
//        val noise: LegacyTerrainNoise = terrainNoise(randomState)
//        val noiseX = x / CELL_WIDTH.toDouble()
//        val noiseZ = z / CELL_WIDTH.toDouble()
//        val heightConfig = heightConfigAt(Mth.floor(noiseX).toDouble(), Mth.floor(noiseZ).toDouble(), randomState)
//        val depthNoise: Double = sampleDepthNoise(noise, noiseX, noiseZ)
//        val water = sampleMajorReleaseDensity(
//            noise,
//            noiseX,
//            (SEA_LEVEL - 1) / CELL_HEIGHT.toDouble(),
//            noiseZ,
//            heightConfig,
//            depthNoise
//        ) <= 0.0
//                && sampleMajorReleaseDensity(
//            noise,
//            noiseX,
//            SEA_LEVEL / CELL_HEIGHT.toDouble(),
//            noiseZ,
//            heightConfig,
//            depthNoise
//        ) <= 0.0
//        if (openWaterColumnCache.size >= 131072) {
//            openWaterColumnCache.clear()
//        }
//        openWaterColumnCache.putIfAbsent(key, water)
//        return water
//    }
//
//    private fun biomeShape(x: Int, z: Int, randomState: RandomState): BiomeShape {
//        val key = (x.toLong() shl 32) xor (z.toLong() and 0xFFFFFFFFL)
//        val cached = biomeShapeCache.get(key)
//        if (cached != null) {
//            return cached
//        }
//        val location = biomeId(x, z, randomState)
//        val result = when (location.path) {
//            "ice_cream_sky_mountains" -> BiomeShape(3.55f, 2.9f)
//            "sugar_mountains" -> BiomeShape(0.5f, 0.8f)
//            "sugar_hell_mountains" -> BiomeShape(1.9f, 2.0f)
//            "sugar_plains" -> BiomeShape(0.05f, 0.1f)
//            "sugar_forest" -> BiomeShape(0.1f, 0.15f)
//            "chocolate_forest" -> BiomeShape(0.1f, 0.15f)
//            "sugar_cold_forest" -> BiomeShape(0.1f, 0.3f)
//            "sugar_river" -> BiomeShape(-0.5f, 0.0f)
//            "sugar_oceans" -> BiomeShape(-1.0f, 0.1f)
//            "sugar_enchanted_forest" -> BiomeShape(0.23f, 0.25f)
//            "caramel_forest" -> BiomeShape(0.05f, 0.1f)
//            "cotton_candy_plains" -> BiomeShape(0.05f, 0.1f)
//            "gummy_swamp" -> BiomeShape(-0.1f, 0.1f)
//            "ice_cream_plains" -> BiomeShape(0.05f, 0.1f)
//            else -> BiomeShape(0.05f, 0.1f)
//        }
//        if (biomeShapeCache.size >= 131072) {
//            biomeShapeCache.clear()
//        }
//        val previous = biomeShapeCache.putIfAbsent(key, result)
//        return if (previous != null) previous else result
//    }
//
//    private fun biomeId(x: Int, z: Int, randomState: RandomState): ResourceLocation {
//        val biome = getBiomeSource().getNoiseBiome(Mth.floorDiv(x, 4), 16, Mth.floorDiv(z, 4), randomState.sampler())
//        val id = biome.unwrapKey().map<ResourceLocation?>(Function { key: ResourceKey<Biome?>? -> key!!.location() })
//        return id.orElse(PLAINS)
//    }
//
//    @JvmRecord
//    private data class BiomeShape(val baseHeight: Float, val variation: Float)
//
//    @JvmRecord
//    private data class HeightConfig(val depth: Double, val scale: Double)
//
//    @JvmRecord
//    private data class SurfaceMaterials(val top: BlockState?, val under: BlockState, val underwater: BlockState?)
//
//    private class LegacyTerrainNoise(seed: Long) {
//        private val minLimit: LegacyPerlinOctaveNoise
//        private val maxLimit: LegacyPerlinOctaveNoise
//        private val main: LegacyPerlinOctaveNoise
//        private val depth: LegacyPerlinOctaveNoise
//
//        init {
//            val random = Random(seed)
//            this.minLimit = LegacyPerlinOctaveNoise(random, 16, true)
//            this.maxLimit = LegacyPerlinOctaveNoise(random, 16, true)
//            this.main = LegacyPerlinOctaveNoise(random, 8, true)
//            LegacyPerlinOctaveNoise(random, 4, false)
//            LegacyPerlinOctaveNoise(random, 10, true)
//            this.depth = LegacyPerlinOctaveNoise(random, 16, true)
//            LegacyPerlinOctaveNoise(random, 8, true)
//        }
//    }
//
//
//}
//
