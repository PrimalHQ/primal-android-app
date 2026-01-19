import java.util.*

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.org.jetbrains.kotlin.android)
}

val configProperties by lazy {
    val configFile = File("config.properties")
    if (configFile.exists()) {
        Properties().apply { load(configFile.reader()) }
    } else {
        null
    }
}

android {
    namespace = "net.primal.android.macrobenchmark"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    kotlin {
        jvmToolchain(17)
    }

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        buildConfigField(
            type = "String",
            name = "BENCHMARK_NSEC",
            value = "\"${configProperties?.getProperty("benchmark.nsec", "")}\"",
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
        testInstrumentationRunnerArguments["androidx.benchmark.killExistingPerfettoRecordings"] = "false"
    }

    buildTypes {
        create("benchmark") {
            isDebuggable = true
            signingConfig = getByName("debug").signingConfig
            matchingFallbacks += listOf("release")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    flavorDimensions += listOf("distribution")
    productFlavors {
        create("google") { dimension = "distribution" }
        create("aosp") { dimension = "distribution" }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}

androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark"
    }
}

tasks.register<Exec>("stopPerfetto") {
    val adb = "${android.sdkDirectory}/platform-tools/adb"
    commandLine(adb, "shell", "killall", "perfetto")

    isIgnoreExitValue = true
    description = "Stops every active Perfetto recording on target device"
    group = "benchmark"
}
