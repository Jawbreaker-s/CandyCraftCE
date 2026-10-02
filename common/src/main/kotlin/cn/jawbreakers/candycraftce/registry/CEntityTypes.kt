package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.client.entity.renderers.CandyPigRenderer
import cn.jawbreakers.candycraftce.client.entity.renderers.CookieCreeperRenderer
import cn.jawbreakers.candycraftce.client.entity.renderers.CottonSheepRenderer
import cn.jawbreakers.candycraftce.client.entity.renderers.WaffleSheepRenderer
import cn.jawbreakers.candycraftce.entity.*
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.whenInitialized
import cn.jawbreakers.candycraftce.utils.ServerSafe
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.*
import net.minecraft.world.entity.EntityType.Builder.of
import net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.animal.Pig
import net.minecraft.world.entity.monster.Creeper
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
import java.util.function.BiConsumer
import java.util.function.Supplier

/**
 * Created in 2026/9/23 13:13 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
object CEntityTypes {
    init {
        CLogUtils.sign()
    }

    val candy_pig = register("candy_pig") {
        of(::CandyPigEntity, MobCategory.CREATURE)
            .sized(0.9F, 0.9F)
            .clientTrackingRange(10)
    }
        .attributes(Pig::createAttributes)
        .renderer(::CandyPigRenderer)
//        .placement(::canSpawnOnPudding)

    val waffle_sheep = register("waffle_sheep") {
        of(::WaffleSheepEntity, MobCategory.CREATURE)
            .sized(0.9f, 1.3f)
            .clientTrackingRange(10)
    }
        .attributes(WaffleSheepEntity::createAttributes)
        .renderer(::WaffleSheepRenderer)

    val cotton_sheep = register("cotton_sheep") {
        of(::CottonCandySheepEntity, MobCategory.CREATURE)
            .sized(0.9f, 1.3f)
            .clientTrackingRange(10)
    }
        .attributes(CottonCandySheepEntity::createAttributes)
        .renderer(::CottonSheepRenderer)
        .placement(ON_GROUND, MOTION_BLOCKING_NO_LEAVES, ::canSpawnOnCotton)

    val cookie_creeper = register("cookie_creeper") {
        of(::CookieCreeper, MobCategory.MONSTER)
            .sized(0.6F, 1.7F)
            .clientTrackingRange(8)
    }
        .attributes(Creeper::createAttributes)
        .renderer(::CookieCreeperRenderer)

    //=================================
    private fun <E : Entity> register(name: String, type: Supplier<EntityType.Builder<E>>) =
        CPlatformUtils.registerEntityType(name) { type.get().build("$MOD_ID:$name") }

    private inline fun <T : LivingEntity> Entry<EntityType<T>>.attributes(
        factory: Supplier<AttributeSupplier.Builder>,
        crossinline modifier: (AttributeSupplier.Builder.() -> Unit) = { },
    ) = apply {
        CPlatformUtils.registerEntityAttribute(this) { factory.get().also { modifier(it) } }
    }

    private fun registerSlime(
        name: String, width: Float, height: Float,
    ) =
        register(name) {
            of(::BasicCandySlimeEntity, MobCategory.MONSTER)
                .sized(width, height)
                .clientTrackingRange(8)
        }

    @Suppress("UNCHECKED_CAST")
    @ServerSafe
    private inline fun <T : Entity, ST : T> Entry<EntityType<ST>>.renderer(
        crossinline provider: (EntityRendererProvider.Context) -> EntityRenderer<T>,
        crossinline layers: BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>>.() -> Unit = {},
    ) =
        apply {
            ifClient {
                CPlatformUtils.clients?.registerEntityRenderer(this) { provider(it) }
                whenInitialized {
                    CPlatformUtils.clients?.let { layers(it::registerRenderLayers) }
                }
            }
        }

    //
//            event.register(CANDY_PIG.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(CANDY_WOLF.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            // Gummy terrain is 15.92/16 blocks tall, so vanilla ON_GROUND rejects it
//            // before our candy-surface predicate can run.
//            event.register(GUMMY_BUNNY.get(), NO_RESTRICTIONS, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(COTTON_CANDY_SHEEP.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES,CottonCandySheepEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(EASTER_CHICKEN.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES,EasterChickenEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(GUMMY_MOUSE.get(), NO_RESTRICTIONS, MOTION_BLOCKING_NO_LEAVES,GummyMouseEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(GUMMY_BEAR.get(), NO_RESTRICTIONS, MOTION_BLOCKING_NO_LEAVES,GummyBearEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(PINGOUIN.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(SUGUARD.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(MAGE_SUGUARD.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canHostileCandyMobSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(CANDY_CREEPER.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(COTTON_CANDY_SPIDER.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(JELLY_QUEEN.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(NOUGAT_GOLEM.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(BEETLE.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(CARAMEL_BEE.get(), NO_RESTRICTIONS, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canCaramelBeeSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(CANDY_FISH.get(), IN_WATER, MOTION_BLOCKING_NO_LEAVES, CandyFishEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(NESSIE.get(), IN_WATER, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canNessieSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
    private fun <T : Mob> Entry<EntityType<T>>.placement(
        spawnType: SpawnPlacements.Type,
        mapType: Heightmap.Types,
        predicate: SpawnPlacements.SpawnPredicate<T>,
    ) = apply {
        CPlatformUtils.setEntityPlacement(this, spawnType, mapType, predicate)
    }

    //==============Placement Presets===============
    private fun isBrightEnoughToSpawn(level: BlockAndTintGetter, pos: BlockPos): Boolean {
        return level.getRawBrightness(pos, 0) > 8
    }

    private fun <M : Mob> canSpawnOnPudding(
        type: EntityType<M>,
        level: LevelAccessor,
        reason: MobSpawnType,
        pos: BlockPos,
        random: RandomSource,
    ): Boolean {
        return isBrightEnoughToSpawn(level, pos)
                && level.getBlockState(pos.below()).`is`(CBlockTags.pudding_animal_spawnable_on)
    }

    private fun <M : Mob> canSpawnOnCotton(
        type: EntityType<M>,
        level: ServerLevelAccessor,
        reason: MobSpawnType,
        pos: BlockPos,
        random: RandomSource,
    ): Boolean {
        return isBrightEnoughToSpawn(level, pos)
                && level.getBlockState(pos.below()).`is`(CBlockTags.cotton_animal_spawnable_on)
    }
}