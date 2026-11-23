package cn.jawbreakers.candycraftce.level.noise

import net.minecraft.util.Mth
import net.minecraft.util.RandomSource

class LegacyPerlinNoise(random: RandomSource, useOffset: Boolean) {
    private val permutations = IntArray(512)
    private val offsetX: Double = if (useOffset) random.nextDouble() * 256.0 else 0.0
    private val offsetY: Double = if (useOffset) random.nextDouble() * 256.0 else 0.0
    private val offsetZ: Double = if (useOffset) random.nextDouble() * 256.0 else 0.0

    init {
        for (i in 0..255) {
            permutations[i] = i
        }

        for (i in 0..255) {
            val j = random.nextInt(256 - i) + i
            val value = permutations[i]
            permutations[i] = permutations[j]
            permutations[j] = value
            permutations[i + 256] = permutations[i]
        }
    }

    fun sampleXZ(x: Double, z: Double, frequency: Double): Double {
        var x = x
        var z = z
        var frequency = frequency
        frequency = 1.0 / frequency
        x += offsetX
        z += offsetZ

        val floorX = Mth.floor(x)
        val floorZ = Mth.floor(z)
        val gridX = floorX and 255
        val gridZ = floorZ and 255

        x -= floorX.toDouble()
        z -= floorZ.toDouble()

        val u: Double = fade(x)
        val w: Double = fade(z)
        val a = permutations[gridX]
        val aa = permutations[a] + gridZ
        val b = permutations[gridX + 1]
        val ba = permutations[b] + gridZ

        val front: Double = lerp(u, grad(permutations[aa], x, 0.0, z), grad(permutations[ba], x - 1.0, 0.0, z))
        val back: Double =
            lerp(u, grad(permutations[aa + 1], x, 0.0, z - 1.0), grad(permutations[ba + 1], x - 1.0, 0.0, z - 1.0))
        return lerp(w, front, back) * frequency
    }

    fun sampleXYZ(x: Double, y: Double, z: Double, yScale: Double, yMax: Double): Double {
        var x = x
        var y = y
        var z = z
        x += offsetX
        y += offsetY
        z += offsetZ

        val floorX = Mth.floor(x)
        val floorY = Mth.floor(y)
        val floorZ = Mth.floor(z)

        x -= floorX.toDouble()
        y -= floorY.toDouble()
        z -= floorZ.toDouble()

        val yOffset: Double
        if (yScale != 0.0) {
            val clippedY = if (yMax in 0.0..<y) yMax else y
            yOffset = Mth.floor(clippedY / yScale + 1.0000000116860974E-7) * yScale
        } else {
            yOffset = 0.0
        }

        return sampleXYZ(floorX, floorY, floorZ, x, y - yOffset, z, y)
    }

    private fun sampleXYZ(
        floorX: Int,
        floorY: Int,
        floorZ: Int,
        localX: Double,
        localOffsetY: Double,
        localZ: Double,
        localY: Double,
    ): Double {
        val gridX = floorX and 255
        val gridY = floorY and 255
        val gridZ = floorZ and 255

        val a = permutations[gridX] + gridY
        val aa = permutations[a] + gridZ
        val ab = permutations[a + 1] + gridZ
        val b = permutations[gridX + 1] + gridY
        val ba = permutations[b] + gridZ
        val bb = permutations[b + 1] + gridZ

        val u: Double = fade(localX)
        val v: Double = fade(localY)
        val w: Double = fade(localZ)

        return Mth.lerp3(
            u,
            v,
            w,
            grad(permutations[aa], localX, localOffsetY, localZ),
            grad(permutations[ba], localX - 1.0, localOffsetY, localZ),
            grad(permutations[ab], localX, localOffsetY - 1.0, localZ),
            grad(permutations[bb], localX - 1.0, localOffsetY - 1.0, localZ),
            grad(permutations[aa + 1], localX, localOffsetY, localZ - 1.0),
            grad(permutations[ba + 1], localX - 1.0, localOffsetY, localZ - 1.0),
            grad(permutations[ab + 1], localX, localOffsetY - 1.0, localZ - 1.0),
            grad(permutations[bb + 1], localX - 1.0, localOffsetY - 1.0, localZ - 1.0)
        )
    }

    companion object {
        private fun lerp(delta: Double, start: Double, end: Double): Double {
            return start + delta * (end - start)
        }

        private fun fade(value: Double): Double {
            return value * value * value * (value * (value * 6.0 - 15.0) + 10.0)
        }

        private fun grad(hash: Int, x: Double, y: Double, z: Double): Double {
            return when (hash and 15) {
                0 -> x + y
                1 -> -x + y
                2 -> x - y
                3 -> -x - y
                4 -> x + z
                5 -> -x + z
                6 -> x - z
                7 -> -x - z
                8 -> y + z
                9, 13 -> -y + z
                10 -> y - z
                11 -> -y - z
                12 -> y + x
                14 -> y - x
                else -> -y - z
            }
        }
    }
}
