package cn.jawbreakers.candycraftce

import cn.jawbreakers.candycraftce.client.PuddingColor
import cn.jawbreakers.candycraftce.registry.*
import cn.jawbreakers.candycraftce.registry.worldgen.CLevels
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CLogUtils.mainLog
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.ICPlatForm
import cn.jawbreakers.candycraftce.utils.registry.Accessor
import cn.jawbreakers.candycraftce.utils.registry.LateInitAccessor
import java.util.*
import java.util.function.Supplier
import kotlin.time.measureTime

object CandyCraftCE {
    const val MOD_ID = "candycraftce"
    const val MOD_NAME = "CandyCraftCE"

    init {
        CLogUtils.sign()
    }

    lateinit var platform: ICPlatForm
        private set
    private var postWorks: LinkedList<Runnable>? = LinkedList()
    fun <R> onPostWork(actions: Supplier<R>): Accessor<R> {
        val acc = LateInitAccessor<R>()
        postWorks?.add { acc.set(actions.get()) } ?: acc.set(actions.get())
        return acc
    }

    @Suppress("UnusedExpression")
    fun init(platform: ICPlatForm, preWorks: () -> Unit = {}, postWorks: () -> Unit = {}) {
        this.platform = platform
        mainLog.info("Initializing $MOD_NAME...")
        CLogUtils.markSignBegin()
        measureTime {
            preWorks()
            CTabs
            CFluids
            CBlocks
            CBlockEntities
            CItems
            CEntityTypes
            CMobEffects
            CParticleTypes
            CLevels
            ifClient {
                PuddingColor.initColor()
            }
            this.postWorks!!.also {
                it.forEach(Runnable::run)
                this.postWorks = null
            }
            postWorks()
        }.also {
            CLogUtils.markLateForSign()
            mainLog.info("CandyCraftCE loaded in $it")
        }
    }
}