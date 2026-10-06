package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.fluid.CFluidPresets
import cn.jawbreakers.candycraftce.fluid.CFluidReferences
import cn.jawbreakers.candycraftce.registry.CBlocks.asItemEntry
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.translate
import cn.jawbreakers.candycraftce.utils.CUtils.modLoc
import cn.jawbreakers.candycraftce.utils.registry.Accessor
import cn.jawbreakers.candycraftce.utils.registry.Entry
import com.mojang.datafixers.types.Type
import com.mojang.serialization.Codec
import net.minecraft.Util
import net.minecraft.client.color.block.BlockColor
import net.minecraft.client.color.item.ItemColor
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.particle.ParticleEngine
import net.minecraft.client.renderer.DimensionSpecialEffects
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.SpawnEggItem
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType
import java.util.function.Consumer
import java.util.function.Supplier

object CPlatformUtils : ICPlatForm by CandyCraftCE.platform {
    @ServerSafe
    inline fun <R> ifClient(action: ICPlatFormClients. () -> R): R? {
        return if (isClient) clients?.let(action) else null
    }

    inline fun <R> ifDatagen(action: ICPlatformDatagen .() -> R): R? {
        return datagen?.let(action)
    }

    inline fun <R> ifDev(action: () -> R): R? = if (isDev) action() else null

    fun ICPlatFormClients.registerBlockColor(vararg blocks: Entry<out Block>, color: Int) {
        registerBlockColor(*blocks) { _, _, _, _ -> color }
    }

    fun ICPlatFormClients.registerItemColor(vararg items: Entry<out Item>, color: Int) {
        registerItemColor(*items) { _, _ -> color }
    }

    fun ICPlatFormClients.registerBlockAndItemColor(vararg entries: Entry<out Block>, color: Int) {
        registerBlockColor(*entries, color = color)
        registerItemColor(*entries.map { it.asItemEntry() }.toTypedArray(), color = color)
    }

    fun String.translate(en: String, zh: String): String = apply {
        datagen?.onGatherLanguage {
            add(this@translate, en, zh)
        }
    }

    fun <T : Component> T.translate(en: String, zh: String): T = apply {
        datagen?.onGatherLanguage {
            add(this@translate, en, zh)
        }
    }

}

interface ICPlatForm {
    companion object {
        val SPAWN_EGG_DESC: String = Util.makeDescriptionId("item", "spawn_egg".modLoc())
            .translate("%s Spawn Egg", "%s刷怪蛋")
    }

    val isDev: Boolean
    val isClient: Boolean
    val fluids: ICPlatformFluids
    val levels: ICPlatformLevels

    val datagen: ICPlatformDatagen?
    val clients: ICPlatFormClients?

    //当所有对象注册完毕后
    fun <T> whenInitialized(action: () -> T): Accessor<T>

    fun <E : Item> registerItem(name: String, factory: Supplier<E>): Entry<E>
    fun <E : Block> registerBlock(name: String, factory: Supplier<E>): Entry<E>
    fun <E : BlockEntity> registerBlockEntity(
        key: String,
        builder: Supplier<BlockEntityType.Builder<E>>,
        dsl: Type<*>?,
    ): Entry<BlockEntityType<E>>

    fun <E : Entity> registerEntityType(key: String, type: Supplier<EntityType<E>>): Entry<EntityType<E>>
    fun <LE : LivingEntity> registerEntityAttribute(
        entity: Entry<EntityType<LE>>,
        builder: Supplier<AttributeSupplier.Builder>,
    )

    fun <P : ParticleType<*>> registerParticleType(name: String, factory: Supplier<P>): Entry<P>

    fun registerCreativeTab(name: String, builder: Consumer<CreativeModeTab.Builder>): Entry<CreativeModeTab>
    fun <T : Mob> setEntityPlacement(
        entry: Entry<EntityType<T>>,
        spawnType: SpawnPlacements.Type,
        mapType: Heightmap.Types,
        predicate: SpawnPlacements.SpawnPredicate<T>,
    )

    fun <M : MobEffect> registerMobEffect(name: String, factory: Supplier<M>): Entry<M>
    fun createSpawnEggItem(
        type: Entry<EntityType<out Mob>>,
        backgroundColor: Int,
        highlightColor: Int,
        properties: Item.Properties,
    ): SpawnEggItem
}

interface ICPlatformFluids {
    companion object {
        const val SOURCE_SUFFIX = "_source"
        const val FLOWING_SUFFIX = "_flowing"
    }

    fun createBucketItem(ref: CFluidReferences, properties: Item.Properties): BucketItem
    fun registerGrenadine(presets: CFluidPresets): CFluidReferences
    fun registerCaramel(presets: CFluidPresets): CFluidReferences

}

interface ICPlatformLevels {
    fun registerDimensionSpecialEffects(id: ResourceLocation, effects: DimensionSpecialEffects)
    fun <T : Codec<out ChunkGenerator>> registerChunkGenerator(name: String, codec: Supplier<T>): Entry<T>
    fun <T : StructureType<*>> registerStructureType(name: String, type: T): Entry<T>
    fun <F : Feature<*>> registerFeature(name: String, factory: Supplier<F>): Entry<F>
    fun <P : FoliagePlacer> registerFoliagePlacer(name: String, codec: Supplier<Codec<P>>): Entry<FoliagePlacerType<P>>
    fun <T : StructurePieceType> registerStructurePieceType(key: String, type: T): Entry<T>
}

interface ICPlatformDatagen {
    fun onBootstrap(action: RegistrySetBuilder.() -> Unit)
    fun onGatherLanguage(action: ILanguageProvider.() -> Unit)
}

@ClientOnly
interface ICPlatFormClients {
    fun setRenderLayer(block: Entry<out Block>, layer: RenderType)
    fun registerBlockColor(vararg blocks: Entry<out Block>, color: BlockColor)
    fun registerItemColor(vararg items: Entry<out Item>, color: ItemColor)
    fun <T : ParticleOptions> registerParticleFactory(
        type: Entry<out ParticleType<T>>,
        factory: ParticleEngine.SpriteParticleRegistration<T>,
    )

    fun registerRenderLayers(layer: ModelLayerLocation, provider: Supplier<LayerDefinition>)
    fun <E : Entity, SE : E> registerEntityRenderer(entry: Entry<EntityType<SE>>, renderer: EntityRendererProvider<E>)
}

interface ILanguageProvider {
    fun add(key: String, en: String, zh: String)
    fun add(key: Component, en: String, zh: String)
}
