plugins {
    id("multiloader-loader")
    alias(libs.plugins.loom)
}

val modId: String by project
val jetOption = rootProject.ext["jetOption"] as Boolean

repositories {
    maven { url = uri("https://maven.shedaniel.me/") }
    maven { url = uri("https://maven.terraformersmc.com/releases/") }
}
dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${libs.versions.parchmentMC.get()}:${libs.versions.parchment.get()}@zip")
    })
    modImplementation(libs.fabricLoader)
    modImplementation(libs.fabricApi)

    modImplementation(libs.flk)

    modImplementation("libs:ReservoirAPI:1.0-beta3")

    modImplementation(libs.fabricClothConfig) {
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-api")
    }
}

sourceSets {
    main {
        resources.srcDir(project(":common").file("src/generated/resources"))
    }
}
loom {
    val aw = project(":common").file("src/main/resources/${modId}.accesswidener")
    if (aw.exists()) {
        accessWidenerPath.set(aw)
    }
    mixin {
        defaultRefmapName.set("${modId}.refmap.json")
    }

    runs {
        configureEach {
            vmArg("-Dmixin.debug.export=true")
            if (jetOption) {
                vmArg("-XX:+AllowEnhancedClassRedefinition")
            }
        }
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("runs/client")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("runs/server")
        }
    }
}