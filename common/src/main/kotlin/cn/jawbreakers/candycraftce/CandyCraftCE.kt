package cn.jawbreakers.candycraftce

import cn.jawbreakers.candycraftce.misc.PuddingColor
import cn.jawbreakers.candycraftce.registry.*
import cn.jawbreakers.candycraftce.utils.CLogUtils
import cn.jawbreakers.candycraftce.utils.CLogUtils.clog
import cn.jawbreakers.candycraftce.utils.CLogUtils.mainLog
import cn.jawbreakers.candycraftce.utils.CPlatformUtils.ifClient
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
        this.platform = platform
        mainLog.info("Initializing $MOD_NAME...")
        CLogUtils.markSignBegin()
        measureTime {
            CTabs
            CFluids
            CBlocks
            CBlockEntities
            CItems
            CLevels
            ifClient {
                clog.info("Init client part")
                PuddingColor.initColor()
                CLevels.initClient()
            }
            postWorks()
        }.also {
            CLogUtils.markLateForSign()
            mainLog.info("CandyCraftCE loaded in $it")
        }
    }
}