package cn.jawbreakers.candycraftce.registry

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import cn.jawbreakers.candycraftce.CandyCraftCE.onPostWork
import cn.jawbreakers.candycraftce.client.entity.models.CaramelBeeModel
import cn.jawbreakers.candycraftce.client.entity.models.CranfishModel
import cn.jawbreakers.candycraftce.client.entity.models.GummyBunnyModel
import cn.jawbreakers.candycraftce.client.entity.models.GummyBunnyOuterModel
import cn.jawbreakers.candycraftce.client.entity.renderers.*
import cn.jawbreakers.candycraftce.client.entity.renderers.jelly.JellyQueenRenderer
import cn.jawbreakers.candycraftce.client.entity.renderers.jelly.LemonJellyRenderer
import cn.jawbreakers.candycraftce.client.entity.renderers.jelly.MintJellyRenderer
import cn.jawbreakers.candycraftce.client.entity.renderers.jelly.RaspberryJellyRenderer
import cn.jawbreakers.candycraftce.entity.*
import cn.jawbreakers.candycraftce.entity.jelly.*
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.ClientOnly
import cn.jawbreakers.candycraftce.utils.ServerSafe
import cn.jawbreakers.candycraftce.utils.registry.Entry
import it.unimi.dsi.fastutil.ints.IntIntPair
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.ThrownItemRenderer
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.*
import net.minecraft.world.entity.EntityType.Builder.of
import net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.animal.Pig
import net.minecraft.world.entity.animal.WaterAnimal
import net.minecraft.world.entity.animal.WaterAnimal.checkSurfaceWaterAnimalSpawnRules
import net.minecraft.world.entity.monster.Creeper
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.monster.Slime
import net.minecraft.world.entity.monster.Spider
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
import java.util.function.Supplier

