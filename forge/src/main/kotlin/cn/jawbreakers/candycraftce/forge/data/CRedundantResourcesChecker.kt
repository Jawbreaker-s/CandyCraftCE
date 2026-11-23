package cn.jawbreakers.candycraftce.forge.data

import com.google.gson.JsonParser
import net.minecraft.resources.ResourceLocation
import java.io.File

object CRedundantResourcesChecker {

    fun run() {
        val projectRoot = findProjectRoot()
        val commonDir = File(projectRoot, "common")

        // 两个模型目录：生成的和手写的
        val modelDirs = listOf(
            File(commonDir, "src/generated/resources/assets/candycraftce/models"),
            File(commonDir, "src/main/resources/assets/candycraftce/models")
        )

        // 贴图根目录
        val textureDir = File(commonDir, "src/main/resources/assets/candycraftce/textures")

        if (!textureDir.exists()) {
            println("[CRedundantResourcesChecker] Texture directory not found: ${textureDir.absolutePath}")
            return
        }

        // 收集所有被模型引用的贴图（相对路径，不含 .png 后缀）
        val referencedTextures = mutableSetOf<String>()

        modelDirs.forEach { modelDir ->
            if (!modelDir.exists()) return@forEach
            modelDir.walkTopDown()
                .filter { it.isFile && it.extension == "json" }
                .forEach { file ->
                    try {
                        file.reader().use { reader ->
                            val json = JsonParser.parseReader(reader).asJsonObject
                            if (json.has("textures")) {
                                val textures = json.getAsJsonObject("textures")
                                textures.entrySet().forEach { (_, value) ->
                                    val textureRef = value.asString
                                    // 忽略变量引用（如 "#texture"），只处理直接路径
                                    if (!textureRef.startsWith("#")) {
                                        val (namespace, path) = parseTextureRef(textureRef)
                                        if (namespace == "candycraftce") {
                                            referencedTextures.add(path)
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("[CRedundantResourcesChecker] Failed to parse model file: ${file.absolutePath}")
                        e.printStackTrace()
                    }
                }
        }
        val usedTexture = mutableListOf<String>()
        // 遍历贴图目录，找出未被引用的 PNG
        val redundantTextures = mutableListOf<File>()
        listOf(textureDir.resolve("block"), textureDir.resolve("item")).forEach {
            it.walkTopDown()
                .filter { it.isFile && it.extension == "png" }
                .forEach { file ->
                    val relativePath = file.relativeTo(textureDir).path.replace(File.separatorChar, '/')
                    val pathWithoutExt = relativePath.removeSuffix(".png")
                    if (!referencedTextures.contains(pathWithoutExt)) {
                        redundantTextures.add(file)
                    } else {
                        usedTexture.add(pathWithoutExt)
                    }
                }
        }
        val missingTextures = referencedTextures.filter { it !in usedTexture }

        // 输出结果
        if (redundantTextures.isEmpty()) {
            println("[CRedundantResourcesChecker] No redundant textures found.")
        } else {
            println("[CRedundantResourcesChecker] Found ${redundantTextures.size} redundant textures:")
            redundantTextures.forEach { file ->
                println("  ${file.relativeTo(commonDir).path}")
            }
        }
        if (missingTextures.isEmpty()) {
            println("[CRedundantResourcesChecker] No missing textures found.")
        } else {
            println("[CRedundantResourcesChecker] Missing Textures: ${missingTextures.size}")
            missingTextures.forEach { tex ->
                println("  $tex")
            }
        }
    }

    /** 解析贴图引用，返回 (命名空间, 路径) */
    private fun parseTextureRef(ref: String): Pair<String, String> {
        return ResourceLocation(ref).run { namespace to path }
    }

    /** 从当前工作目录向上查找项目根目录（包含 settings.gradle 或 settings.gradle.kts） */
    private fun findProjectRoot(): File {
        var dir = File(".").canonicalFile
        while (dir.parentFile != null) {
            if (File(dir, "settings.gradle").exists() || File(dir, "settings.gradle.kts").exists()) {
                return dir
            }
            dir = dir.parentFile
        }
        return File(".").canonicalFile
    }

    fun getName(): String = "Candy Redundant Resources Checker"
}