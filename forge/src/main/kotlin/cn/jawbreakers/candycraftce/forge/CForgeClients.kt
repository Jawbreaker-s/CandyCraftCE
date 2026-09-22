package cn.jawbreakers.candycraftce.forge

import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.whenInitialized
import cn.jawbreakers.candycraftce.utils.ICPlatFormClients
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.client.color.block.BlockColor
import net.minecraft.client.color.item.ItemColor
import net.minecraft.client.particle.ParticleEngine
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.event.RegisterColorHandlersEvent
import net.minecraftforge.client.event.RegisterParticleProvidersEvent
import thedarkcolour.kotlinforforge.forge.MOD_BUS

/**
 * Created in 2026/9/20 23:14 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
@OnlyIn(Dist.CLIENT)
object CForgeClients : ICPlatFormClients {
    init {
        with(MOD_BUS) {
            addListener(::onRegisterBlockColorHandlers)
            addListener(::onRegisterItemColorHandlers)
            addListener(::onRegisterParticleProviders)
        }
    }

    //=================================
    override fun setRenderLayer(block: Entry<out Block>, layer: RenderType) {
        whenInitialized {
            @Suppress("DEPRECATION")
            ItemBlockRenderTypes.setRenderLayer(block.get(), layer)
        }
    }

    //=================================
    private val blockColors: MutableMap<BlockColor, List<Entry<out Block>>> = mutableMapOf()
    private val itemColors: MutableMap<ItemColor, List<Entry<out Item>>> = mutableMapOf()

    fun onRegisterBlockColorHandlers(event: RegisterColorHandlersEvent.Block) {
        clog.info("onRegisterBlockColorHandlers")
        blockColors.forEach { (color, blocks) ->
            event.register(color, *blocks.map { it.get() }.toTypedArray())
        }
    }

    fun onRegisterItemColorHandlers(event: RegisterColorHandlersEvent.Item) {
        clog.info("onRegisterItemColorHandlers")
        itemColors.forEach { (color, items) ->
            event.register(color, *items.map { it.get() }.toTypedArray())
        }
    }

    override fun registerBlockColor(vararg blocks: Entry<out Block>, color: BlockColor) {
        blockColors[color] = blocks.toList()
    }

    override fun registerItemColor(vararg items: Entry<out Item>, color: ItemColor) {
        itemColors[color] = items.toList()
    }

    private val particles: MutableMap<ParticleType<*>, ParticleEngine.SpriteParticleRegistration<*>> = mutableMapOf()
    override fun <T : ParticleOptions> registerParticleFactory(
        type: Entry<out ParticleType<T>>,
        factory: ParticleEngine.SpriteParticleRegistration<T>,
    ) {
        whenInitialized {
            particles[type.get()] = factory
        }
    }

    fun onRegisterParticleProviders(event: RegisterParticleProvidersEvent) {
        particles.forEach { (particle, factory) ->
            @Suppress("UNCHECKED_CAST")
            event.registerSpecial(
                particle as ParticleType<ParticleOptions>,
                factory as ParticleProvider<ParticleOptions>,
            )
        }
    }


}
