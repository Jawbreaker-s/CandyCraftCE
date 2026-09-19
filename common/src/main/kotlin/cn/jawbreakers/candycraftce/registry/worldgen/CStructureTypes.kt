package cn.jawbreakers.candycraftce.registry.worldgen

import cn.jawbreakers.candycraftce.level.structure.FloatingIslandStructure
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureType

/**
 * Created in 2026/9/19 23:11
 * Project: CandyCraftCE
 * Author: [Bread_NiceCat](https://github.com/Bread-NiceCat)
 */
object CStructureTypes {
    init {
        CLogUtils.sign()
    }

    val floating_island_type = register("floating_island") { FloatingIslandStructure.codec }

    private fun <S : Structure> register(name: String, structure: StructureType<S>): Entry<StructureType<S>> {
        return CPlatformUtils.levels.registerStructureType(name, structure)
    }
}