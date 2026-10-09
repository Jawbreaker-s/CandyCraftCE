package cn.jawbreakers.candycraftce.forge

import cn.jawbreakers.candycraftce.client.hud.CandyBossBarHud
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.whenInitialized
import cn.jawbreakers.candycraftce.utils.ICPlatFormClients
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.client.Minecraft
import net.minecraft.client.color.block.BlockColor
import net.minecraft.client.color.item.ItemColor
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.particle.ParticleEngine
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.client.event.RegisterColorHandlersEvent
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent
import net.minecraftforge.client.event.RegisterParticleProvidersEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import java.util.function.Supplier

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
            addListener(::onRegisterEntityRenderers)
            addListener(::onRegisterLayerDefinitions)
            addListener(::onRegisterGuiOverlays)
        }
        with(FORGE_BUS) {
            addListener(::onClientTick)
        }
    }


    fun onRegisterGuiOverlays(event: RegisterGuiOverlaysEvent) {
        event.registerAboveAll("candy_bossbar_hud") { gui, graphics, partialTick, screenWidth, screenHeight ->
            hudBossBar.render(graphics, partialTick)
        }
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) {
            hudBossBar.tick()
        }
    }


    override val hudBossBar by lazy { CandyBossBarHud(Minecraft.getInstance()) }

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

    val entityRenderers: MutableMap<Entry<EntityType<Entity>>, EntityRendererProvider<Entity>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    override fun <E : Entity, SE : E> registerEntityRenderer(
        entry: Entry<EntityType<SE>>,
        renderer: EntityRendererProvider<E>,
    ) {
        entityRenderers[entry as Entry<EntityType<Entity>>] = renderer as EntityRendererProvider<Entity>
    }

    fun onRegisterEntityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        entityRenderers.forEach { (type, provider) ->
            event.registerEntityRenderer(type.get(), provider)
        }
    }

    private val layers: MutableMap<ModelLayerLocation, Supplier<LayerDefinition>> = mutableMapOf()

    fun onRegisterLayerDefinitions(event: EntityRenderersEvent.RegisterLayerDefinitions) {
        layers.forEach(event::registerLayerDefinition)
    }

    override fun registerRenderLayers(layer: ModelLayerLocation, provider: Supplier<LayerDefinition>) {
        layers[layer] = provider
    }


}
