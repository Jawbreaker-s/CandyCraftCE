package cn.jawbreakers.candycraftce.registry.worldgen

import cn.jawbreakers.candycraftce.level.structure.FloatingIslandStructure.FloatingIslandPiece
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType

/**
 * Created in 2026/9/19 22:50
 * Project: CandyCraftCE
 * Author: [Bread_NiceCat](https://github.com/Bread-NiceCat)
 */

object CStructurePieceTypes {
    init {
        CLogUtils.sign()
    }

    val floating_island_piece = contextless("FIP", ::FloatingIslandPiece)

    private fun register(key: String, type: StructurePieceType): Entry<StructurePieceType> {
        return CPlatformUtils.levels.registerStructurePieceType(key.lowercase(), type)
    }

    private fun contextless(key: String, type: StructurePieceType.ContextlessType) = register(key, type)

}