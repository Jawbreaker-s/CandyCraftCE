package cn.jawbreakers.candycraftce.utils

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_NAME
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by NiceCat on 2026/5/1.
 * Project: candycraftce
 * @author <a href="https://github.com/BreadNiceCat">Bread_NiceCat</a>
 *
 */
object CLogUtils {

    val walker: StackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)

    private val logCache = ConcurrentHashMap<String, Logger>()
    val mainLog = modLogger("Core")
    private val registerLog = modLogger("Registry")
    val debugLog = modLogger("Debug")
    val clog: Logger
        get() = modLogger(walker.callerClass.let {
            try {
                @Suppress("NO_REFLECTION_IN_CLASS_PATH")
                if (it.kotlin.isCompanion) it.enclosingClass else it
            } catch (e: Exception) {
                e.printStackTrace()
                it
            }
        }.simpleName)

    fun modLogger(tag: String): Logger {
        return logCache.computeIfAbsent(tag) { LoggerFactory.getLogger("$MOD_NAME|${it}") }
    }

    private val signed = mutableSetOf<Class<*>>()
    private var late = false
    private var start = true
    fun sign() {
        val clazz = walker.callerClass
        if (!start) {
//            mainLog.error("The main class is not initialized. Accessing this class($clazz) too early may cause errors!")
            throw IllegalStateException("The main class is not initialized. Accessing this class($clazz) too early may cause errors!")
        }
        require(signed.add(clazz)) { "Class ${clazz.simpleName} is already signed" }
        mainLog.info("${clazz.simpleName} loaded")
        if (late) {
//            mainLog.error("This class($clazz) is late for sign! It is a bug, please report it!")
            throw IllegalStateException("Class $clazz is late for sign! It is a bug, please report it!")
        }
    }

    internal fun markLateForSign() {
        late = true
    }

    internal fun markSignBegin() {
        start = true
    }

    fun logRegister(type: String, id: ResourceLocation) {
        registerLog.info("Registering $type/$id")
    }
}
