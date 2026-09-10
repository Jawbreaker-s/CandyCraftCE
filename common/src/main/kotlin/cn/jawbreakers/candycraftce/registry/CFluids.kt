package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.fluid.CFluidProperties
import cn.jawbreakers.candycraftce.fluid.CFluidType
import cn.jawbreakers.candycraftce.fluid.FogRenderType
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.fluids
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.LinearGradient.Companion.vecColor
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.world.level.material.FlowingFluid

object CFluids {
    init {
        CLogUtils.sign()
    }

    val grenadine_properties: CFluidProperties = CFluidProperties(
        type = CFluidType(
            "fluid.$MOD_ID.grenadine",
            stillTexture = "block/grenadine_static".modLoc(),
            flowingTexture = "block/grenadine_flow".modLoc(),
            tintColor = 0xB0FFFFFF.toInt(),
            renderOverlayTexture = "textures/misc/grenadine_underwater.png".modLoc(),
            isTransparent = true,
            fogColor = 0xF22929.vecColor,
            fogRenderType = ifClient { FogRenderType.water(48.0F) },
        ),
        block = { CBlocks.grenadine },
        source = { source_grenadine },
        flowing = { flowing_grenadine },
//        CCItems.GRENADINE_BUCKET
//				.block(CCBlocks.GRENADINE)
    )
    val source_grenadine = register("grenadine", grenadine_properties) {
        fluids.createSource(grenadine_properties)
    }
    val flowing_grenadine = register("flowing_grenadine", grenadine_properties) {
        fluids.createFlowing(grenadine_properties)
    }

    val caramel_properties: CFluidProperties = CFluidProperties(
        type = CFluidType(
            "fluid.$MOD_ID.caramel",
            stillTexture = "block/caramel_static".modLoc(),
            flowingTexture = "block/caramel_static".modLoc(),
            tintColor = 0xB0FFFFFF.toInt(),
            overlayTexture = "textures/misc/caramel_underwater.png".modLoc(),
            fogColor = 0x914000.vecColor,
            fogRenderType = ifClient { FogRenderType.water(48.0F) },
        ),
        source = { source_caramel },
        flowing = { flowing_caramel },
        block = { CBlocks.caramel },
    )
    val source_caramel = register("caramel", caramel_properties) {
        fluids.createSource(caramel_properties)
    }
    val flowing_caramel = register("flowing_caramel", caramel_properties) {
        fluids.createFlowing(caramel_properties)
    }

    val liquid_chocolate_properties: CFluidProperties = CFluidProperties(
        type = CFluidType(
            "fluid.$MOD_ID.liquid_chocolate",
            density = 1030,
            temperature = 315,
            stillTexture = "block/liquid_chocolate_still".modLoc(),
            flowingTexture = "block/liquid_chocolate_flow".modLoc(),
            tintColor = 0xB0FFFFFF.toInt(),
            fogColor = 0x482B17.vecColor,
            fogRenderType = ifClient { FogRenderType.lava },
        ),
        source = { source_liquid_chocolate },
        flowing = { flowing_liquid_chocolate },
        block = { CBlocks.liquid_chocolate },
    )
    val source_liquid_chocolate = register("liquid_chocolate", liquid_chocolate_properties) {
        fluids.createSource(liquid_chocolate_properties)
    }
    val flowing_liquid_chocolate =
        register("flowing_liquid_chocolate", liquid_chocolate_properties) {
            fluids.createFlowing(liquid_chocolate_properties)
        }
    val liquid_candy_properties: CFluidProperties = CFluidProperties(
        type = CFluidType(
            "fluid.$MOD_ID.liquid_candy",
            density = 2000,
            viscosity = 3000,
            temperature = 1000,
            lightLevel = 12,
            stillTexture = "block/liquid_candy_still".modLoc(),
            flowingTexture = "block/liquid_candy_flow".modLoc(),
            tintColor = 0xB0FFFFFF.toInt(),
            fogColor = 0xE674CA.vecColor,
            fogRenderType = ifClient { FogRenderType.lava },
        ),
        source = { source_liquid_candy },
        flowing = { flowing_liquid_candy },
        block = { CBlocks.liquid_candy },
    )
    val source_liquid_candy = register("liquid_candy", liquid_candy_properties) {
        fluids.createSource(liquid_candy_properties)
    }
    val flowing_liquid_candy = register("flowing_liquid_candy", liquid_candy_properties) {
        fluids.createFlowing(liquid_candy_properties)
    }

    private fun <E : FlowingFluid> register(name: String, properties: CFluidProperties, factory: () -> E): Entry<E> =
        CPlatformUtils.registerFluids(name, properties, factory)
}