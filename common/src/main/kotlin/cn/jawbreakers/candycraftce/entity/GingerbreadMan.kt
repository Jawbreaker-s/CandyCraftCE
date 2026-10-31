package cn.jawbreakers.candycraftce.entity

import cn.jawbreakers.candycraftce.misc.EmblemHelper
import cn.jawbreakers.candycraftce.utils.CUtils.defineId
import cn.jawbreakers.candycraftce.utils.CUtils.synched
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

/**
 * Created in 2024/7/1 上午9:36
 * Project: candycraftce
 * 
 * Author [Bread_NiceCat](https://github.com/Bread-Nicecat)
 *
 */
class GingerbreadMan(type: EntityType<out GingerbreadMan>, level: Level) : PathfinderMob(type, level) {
    companion object {
        private val JOB: EntityDataAccessor<String> = defineId(EntityDataSerializers.STRING)
        private const val JOB_TAG = "Job"

        fun createAttributes(): AttributeSupplier.Builder {
            return createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.45)
        }
    }

    var job: GingerbreadJob by synched(JOB).xmap({ GingerbreadJob.idOf(it) }, { it.id })

    override fun registerGoals() {
        this.goalSelector.addGoal(0, FloatGoal(this))
        this.goalSelector.addGoal(1, PanicGoal(this, 1.5))
        this.goalSelector.addGoal(
            1,
            AvoidEntityGoal(this, Player::class.java, 8f, 1.0, 1.5) {
                it is Player && EmblemHelper.shouldGingerbreadManAvoid(it)
            }
        )
        this.goalSelector.addGoal(3, WaterAvoidingRandomStrollGoal(this, 0.8))
        this.goalSelector.addGoal(4, LookAtPlayerGoal(this, Player::class.java, 8f))
        this.goalSelector.addGoal(4, RandomLookAroundGoal(this))
    }

    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(JOB, GingerbreadJob.NONE.id)
    }

    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        tag.putString(JOB_TAG, this.job.id)
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        if (tag.contains(JOB_TAG)) {
            this.job = GingerbreadJob.idOf(tag.getString(JOB_TAG))
        }
    }


    enum class GingerbreadJob(val id: String) {
        NONE("none"), BLACKSMITH("blacksmith"), FARMER("farmer"), TRAVELLER("traveller");

        companion object {
            private val _cache: HashMap<String, GingerbreadJob> = HashMap()
            fun idOf(id: String, default: GingerbreadJob = NONE): GingerbreadJob {
                return _cache.computeIfAbsent(id) {
                    entries.firstOrNull { it.id == id } ?: default
                }
            }
        }
    }
}
