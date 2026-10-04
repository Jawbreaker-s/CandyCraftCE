package cn.jawbreakers.candycraftce.forge

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.forge.ForgeEntry.Companion.asEntry
import cn.jawbreakers.candycraftce.forge.data.CandyCraftCEData
import cn.jawbreakers.candycraftce.forge.fluid.CForgeFluids
import cn.jawbreakers.candycraftce.forge.levels.CForgeLevels
import cn.jawbreakers.candycraftce.utils.*
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CLogUtils.logRegister
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.registry.Accessor
import cn.jawbreakers.candycraftce.utils.registry.Entry
import cn.jawbreakers.candycraftce.utils.registry.LateInitAccessor
import com.mojang.datafixers.types.Type
import kotlinx.coroutines.Runnable
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.registries.Registries
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraftforge.common.ForgeSpawnEggItem
import net.minecraftforge.data.loading.DatagenModLoader
import net.minecraftforge.event.entity.EntityAttributeCreationEvent
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.loading.FMLLoader
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.DeferredRegister.create
import net.minecraftforge.registries.ForgeRegistries
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import java.util.function.Consumer
import java.util.function.Supplier


@Mod(CandyCraftCE.MOD_ID)
class CandyCraftCEForge : ICPlatForm {
    companion object {
        lateinit var instance: CandyCraftCEForge
            private set
    }

    init {
        instance = this
    }

    override val isDev by lazy { !FMLLoader.isProduction(); }

    override val isClient by lazy { FMLLoader.getDist().isClient }
    override val fluids: ICPlatformFluids get() = CForgeFluids
    override val levels: ICPlatformLevels get() = CForgeLevels
    override val datagen: ICPlatformDatagen? get() = if (DatagenModLoader.isRunningDataGen()) CandyCraftCEData else null
    override val clients: ICPlatFormClients? get() = if (isClient) CForgeClients else null

    val items: DeferredRegister<Item> = create(ForgeRegistries.ITEMS, CandyCraftCE.MOD_ID)
    val blocks: DeferredRegister<Block> = create(ForgeRegistries.BLOCKS, CandyCraftCE.MOD_ID)
    val entities: DeferredRegister<EntityType<*>> = create(ForgeRegistries.ENTITY_TYPES, CandyCraftCE.MOD_ID)
    val be: DeferredRegister<BlockEntityType<*>> = create(ForgeRegistries.BLOCK_ENTITY_TYPES, CandyCraftCE.MOD_ID)
    val tabs: DeferredRegister<CreativeModeTab> = create(Registries.CREATIVE_MODE_TAB, CandyCraftCE.MOD_ID)
    val particles: DeferredRegister<ParticleType<*>> = create(Registries.PARTICLE_TYPE, CandyCraftCE.MOD_ID)
    val mobEffects: DeferredRegister<MobEffect> = create(ForgeRegistries.MOB_EFFECTS, CandyCraftCE.MOD_ID)

    val registries = listOf(items, blocks, entities, be, tabs, particles, mobEffects)

    //=================================
    private var lateUsage: MutableList<Runnable>? = mutableListOf()

    fun onMinecraftSetup(event: FMLCommonSetupEvent) {
        clog.info("Running `whenInitialized`")
        lateUsage!!.also {
            lateUsage = null
            it.forEach(Runnable::run)
        }
    }

    override fun <T> whenInitialized(action: () -> T): Accessor<T> {
        val accessor = LateInitAccessor<T>()
        lateUsage?.add { accessor.set(action()) } ?: accessor.set(action())
        return accessor
    }
    //=================================

    fun <E> register(register: DeferredRegister<in E>, name: String, factory: Supplier<E>): Entry<E> {
        return register.register(name, factory)
            .also { logRegister(register.registryName.path, it.id) }
            .asEntry()
    }

    override fun createSpawnEggItem(
        type: Entry<EntityType<out Mob>>,
        backgroundColor: Int,
        highlightColor: Int,
        properties: Item.Properties,
    ) = ForgeSpawnEggItem(type, backgroundColor, highlightColor, properties)

    override fun <I : Item> registerItem(name: String, factory: Supplier<I>): Entry<I> =
        register(items, name, factory)

    override fun <M : MobEffect> registerMobEffect(
        name: String,
        factory: Supplier<M>,
    ): Entry<M> = register(mobEffects, name, factory)

    override fun <E : Block> registerBlock(name: String, factory: Supplier<E>): Entry<E> =
        register(blocks, name, factory)

    override fun <E : BlockEntity> registerBlockEntity(
        key: String,
        builder: Supplier<BlockEntityType.Builder<E>>,
        dsl: Type<*>?,
    ): Entry<BlockEntityType<E>> = register(be, key, { builder.get().build(dsl) })

    override fun <E : Entity> registerEntityType(key: String, type: Supplier<EntityType<E>>): Entry<EntityType<E>> =
        register(entities, key, type)

    private val attributes = hashMapOf<Entry<EntityType<out LivingEntity>>, Supplier<AttributeSupplier.Builder>>()

    @Suppress("UNCHECKED_CAST")
    override fun <LE : LivingEntity> registerEntityAttribute(
        entity: Entry<EntityType<LE>>,
        builder: Supplier<AttributeSupplier.Builder>,
    ) {
        attributes[entity as Entry<EntityType<out LivingEntity>>] = builder
    }

    fun onEntityAttributeCreation(event: EntityAttributeCreationEvent) {
        attributes.forEach { (entry, supplier) ->
            event.put(entry.value, supplier.get().build())
        }
    }

    val placements =
        hashMapOf<Entry<EntityType<Mob>>, Triple<SpawnPlacements.Type, Heightmap.Types, SpawnPlacements.SpawnPredicate<Mob>>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Mob> setEntityPlacement(
        entry: Entry<EntityType<T>>,
        spawnType: SpawnPlacements.Type,
        mapType: Heightmap.Types,
        predicate: SpawnPlacements.SpawnPredicate<T>,
    ) {
        placements[entry as Entry<EntityType<Mob>>] = Triple(
            spawnType,
            mapType,
            predicate as SpawnPlacements.SpawnPredicate<Mob>
        )
    }

    fun onSpawnPlacementRegister(event: SpawnPlacementRegisterEvent) {
        placements.forEach {
            val (spawnType, mapType, predicate) = it.value
            event.register(
                it.key.get(),
                spawnType,
                mapType,
                predicate,
                Operation.OR
            )
        }
    }

    override fun registerCreativeTab(
        name: String,
        builder: Consumer<CreativeModeTab.Builder>,
    ): Entry<CreativeModeTab> {
        return register(tabs, name) { CreativeModeTab.builder().also(builder::accept).build() }
    }

    override fun <P : ParticleType<*>> registerParticleType(
        name: String,
        factory: Supplier<P>,
    ): Entry<P> = register(particles, name, factory)


    //=================================
    init {
        clog.info("on Forge Initializing...")
        @Suppress("UnusedExpression")
        CandyCraftCE.init(
            this,
            preWorks = {
                //Registers
                registries.forEach { it.register(MOD_BUS) }

                with(MOD_BUS) {
                    addListener(::onMinecraftSetup)
                    addListener(::onEntityAttributeCreation)
                    addListener(::onSpawnPlacementRegister)
                }
                CForgeFluids
                CForgeLevels
                ifClient {
                    CForgeClients
                }
                CandyCraftCEData
            })
    }
}
