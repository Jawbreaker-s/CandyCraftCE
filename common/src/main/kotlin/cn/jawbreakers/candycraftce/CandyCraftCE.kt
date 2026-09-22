package cn.jawbreakers.candycraftce

import cn.jawbreakers.candycraftce.client.PuddingColor
import cn.jawbreakers.candycraftce.registry.*
import cn.jawbreakers.candycraftce.registry.worldgen.CLevels
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CLogUtils.mainLog
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
import cn.jawbreakers.candycraftce.utils.ICPlatForm
import kotlin.time.measureTime

object CandyCraftCE {
    const val MOD_ID = "candycraftce"
    const val MOD_NAME = "CandyCraftCE"

    init {
        CLogUtils.sign()
    }

    lateinit var platform: ICPlatForm
        private set


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
            CParticles
            CLevels
            ifClient {
                PuddingColor.initColor()
            }
            postWorks()
        }.also {
            CLogUtils.markLateForSign()
            mainLog.info("CandyCraftCE loaded in $it")
        }
    }
}