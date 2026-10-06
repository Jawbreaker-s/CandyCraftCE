package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.fluid.CFluidPresets
import cn.jawbreakers.candycraftce.fluid.CFluidReferences
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.fluids
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import net.minecraft.world.level.material.MapColor

object CFluids {
    init {
        CLogUtils.sign()
    }

    val grenadine: CFluidReferences = fluids.registerGrenadine(
        CFluidPresets(
            "grenadine",
            stillTexture = "block/grenadine_static".modLoc(),
            flowingTexture = "block/grenadine_flow".modLoc(),
            underwaterTexture = "textures/misc/grenadine_underwater.png".modLoc(),
            fogColor = 0xF22929,
            isTransparent = true,
            bucket = { CItems.grenadine_bucket },
            mapColor = MapColor.COLOR_RED,
            tickRate = 5
        )
    )

    val caramel: CFluidReferences = fluids.registerCaramel(
        CFluidPresets(
            "caramel",
            stillTexture = "block/caramel_static".modLoc(),
            flowingTexture = "block/caramel_static".modLoc(),
            underwaterTexture = "textures/misc/caramel_underwater.png".modLoc(),
            fogColor = 0x914000,
            bucket = { CItems.caramel_bucket },
            mapColor = MapColor.COLOR_ORANGE,
            tickRate = 10
        )
    )

}