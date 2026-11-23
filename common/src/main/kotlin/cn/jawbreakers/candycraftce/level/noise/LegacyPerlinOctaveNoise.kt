package cn.jawbreakers.candycraftce.level.noise

import net.minecraft.util.Mth
import net.minecraft.util.RandomSource

class LegacyPerlinOctaveNoise(random: RandomSource, octaveCount: Int, useOffset: Boolean) {
    private val octaves = Array(octaveCount) {
        LegacyPerlinNoise(random, useOffset)
    }

    fun sampleXZWrapped(x: Double, z: Double, scaleX: Double, scaleZ: Double): Double {
        var total = 0.0
        var frequency = 1.0

        for (octave in octaves) {
            var offsetX = x * frequency * scaleX
            var offsetZ = z * frequency * scaleZ
            var offsetXCoord = Mth.lfloor(offsetX)
            var offsetZCoord = Mth.lfloor(offsetZ)
            offsetX -= offsetXCoord.toDouble()
            offsetZ -= offsetZCoord.toDouble()
            offsetXCoord %= 16777216L
            offsetZCoord %= 16777216L
            offsetX += offsetXCoord.toDouble()
            offsetZ += offsetZCoord.toDouble()

            total += octave.sampleXZ(offsetX, offsetZ, frequency)
            frequency /= 2.0
        }

        return total
    }

    fun sampleWrapped(x: Double, y: Double, z: Double, scaleX: Double, scaleY: Double, scaleZ: Double): Double {
        var total = 0.0
        var frequency = 1.0

        for (octave in octaves) {
            var offsetX = x * frequency * scaleX
            var offsetZ = z * frequency * scaleZ
            var offsetXCoord = Mth.lfloor(offsetX)
            var offsetZCoord = Mth.lfloor(offsetZ)
            offsetX -= offsetXCoord.toDouble()
            offsetZ -= offsetZCoord.toDouble()
            offsetXCoord %= 16777216L
            offsetZCoord %= 16777216L
            offsetX += offsetXCoord.toDouble()
            offsetZ += offsetZCoord.toDouble()

            val scaledY = y * scaleY * frequency
            total += octave.sampleXYZ(offsetX, scaledY, offsetZ, scaleY * frequency, scaledY) / frequency
            frequency /= 2.0
        }

        return total
    }
}
