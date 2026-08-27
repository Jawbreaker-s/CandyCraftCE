package cn.jawbreakers.candycraftce

import cn.jawbreakers.candycraftce.registry.CBlocks
import cn.jawbreakers.candycraftce.registry.CItems
import cn.jawbreakers.candycraftce.registry.CTabs
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CLogUtils.mainLog
import cn.jawbreakers.candycraftce.utils.PlatformInstance
import kotlin.time.measureTime

object CandyCraftCE {
    const val MOD_ID = "candycraftce"
    const val MOD_NAME = "CandyCraftCE"

    init {
        CLogUtils.sign()
    }

    lateinit var platform: PlatformInstance
        private set


    @Suppress("UnusedExpression")
    fun init(platform: PlatformInstance, postWorks: () -> Unit = {}) {
        mainLog.info("Initializing $MOD_NAME...")
        this.platform = platform
        CLogUtils.markSignBegin()
        measureTime {
            CTabs
            CBlocks
            CItems
            postWorks()
        }.also {
            CLogUtils.markLateForSign()
            mainLog.info("CandyCraftCE loaded in $it")
        }
    }
}