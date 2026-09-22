package cn.jawbreakers.candycraftce.fabric

import cn.jawbreakers.candycraftce.utils.CPlatformUtils.whenInitialized
import cn.jawbreakers.candycraftce.utils.ICPlatFormClients
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.client.color.block.BlockColor
import net.minecraft.client.color.item.ItemColor
import net.minecraft.client.particle.ParticleEngine
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

/**
 * Created in 2026/9/20 23:23 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@Environment(EnvType.CLIENT)
object CFabricClients : ICPlatFormClients {
    override fun setRenderLayer(block: Entry<out Block>, layer: RenderType) {
        whenInitialized {
            BlockRenderLayerMap.INSTANCE.putBlock(block.get(), layer)
        }
    }

    override fun registerBlockColor(vararg blocks: Entry<out Block>, color: BlockColor) {
        whenInitialized {
            ColorProviderRegistry.BLOCK.register(color, *blocks.map { it.get() }.toTypedArray())
        }
    }

    override fun registerItemColor(vararg items: Entry<out Item>, color: ItemColor) {
        whenInitialized {
            ColorProviderRegistry.ITEM.register(color, *items.map { it.get() }.toTypedArray())
        }
    }

    override fun <T : ParticleOptions> registerParticleFactory(
        type: Entry<out ParticleType<T>>,
        factory: ParticleEngine.SpriteParticleRegistration<T>,
    ) {
        whenInitialized {
            ParticleFactoryRegistry.getInstance().register(type.get(), factory::create)
        }
    }
}