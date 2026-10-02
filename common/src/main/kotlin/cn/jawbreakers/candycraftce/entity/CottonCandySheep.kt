package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.registry.CEntityTypes
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CItems.defaultInstance
import cn.jawbreakers.candycraftce.utils.CLevelUtils
import net.minecraft.core.BlockPos
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

class CottonCandySheep(type: EntityType<out CottonCandySheep?>, level: Level) :
    WaffleSheep(type, level) {
    companion object {
        val fur: EntityDataAccessor<Boolean> =
            SynchedEntityData.defineId(CottonCandySheep::class.java, EntityDataSerializers.BOOLEAN)

        fun createAttributes(): AttributeSupplier.Builder {
            return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
        }
    }

    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(fur, true)
    }

    var furry: Boolean
        set(value) = entityData.set(fur, value)
        get() = entityData.get(fur)

    override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(hand)
        if (!isBaby && furry && stack.`is`(CItems.marshmallow_stick.get())) {
            furry = false
            if (!level().isClientSide) {
                val give = when (type) {
                    CEntityTypes.raspberry_cotton_sheep.get() -> CItems.raspberry_cotton_candy.get()
                    else -> CItems.cotton_candy.get()
                }.defaultInstance
                if (!CLevelUtils.consumeAndGive(player, hand, give)) {
                    spawnAtLocation(give)
                }
            }
            playSound(SoundEvents.SHEEP_SHEAR, 1.0f, 1.0f)
            return InteractionResult.sidedSuccess(level().isClientSide)
        }
        return super.mobInteract(player, hand)
    }

    override fun willDropNugget(source: DamageSource): Boolean = false

    override fun ate() {
        furry = true
        if (isBaby) ageUp(60)
    }

    override fun getAmbientSound(): SoundEvent = SoundEvents.SHEEP_AMBIENT
    override fun getHurtSound(source: DamageSource): SoundEvent = SoundEvents.SHEEP_HURT
    override fun getDeathSound(): SoundEvent = SoundEvents.SHEEP_DEATH
    override fun playStepSound(pos: BlockPos, state: BlockState) = playSound(SoundEvents.SHEEP_STEP, 0.15f, 1.0f)
    override fun dropCustomDeathLoot(source: DamageSource, looting: Int, recentlyHit: Boolean) {
        if (furry) {
            spawnAtLocation(CItems.cotton_candy.defaultInstance.apply {
                count = 1 + random.nextInt(2) + random.nextInt(looting + 1)
            })
        }
    }

}

