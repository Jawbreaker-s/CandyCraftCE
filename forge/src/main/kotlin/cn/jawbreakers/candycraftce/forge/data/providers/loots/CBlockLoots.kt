package cn.jawbreakers.candycraftce.forge.data.providers.loots

import cn.jawbreakers.candycraftce.CandyCraftCE
import cn.jawbreakers.candycraftce.block.CandyCropBlock
import cn.jawbreakers.candycraftce.block.PlacedCutableJellyBlock
import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.utils.CUtils.applyIfNotNull
import cn.jawbreakers.candycraftce.utils.EntrySet
import cn.jawbreakers.candycraftce.utils.registry.Entry
import net.minecraft.advancements.critereon.StatePropertiesPredicate
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.StemBlock
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction.setCount
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator.between
import net.minecraftforge.registries.ForgeRegistries

/**
 * Created in 2026/10/10 21:05 by [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * Project: CandyCraftCE
 */
class CBlockLoots : BlockLootSubProvider(setOf(), FeatureFlags.REGISTRY.allFlags()) {
    companion object {
        private val LEAVES_SAPLING_CHANCES = floatArrayOf(0.05f, 0.0625f, 0.083333336f, 0.1f)
        private val LEAVES_STICK_CHANCES = floatArrayOf(0.02f, 0.022222223f, 0.025f, 0.033333335f, 0.1f)
        private val LEAVES_SPECIAL_CHANCES = floatArrayOf(0.005F, 0.0055555557F, 0.00625F, 0.008333334F, 0.025F)
        private val HAS_NO_SHEARS = HAS_SHEARS.invert()
        private val HAS_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH)
        private val HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS_OR_SILK_TOUCH.invert()
    }

    override fun generate() {
        CItems.apply {
            CBlocks.apply {
                val matured = LootItemBlockStatePropertyCondition.hasBlockStateProperties(lollipop_stem.get())
                    .setProperties(
                        StatePropertiesPredicate.Builder.properties()
                            .hasProperty(CandyCropBlock.AGE, CandyCropBlock.MAX_AGE)
                    )
                add(
                    dragibus_crops.get(),
                    applyExplosionDecay(
                        dragibus.get(),
                        LootTable.lootTable()
                            .withPool(LootPool.lootPool().add(LootItem.lootTableItem(dragibus.get())))
                            .withPool(
                                LootPool.lootPool().`when`(matured)
                                    .add(
                                        LootItem.lootTableItem(dragibus.get()).apply(
                                            ApplyBonusCount.addBonusBinomialDistributionCount(
                                                Enchantments.BLOCK_FORTUNE,
                                                0.5714286f,
                                                3
                                            )
                                        )
                                    )
                            )
                    )
                )
                add(
                    lollipop_stem.get(),
                    //SILK + MATURED
                    createSilkTouchOrShearsDispatchTable(
                        lollipop_stem.get(),
                        LootItem.lootTableItem(CItems.lollipop_stem.get()).`when`(matured)
                    ).withPool(
                        //NOT SILK OR NOT MATURED
                        applyExplosionDecay(
                            lollipop_stem.get(),
                            LootPool.lootPool().`when`(HAS_NO_SHEARS_OR_SILK_TOUCH.or(matured.invert()))
                                .setRolls(exactly(1.0f))
                                .add(
                                    LootItem.lootTableItem(CItems.lollipop_seeds.get())
                                        .apply(StemBlock.AGE.possibleValues) { age ->
                                            setCount(
                                                BinomialDistributionGenerator.binomial(
                                                    3,
                                                    (age + 1).toFloat() / 15.0f
                                                )
                                            ).`when`(
                                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(
                                                    lollipop_stem.get()
                                                ).setProperties(
                                                    StatePropertiesPredicate.Builder.properties()
                                                        .hasProperty(CandyCropBlock.AGE, age)
                                                )
                                            )
                                        }
                                )
                        )
                    )
                )
                add(
                    caramel_leaves.get(),
                    createCandyLeaves(caramel_leaves.get(), caramel_sapling.get(), caramel_leaf.get())
                )
                add(
                    chocolate_leaves.get(),
                    createCandyLeaves(chocolate_leaves.get(), chocolate_sapling.get(), chocolate_leaf.get())
                )
                add(
                    white_chocolate_leaves.get(),
                    createCandyLeaves(
                        white_chocolate_leaves.get(),
                        white_chocolate_sapling.get(),
                        white_chocolate_leaf.get()
                    )
                )
                add(
                    enchant_candy_leaves.get(),
                    createCandyLeaves(enchant_candy_leaves.get(), Items.SUGAR, magical_leaf.get())
                )
                add(
                    candied_cherry_leaves.get(),
                    createCandyLeaves(
                        candied_cherry_leaves.get(),
                        candied_cherry_sapling.get(),
                        candied_cherry_leaf.get(),
                        special = CItems.candied_cherry.get()
                    )
                )
                dropWhenSilkTouch(custard_pudding_block.get(), pudding_block.get())
                dropWhenSilkTouch(chocolate_stone.get(), chocolate_cobblestone.get())
                dropWhenSilkTouch(white_chocolate_stone.get(), white_chocolate_cobblestone.get())

                dropWhenSilkTouch(honey_ore.get(), honey_shard.get(), between(1f, 2f), true)
                dropWhenSilkTouch(white_honey_ore.get(), honey_shard.get(), between(1f, 2f), true)
                dropWhenSilkTouch(nougat_ore.get(), nougat_powder.get(), between(2f, 3f), true)
                dropWhenSilkTouch(white_nougat_ore.get(), nougat_powder.get(), between(2f, 3f), true)
                add(
                    marshmallow_slice_flower.get(),
                    createShearsOnlyDrop(marshmallow_slice_flower.get())
                        .withPool(
                            LootPool.lootPool().`when`(HAS_NO_SHEARS).setRolls(exactly(1f))
                                .add(LootItem.lootTableItem(marshmallow_slice.get()))
                        )
                        .withPool(
                            LootPool.lootPool().`when`(HAS_NO_SHEARS).setRolls(exactly(1f))
                                .add(LootItem.lootTableItem(marshmallow_slice_flower.get()))
                        )
                )
                add(
                    lollipop_fruit.get(), createShearsOnlyDrop(lollipop_fruit.get())
                        .withPool(
                            LootPool.lootPool().`when`(HAS_NO_SHEARS).setRolls(exactly(1f))
                                .add(
                                    LootItem.lootTableItem(lollipop.get()).apply(
                                        setCount(between(2f, 4f))
                                    )
                                )
                        )
                )
                listOf(
                    caramel_jelly_block to caramel_jelly_slice,
                    strawberry_jelly_block to strawberry_jelly_slice,
                    royal_rations_block to royal_rations_slice
                ).forEach { (block, slice) ->
                    val block = block.get()
                    val slice = slice.get()
                    fun ifCut(value: Int) = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                        .setProperties(
                            StatePropertiesPredicate.Builder.properties()
                                .hasProperty(PlacedCutableJellyBlock.CUTS, value)
                        )

                    val full = ifCut(0)
                    val condition = HAS_SILK_TOUCH.and(full)
                    add(
                        block,
                        applyExplosionDecay(
                            block,
                            LootTable.lootTable()
                                .withPool(
                                    LootPool.lootPool().`when`(condition)
                                        .add(LootItem.lootTableItem(block))
                                )
                                .withPool(
                                    LootPool.lootPool().`when`(condition.invert())
                                        .setRolls(exactly(1f))
                                        .add(LootItem.lootTableItem(slice))
                                        .apply(PlacedCutableJellyBlock.CUTS.possibleValues) { cuts ->
                                            setCount(exactly(PlacedCutableJellyBlock.MAX_CUTS - cuts + 1f))
                                                .`when`(ifCut(cuts))
                                        }
                                )
                        )
                    )
                }

                listOf(
                    sweet_grass_red,
                    sweet_grass_pale,
                    sweet_grass_yellow,
                    sweet_grass_pink,
                    rope_raspberry,
                    mint,
                    banana_seaweed,
                ).forEach {
                    add(it.get(), createShearsOnlyDrop(it.get()))
                }

                (glassTypes.flatten().flatMap { listOf(it.first, it.second) } + listOf(
                    grenadine_ice,
                    mint_jelly_block,
                    lemon_jelly_block,
                    raspberry_jelly_block,
                )).forEach { dropWhenSilkTouch(it.get()) }


                listOf(
                    pudding_block,
                    sugar_sand,
                    marshmallow_log,
                    light_marshmallow_log,
                    dark_marshmallow_log,
                    stripped_marshmallow_log,
                    stripped_light_marshmallow_log,
                    stripped_dark_marshmallow_log,
                    marshmallow_planks,
                    marshmallow_family,
                    light_marshmallow_planks,
                    light_marshmallow_family,
                    dark_marshmallow_planks,
                    dark_marshmallow_family,
                    marshmallow_ladder,
                    candy_cane_block,
                    candy_cane_family,
                    sugar_block,
                    sugar_family,
                    sugar_brick,
                    sugar_brick_family,
                    cotton_candy_block,
                    cotton_candy_family,
                    caramel_block,
                    caramel_family,
                    caramel_bricks,
                    caramel_bricks_family,
                    licorice_block,
                    licorice_family,
                    licorice_brick,
                    licorice_brick_family,
                    nougat_block,
                    chiseled_nougat_block,
                    square_pattern_nougat_block,
                    nougat_head,
                    nougat_family,
                    ice_cream,
                    ice_cream_family,
                    mint_ice_cream,
                    mint_ice_cream_family,
                    blueberry_ice_cream,
                    blueberry_ice_cream_family,
                    chocolate_ice_cream,
                    chocolate_ice_cream_family,
                    banana_ice_cream,
                    banana_ice_cream_family,
                    strawberry_ice_cream,
                    strawberry_ice_cream_family,
                    sugar_spikes,
                    cranberry_spikes,
                    trampojelly,
                    red_trampojelly,
                    purple_trampojelly,
                    yellow_trampojelly,
                    jelly_shock_absorber,
                    banana_block,
                    chewing_gum_block,
                    mint_block,
                    mint_family,
                    candied_cherry_sack,
                    raspberry_block,
                    honey_lamp,
                    honeycomb_block,
                    pez_block,
                    chocolate_stone_family, white_chocolate_stone_family,
                    chocolate_bricks, white_chocolate_bricks,
                    chocolate_cobblestone, white_chocolate_cobblestone,
                    chocolate_cobblestone_family, white_chocolate_cobblestone_family,
                    licorice_ore, white_licorice_ore,
                    pez_ore, white_pez_ore,
                    jelly_ore, white_jelly_ore,
                    caramel_sapling,
                    chocolate_sapling,
                    candied_cherry_sapling,
                    white_chocolate_sapling,
                    acid_mint_flower,
                    sugar_essence_flower,
                    fraise_tagada_flower,
                    chewing_gum_puddle,
                    marshmallow_slice,
                ).forEach {
                    when (it) {
                        is Entry<*> -> dropSelf(it.get() as Block)
                        is EntrySet<*> -> it.forEach { b -> dropSelf(b as Block) }
                        else -> error("Unsupported type: $it")
                    }
                }
            }
        }
    }

    fun dropWhenSilkTouch(block: Block, alternation: ItemLike, count: Int = 1) {
        dropWhenSilkTouch(block, alternation, exactly(count.toFloat()))
    }

    fun dropWhenSilkTouch(block: Block, alternation: ItemLike, count: NumberProvider, oreFortune: Boolean = false) {
        add(
            block, applyExplosionDecay(
                block, createSilkTouchDispatchTable(
                    block,
                    LootItem.lootTableItem(alternation)
                        .apply(setCount(count))
                        .also { if (oreFortune) it.apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)) }
                )
            )
        )
    }

    fun createCandyLeaves(
        leaves: Block,
        sapling: ItemLike,
        leaf: ItemLike?,
        stick: ItemLike = CItems.marshmallow_stick.get(),
        special: ItemLike? = null,
    ): LootTable.Builder {
        return createSilkTouchOrShearsDispatchTable(
            leaves,
            applyExplosionCondition(leaves, LootItem.lootTableItem(sapling))
                .`when`(
                    BonusLevelTableCondition.bonusLevelFlatChance(
                        Enchantments.BLOCK_FORTUNE,
                        *LEAVES_SAPLING_CHANCES
                    )
                )
        ).withPool(
            LootPool.lootPool().`when`(HAS_NO_SHEARS_OR_SILK_TOUCH).setRolls(exactly(1.0f))
                .`when`(
                    BonusLevelTableCondition.bonusLevelFlatChance(
                        Enchantments.BLOCK_FORTUNE,
                        *LEAVES_STICK_CHANCES
                    )
                )
                .add(
                    applyExplosionDecay(
                        leaves,
                        LootItem.lootTableItem(stick).apply(setCount(between(1.0f, 2.0f)))
                    )
                )
        ).applyIfNotNull(leaf) { leaf ->
            withPool(
                LootPool.lootPool().`when`(HAS_NO_SHEARS_OR_SILK_TOUCH).setRolls(exactly(1.0f))
                    .`when`(
                        BonusLevelTableCondition.bonusLevelFlatChance(
                            Enchantments.BLOCK_FORTUNE,
                            *LEAVES_SAPLING_CHANCES
                        )
                    )
                    .add(
                        applyExplosionDecay(
                            leaves,
                            LootItem.lootTableItem(leaf).apply(setCount(between(1.0f, 2.0f)))
                        )
                    )
            )
        }.applyIfNotNull(special) { special ->
            withPool(
                LootPool.lootPool().`when`(HAS_NO_SHEARS_OR_SILK_TOUCH).setRolls(exactly(1.0f))
                    .`when`(
                        BonusLevelTableCondition.bonusLevelFlatChance(
                            Enchantments.BLOCK_FORTUNE,
                            *LEAVES_SPECIAL_CHANCES
                        )
                    )
                    .add(
                        applyExplosionDecay(
                            leaves,
                            LootItem.lootTableItem(special)
                        )
                    )
            )
        }
    }

    override fun getKnownBlocks(): Iterable<Block> {
        return ForgeRegistries.BLOCKS.entries
            .asSequence()
            .filter { it.key.location().namespace == CandyCraftCE.MOD_ID }
            .map { it.value }
            .asIterable()
    }


}