package cn.jawbreakers.candycraftce.forge.data.providers

import cn.jawbreakers.candycraftce.CandyCraftCE.MOD_ID
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraftforge.common.data.LanguageProvider
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class CI18nProvider(output: PackOutput) : DataProvider {
    override fun getName(): String = "CandyCraftCE I18n"
    private val enUS: SubLanguageProvider = SubLanguageProvider(output, "en_us")
    private val zhCN: SubLanguageProvider = SubLanguageProvider(output, "zh_cn")
    private val subs = setOf(enUS, zhCN)


    private fun addTranslations() {
        add("mod.$MOD_ID", "CandyCraft Community Edition", "糖果世界社区版")
    }

    fun add(key: String, en: String, zh: String) {
        enUS.add { it.add(key, en) }
        zhCN.add { it.add(key, zh) }
    }

    class SubLanguageProvider(output: PackOutput, locate: String) : LanguageProvider(output, MOD_ID, locate) {
        private val entries = mutableListOf<Consumer<LanguageProvider>>()
        override fun addTranslations() {
            entries.forEach { it.accept(this) }
        }

        fun add(consumer: Consumer<LanguageProvider>) = entries.add(consumer)
    }

    override fun run(output: CachedOutput): CompletableFuture<*> {
        addTranslations()
        return CompletableFuture.allOf(*subs.map { it.run(output) }.toTypedArray())
    }
}