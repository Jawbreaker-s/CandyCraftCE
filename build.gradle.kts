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
    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}