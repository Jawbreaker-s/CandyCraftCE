plugins {
    // see https://fabricmc.net/develop/ for new versions
    alias(libs.plugins.loom) apply false
    // see https://projects.neoforged.net/neoforged/moddevgradle for new versions
    alias(libs.plugins.moddev) apply false
}

ext {
    val vendor = System.getProperty("java.vm.vendor")
    val jetOption = vendor.contains("JetBrains")
    println("JVM Vendor: ${vendor}(AllowEnhancedClassRedefinition=${jetOption})")
    set("jetOption", jetOption)
}

allprojects {
    repositories {
        maven {
            name = "Modrinth"
            url = uri("https://api.modrinth.com/maven")
            content {
                includeGroup("maven.modrinth")
            }
        }
        flatDir {
            name = "Flat Dir(${project.name})"
            dirs(project.file("libs"))
            content {
                includeGroup("libs")
            }
        }
    }
    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}