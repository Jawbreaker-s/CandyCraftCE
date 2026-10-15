import groovy.xml.dom.DOMCategory.attributes
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.ext
import org.gradle.kotlin.dsl.jar
import org.gradle.kotlin.dsl.libs
import org.gradle.kotlin.dsl.neoForge
import org.gradle.kotlin.dsl.sourceSets

plugins {
    id("multiloader-loader")
    alias(libs.plugins.moddev)
}

val modId: String by project
val jetOption = rootProject.ext["jetOption"] as Boolean

sourceSets {
    main {
        resources.srcDir(project(":common").file("src/generated/resources"))
    }
}

mixin {
    add(sourceSets.main.get(), "${modId}.refmap.json")
    config("${modId}.mixins.json")
    config("${modId}.forge.mixins.json")
}

tasks.jar {
    manifest {
        attributes["MixinConfigs"] = "${modId}.mixins.json,${modId}.forge.mixins.json"
    }
}

neoForge {
    version = libs.versions.forge
    // Automatically enable neoforge AccessTransformers if the file exists
    val at = project(":common").file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
    parchment {
        minecraftVersion = libs.versions.parchmentMC
        mappingsVersion = libs.versions.parchment
    }
    runs {
        configureEach {
            systemProperty("forge.enabledGameTestNamespaces", modId)
            systemProperty("mixin.debug.export", "true")
            //forge cannot debug with such option with jbr
//            if (jetOption) {
//                jvmArgument("-XX:+AllowEnhancedClassRedefinition")
//            }
            ideName = "Forge ${name.capitalized()} (${project.path})" // Unify the run config names with fabric
        }
        register("client") {
            client()
        }
        register("data") {
            programArguments.add("--mod=${modId}")
            programArguments.add("--all")
            programArguments.add("--output=${project.project(":common").file("src/generated/resources").absolutePath}")
            programArguments.add(
                "--existing=${
                    project.project(":common").file("src/main/resources").absolutePath
                }"
            )
            data()
        }
        register("server") {
            server()
        }
    }
    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}


dependencies {
    modImplementation(libs.kff)
//    modRuntimeOnly(libs.jeiForge)
//    modCompileOnly(libs.jeiForgeApi)
    annotationProcessor(variantOf(libs.mixin) { classifier("processor") })
}