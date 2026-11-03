package cn.jawbreakers.candycraftce.level

import net.minecraft.util.Mth

const val MIN_Y = -64
const val HEIGHT = 384
const val MAX_Y = HEIGHT + MIN_Y // 320，世界顶端的绝对 Y（不含）
const val SEA_LEVEL = 63
const val LAVA_LEVEL = 10

/** 石头/深板岩分界线：绝对 Y 低于此值的实心石头使用 deepslate_stone。  */
const val DEEPSLATE_BOUNDARY = 0

/** 洞穴雕刻时检查的周围区块范围（±8 区块）。  */
const val CARVER_RANGE = 8

/** 噪声采样网格尺寸（XZ 方向 5 个采样点）。  */
const val NOISE_SIZE_XZ = 5

/** 噪声采样网格尺寸（Y 方向 49 个采样点 = HEIGHT / CELL_HEIGHT + 1）。  */
const val NOISE_SIZE_Y = 49

/** 插值单元格宽度（XZ 方向，4 块）。  */
const val CELL_WIDTH = 4

/** 插值单元格高度（Y 方向，8 块）。  */
const val CELL_HEIGHT = 8

/** Y 方向单元格总数（= HEIGHT / CELL_HEIGHT，即 48）。  */
const val CELLS_Y = HEIGHT / CELL_HEIGHT

/**
 * 密度采样 Y 坐标偏移：地形密度以「绝对世界 Y / CELL_HEIGHT」作为噪声 Y，
 * 而网格索引 gy 对应的世界高度为 MIN_Y + gy*CELL_HEIGHT，故噪声 Y = gy + MIN_Y/CELL_HEIGHT。
 * 缺少这个偏移会让整块地形相对海平面错位（旧版世界以 0 为底时无此问题）。
 */
const val NOISE_Y_OFFSET = MIN_Y / CELL_HEIGHT // -8

/** 顶部衰减起始噪声 Y（基于绝对世界高度）：接近世界顶端 3 格起把密度压向 -10，避免地形顶到建筑上限。  */
const val TOP_FADE_Y = (MAX_Y / CELL_HEIGHT - 3).toDouble()

// 噪声缩放相关常量（与旧版 1.12 地形生成参数保持一致）
const val COORDINATE_SCALE = 684.412
const val HEIGHT_SCALE = 684.412
const val MAIN_NOISE_SCALE_XZ = 80.0
const val MAIN_NOISE_SCALE_Y = 160.0
const val LOWER_LIMIT_SCALE = 512.0
const val UPPER_LIMIT_SCALE = 512.0
const val DEPTH_NOISE_SCALE_XZ = 200.0

/** 生物群系深度权重。  */
const val BIOME_DEPTH_WEIGHT = 1.0

/** 生物群系缩放权重。  */
const val BIOME_SCALE_WEIGHT = 1.0
const val BIOME_DEPTH_OFFSET = 0.0
const val BIOME_SCALE_OFFSET = 0.0

/** 基础地形尺寸。  */
const val BASE_SIZE = 8.5

/** Y 轴拉伸系数。  */
const val STRETCH_Y = 12.0

/** 抛物线权重场，用于生物群系地形过渡时的平滑插值。  */
val PARABOLIC_FIELD = FloatArray(25).also {
    for (x in -2..2) {
        for (z in -2..2) {
            it[x + 2 + (z + 2) * 5] = 10.0f / Mth.sqrt(x * x + z * z + 0.2f)
        }
    }
}
val PARABOLIC_FIELD_TOTAL = PARABOLIC_FIELD.sum().toDouble()
fun octaveNoise2D(x: Double, z: Double, octaves: Int, salt: Long): Double {
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


fun smoothNoise2D(x: Double, z: Double, salt: Long): Double {
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

fun fade(value: Double): Double {
    return value * value * value * (value * (value * 6.0 - 15.0) + 10.0)
}

fun randomUnit(x: Int, y: Int, z: Int, salt: Long): Double {
    val bits: Long = hash(x, y, z, salt)
    return (bits ushr 11) * 1.1102230246251565E-16 * 2.0 - 1.0
}

fun hash(x: Int, y: Int, z: Int): Int {
    return hash(x, y, z, -0x340d631b7bdddcdbL).toInt()
}

fun positiveHash(x: Int, y: Int, z: Int, salt: Long): Long {
    return hash(x, y, z, salt) and Long.MAX_VALUE
}

fun hash(x: Int, y: Int, z: Int, salt: Long): Long {
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