/**
 * Created in 2026/9/23 13:13 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
object CEntityTypes {
    init {
        CLogUtils.sign()
    }

    // will register in CItems sooner
    internal val eggs = mutableMapOf<Entry<EntityType<out Mob>>, IntIntPair>()

    val honey_arrow = register("honey_arrow") {
        of(::HoneyArrow, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(4)
            .updateInterval(20)
    }
        .renderer(::HoneyArrowRenderer)

    val dynamite = register("dynamite") {
        of(::DynamiteEntity, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10)
    }
        .renderer(::ThrownItemRenderer)

    val glue_dynamite = register("glue_dynamite") {
        of(::GlueDynamiteEntity, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10)
    }
        .renderer(::ThrownItemRenderer)

    //
//    val gummy_ball = register("gummy_ball") {
//        of(::GummyBallEntity, MobCategory.MISC)
//            .sized(0.25F, 0.25F)
//            .clientTrackingRange(8)
//            .updateInterval(10)
//    }
//
//    val thrown_fork = register("thrown_fork") {
//        of(::ThrownForkEntity, MobCategory.MISC)
//            .sized(0.5F, 0.5F)
//            .clientTrackingRange(4)
//            .updateInterval(20)
//    }
//
//    val thrown_fork_block = register("thrown_fork_block") {
//        of(::ThrownForkBlockEntity, MobCategory.MISC)
//            .sized(0.65F, 0.65F)
//            .clientTrackingRange(8)
//            .updateInterval(2)
//    }
    val candy_pig = register("candy_pig") {
        of(::CandyPigEntity, MobCategory.CREATURE)
            .sized(0.9F, 0.9F)
            .clientTrackingRange(10)
    }
        .egg(0xF1C3C3, 0xFB5757)
        .attributes(Pig::createAttributes)
        .renderer(::CandyPigRenderer)
        .placement(::checkCandyAnimalSpawnRules)

    val waffle_sheep = register("waffle_sheep") {
        of(::WaffleSheep, MobCategory.CREATURE)
            .sized(0.9f, 1.3f)
            .clientTrackingRange(10)
    }
        .egg(0xF1C3C3, 0xFFC000)
        .attributes(WaffleSheep::createAttributes)
        .renderer(::WaffleSheepRenderer)
        .placement(::checkCandyAnimalSpawnRules)

    val gummy_bunny = register("gummy_bunny") {
        of(::GummyBunny, MobCategory.CREATURE)
            .sized(0.5F, 0.4F)
            .clientTrackingRange(10)
    }
        .egg(0xF1C3C3, 0xEEFF33)
        .attributes(GummyBunny::createAttributes)
        .renderer(::GummyBunnyRenderer) {
            registerLayer(GummyBunnyModel.LAYER, GummyBunnyModel<*>::createBodyLayer)
            registerLayer(GummyBunnyOuterModel.LAYER, GummyBunnyOuterModel<*>::createBodyLayer)
        }
        .placement(::checkCandyAnimalSpawnRules)

    //===============WATER_AMBIENT===============
    val cranfish = register("cranfish") {
        of(::Cranfish, MobCategory.WATER_AMBIENT)
            .sized(0.95F, 0.95F)
            .clientTrackingRange(8)
    }
        .egg(0xF1C3C3, 0x3A01DF)
        .attributes(Cranfish::createAttributes)
        .renderer(::CranfishRenderer) {
            registerLayer(CranfishModel.LAYER, CranfishModel<*>::createBodyLayer)
        }
        .placement(::checkCandyWaterAnimalSpawnRules)

    //===============MONSTERS===============
    val cookie_creeper = register("cookie_creeper") {
        of(::CookieCreeper, MobCategory.MONSTER)
            .sized(0.6F, 1.7F)
            .clientTrackingRange(8)
    }
        .egg(0xF1C3C3, 0x777777)
        .attributes(Creeper::createAttributes)
        .renderer(::CookieCreeperRenderer)
        .placement(::checkCandyMonsterSpawnRules)

    val cotton_candy_spider = register("cotton_candy_spider") {
        of(::CottonCandySpider, MobCategory.MONSTER)
            .sized(1.4F, 0.9F)
            .clientTrackingRange(8)
    }
        .egg(0xF1C3C3, 0xA00000)
        .attributes(Spider::createAttributes)
        .renderer(::CottonCandySpiderRenderer)
        .placement(::checkCandyMonsterSpawnRules)


    val caramel_bee = register("caramel_bee") {
        of(::CaramelBee, MobCategory.MONSTER)
            .sized(0.8F, 1.0F)
            .clientTrackingRange(8)
            .updateInterval(1)
    }
        .egg(0xF1C3C3, 0xFE7F01)
        .attributes(CaramelBee::createAttributes)
        .renderer(::CaramelBeeRenderer) {
            registerLayer(CaramelBeeModel.LAYER, CaramelBeeModel<*>::createBodyLayer)
        }
        .placement(SpawnPlacements.Type.NO_RESTRICTIONS, MOTION_BLOCKING_NO_LEAVES, CaramelBee::checkSpawnRules)

    //================JELLIES================
    val lemon_jelly = registerJelly("lemon_jelly", ::LemonJelly)
        .renderer(::LemonJellyRenderer)
        .egg(0x555555, 0xFFFF00)
    val raspberry_jelly = registerJelly("raspberry_jelly", ::RaspberryJelly)
        .renderer(::RaspberryJellyRenderer)
        .egg(0x555555, 0xFF0000)
    val mint_jelly = registerJelly("mint_jelly", ::MintJelly)
        .renderer(::MintJellyRenderer)
        .egg(0x555555, 0x00FFFF)
    val jelly_queen = registerJelly("jelly_queen", ::JellyQueen, isBoss = true)
        .renderer(::JellyQueenRenderer)
        .egg(0xFF7373, 0xCF00EF)

    //=================================
    private fun <E : Entity> register(name: String, type: Supplier<EntityType.Builder<E>>) =
        CPlatformUtils.registerEntityType(name) { type.get().build("$MOD_ID:$name") }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Mob> Entry<EntityType<T>>.egg(backgroundColor: Int, highlightColor: Int) =
        apply {
            eggs[this as Entry<EntityType<out Mob>>] = IntIntPair.of(backgroundColor, highlightColor)
        }

    private inline fun <T : LivingEntity> Entry<EntityType<T>>.attributes(
        factory: Supplier<AttributeSupplier.Builder>,
        crossinline modifier: (AttributeSupplier.Builder.() -> Unit) = { },
    ) = apply {
        CPlatformUtils.registerEntityAttribute(this) { factory.get().also { modifier(it) } }
    }

    private fun <T : BasicJelly> registerJelly(
        name: String, factory: EntityType.EntityFactory<T>, isBoss: Boolean = false,
    ) =
        register(name) {
            of(factory, MobCategory.MONSTER)
                .sized(EntityType.SLIME.width, EntityType.SLIME.height)
                .clientTrackingRange(8)
        }.apply {
            if (isBoss) {
                attributes(BossJelly::createBossJellyAttribute)
            } else {
                attributes(BasicJelly::createJellyAttribute)
            }
        }.placement { type, level, spawnType, pos, random ->
            @Suppress("UNCHECKED_CAST")
            Slime.checkSlimeSpawnRules(type as EntityType<Slime>, level, spawnType, pos, random)
        }


    @Suppress("UNCHECKED_CAST")
    @ServerSafe
    private inline fun <T : Entity, ST : T> Entry<EntityType<ST>>.renderer(
        crossinline provider: (EntityRendererProvider.Context) -> EntityRenderer<T>,
        crossinline layers: LayerRegister.() -> Unit = {},//这里的lambda可以安全使用该文件的Entry对象
    ) =
        apply {
            ifClient {
                registerEntityRenderer(this@renderer) { provider(it) }
                onPostWork {
                    layers(::registerRenderLayers)
                }
            }
        }

    @ClientOnly
    fun interface LayerRegister {
        fun registerLayer(key: ModelLayerLocation, factory: Supplier<LayerDefinition>)
    }

    //	registerSpawnEgg("suguard_spawn_egg", CCEntityTypes.SUGUARD, 0xF1C3C3, 0x8E0082);
    //	registerSpawnEgg("mage_suguard_spawn_egg", CCEntityTypes.MAGE_SUGUARD, 0xF1C3C3, 0xEB3D00);
    //	registerSpawnEgg("candy_wolf_spawn_egg", CCEntityTypes.CANDY_WOLF, 0xF1C3C3, 0xDDDDDD);
    //	registerSpawnEgg("cotton_candy_sheep_spawn_egg", CCEntityTypes.COTTON_CANDY_SHEEP, 0xFF33FF, 0xFFCCFF);
    //	registerSpawnEgg("easter_chicken_spawn_egg", CCEntityTypes.EASTER_CHICKEN, 0x996611, 0x774411);
    //	registerSpawnEgg("gummy_mouse_spawn_egg", CCEntityTypes.GUMMY_MOUSE, 0x00FF00, 0x33BB33);
    //	registerSpawnEgg("gummy_bear_spawn_egg", CCEntityTypes.GUMMY_BEAR, 0x00FF00, 0x33BB33);
    //	registerSpawnEgg("gingerbread_man_spawn_egg", CCEntityTypes.GINGERBREAD_MAN, 0xF1C3C3, 0x61380B);
    //	registerSpawnEgg("pingouin_spawn_egg", CCEntityTypes.PINGOUIN, 0xF1C3C3, 0xFFFFFF);
    //	registerSpawnEgg("beetle_spawn_egg", CCEntityTypes.BEETLE, 0xF1C3C3, 0x250066);
    //	registerSpawnEgg("nessie_spawn_egg", CCEntityTypes.NESSIE, 0xF1C3C3, 0xA9E2F3);
    //	registerSpawnEgg("dragon_spawn_egg", CCEntityTypes.DRAGON, 0x8DC444, 0xA4EDFF);
    //	registerSpawnEgg("king_beetle_spawn_egg", CCEntityTypes.KING_BEETLE, 0x8DC444, 0xA500B3);
    //	registerSpawnEgg("mermaid_spawn_egg", CCEntityTypes.MERMAID, 0x555555, 0x7D82B0);
    //	registerSpawnEgg("nougat_golem_spawn_egg", CCEntityTypes.NOUGAT_GOLEM, 0xD8C18C, 0x805B38);

    //	registerSpawnEgg("pez_jelly_spawn_egg", CCEntityTypes.PEZ_JELLY, 0x9166FF, 0xFFFFFF);
    //	registerSpawnEgg("king_slime_spawn_egg", CCEntityTypes.KING_SLIME, 0xB23838, 0xE37D11);
    //	registerSpawnEgg("boss_suguard_spawn_egg", CCEntityTypes.BOSS_SUGUARD, 0xFF7373, 0xDFDFDF);
    //	registerSpawnEgg("boss_beetle_spawn_egg", CCEntityTypes.BOSS_BEETLE, 0xFF7373, 0x1C1C1C);

    //
//            event.register(CANDY_PIG.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(CANDY_WOLF.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            // Gummy terrain is 15.92/16 blocks tall, so vanilla ON_GROUND rejects it
//            // before our candy-surface predicate can run.
//            event.register(GUMMY_BUNNY.get(), NO_RESTRICTIONS, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(COTTON_CANDY_SHEEP.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES,CottonCandySheep::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(EASTER_CHICKEN.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES,EasterChickenEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(GUMMY_MOUSE.get(), NO_RESTRICTIONS, MOTION_BLOCKING_NO_LEAVES,GummyMouseEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(GUMMY_BEAR.get(), NO_RESTRICTIONS, MOTION_BLOCKING_NO_LEAVES,GummyBearEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(PINGOUIN.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(SUGUARD.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(MAGE_SUGUARD.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canHostileCandyMobSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(COTTON_CANDY_SPIDER.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(JELLY_QUEEN.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(NOUGAT_GOLEM.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(BEETLE.get(), ON_GROUND, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(CARAMEL_BEE.get(), NO_RESTRICTIONS, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canCaramelBeeSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(CANDY_FISH.get(), IN_WATER, MOTION_BLOCKING_NO_LEAVES, Cranfish::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
//            event.register(NESSIE.get(), IN_WATER, MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canNessieSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
    private fun <T : Mob> Entry<EntityType<T>>.placement(
        spawnType: SpawnPlacements.Type,
        mapType: Heightmap.Types,
        predicate: SpawnPlacements.SpawnPredicate<T>,
    ) = apply {
        CPlatformUtils.setEntityPlacement(this, spawnType, mapType, predicate)
    }

    private fun <T : Mob> Entry<EntityType<T>>.placement(predicate: SpawnPlacements.SpawnPredicate<T>) =
        placement(ON_GROUND, MOTION_BLOCKING_NO_LEAVES, predicate)

    //==============Placement Presets===============
    private fun isBrightEnoughToSpawn(level: BlockAndTintGetter, pos: BlockPos): Boolean {
        return level.getRawBrightness(pos, 0) > 8
    }

    private fun <M : Mob> checkCandyAnimalSpawnRules(
        type: EntityType<M>,
        level: ServerLevelAccessor,
        reason: MobSpawnType,
        pos: BlockPos,
        random: RandomSource,
    ): Boolean {
        return isBrightEnoughToSpawn(level, pos) && level.getBlockState(pos.below())
            .`is`(CBlockTags.candy_animal_spawnable_on)
    }

    private fun <M : Monster> checkCandyMonsterSpawnRules(
        type: EntityType<M>,
        level: ServerLevelAccessor,
        reason: MobSpawnType,
        pos: BlockPos,
        random: RandomSource,
    ): Boolean = Monster.checkMonsterSpawnRules(type, level, reason, pos, random)

    private fun <M : WaterAnimal> checkCandyWaterAnimalSpawnRules(
        type: EntityType<M>,
        level: ServerLevelAccessor,
        reason: MobSpawnType,
        pos: BlockPos,
        random: RandomSource,
    ): Boolean = checkSurfaceWaterAnimalSpawnRules(type, level, reason, pos, random)
}