@file:Suppress("ktlint:standard:max-line-length")

import co.touchlab.skie.configuration.DefaultArgumentInterop
import co.touchlab.skie.configuration.FlowInterop
import de.undercouch.gradle.tasks.download.Download
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.touchlab.skie)
    id("de.undercouch.download") version "5.6.0"
}

val breezSparkVersion = libs.versions.breez.spark.kmp.sdk.get()
val breezFrameworkDir = layout.buildDirectory.dir("breez-sdk-spark/$breezSparkVersion")

val downloadBreezFramework by tasks.registering(Download::class) {
    src(
        "https://github.com/breez/breez-sdk-spark-swift/releases/download/$breezSparkVersion/breez_sdk_sparkFFI.xcframework.zip",
    )
    dest(layout.buildDirectory.file("breez-sdk-spark/$breezSparkVersion/breez_sdk_sparkFFI.xcframework.zip"))
    overwrite(false)
}

val unzipBreezFramework by tasks.registering(Copy::class) {
    dependsOn(downloadBreezFramework)
    from(zipTree(layout.buildDirectory.file("breez-sdk-spark/$breezSparkVersion/breez_sdk_sparkFFI.xcframework.zip")))
    into(breezFrameworkDir)
}

private val xcfName = "PrimalShared"
private val xcfVersionName = "0.1.10"

// Shared dependencies exported to iOS
private val exportedDependencies = listOf(
    ":domain:nostr",
    ":domain:primal",
    ":domain:wallet",
    ":domain:account",
    ":core:networking-primal",
    ":core:networking-upload",
    ":core:networking-lightning",
    ":data:account:repository",
    ":data:caching:repository",
    ":data:wallet:repository",
    ":paging-runtime-ios",
)

kotlin {
    // Android target
    androidLibrary {
        namespace = "net.primal"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    // JVM Target
//    jvm("desktop")

    // iOS Target (minimum iOS 16)
    val xcfFramework = XCFramework(xcfName)

    val breezFrameworkSlices = mapOf(
        "ios" to "ios-arm64",
        "ios-simulator" to "ios-arm64_x86_64-simulator",
    )

    fun KotlinNativeTarget.configureFramework(platformName: String) {
        binaries.framework {
            baseName = xcfName
            isStatic = false
            freeCompilerArgs += listOf("-Xadd-light-debug=enable")
            linkerOpts += listOf("-platform_version", platformName, "16.0", "16.0")
            val sliceDir = breezFrameworkSlices[platformName] ?: error("Unknown platform: $platformName")
            linkerOpts += listOf("-F", breezFrameworkDir.get().asFile.resolve("breez_sdk_sparkFFI.xcframework/$sliceDir").absolutePath)
            xcfFramework.add(this)
            exportedDependencies.forEach { dep -> export(project(dep)) }
        }
    }

    iosArm64 { configureFramework("ios") }
    iosSimulatorArm64 { configureFramework("ios-simulator") }

    // Source set declarations (https://kotlinlang.org/docs/multiplatform-hierarchy.html)
    sourceSets {
        commonMain {
            dependencies {
                // Internal - exported to iOS
                exportedDependencies.forEach { api(project(it)) }

                // Core
                implementation(libs.kotlinx.coroutines.core)

                // Serialization
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.io)

                // Logging
                implementation(libs.napier)

                // Interop
                implementation(libs.skie.configuration.annotations)
            }
        }

        androidMain {
            dependencies {
                // Coroutines
                implementation(libs.kotlinx.coroutines.android)
            }
        }

        iosMain {
            dependencies {
            }
        }

//        val desktopMain by getting
//        desktopMain.dependencies {
//            // Add JVM-Desktop-specific dependencies here
//        }
    }

    // This tells the Kotlin/Native compiler to link against the system SQLite library
    // and ensures that NativeSQLiteDriver (used on iOS targets) can find libsqlite3 at
    // runtime without missing symbols.
    targets.withType<KotlinNativeTarget> {
        binaries.all {
            linkerOpts("-lsqlite3")
        }
    }

    // Opting in to the experimental @ObjCName annotation for native coroutines on iOS targets
    kotlin.sourceSets.all {
        languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
    }
}

tasks.configureEach {
    if (name.contains("link", ignoreCase = true) && name.contains("Ios", ignoreCase = true)) {
        dependsOn(unzipBreezFramework)
    }
}

tasks.register("assembleXCFramework") {
    dependsOn("assemble${xcfName}ReleaseXCFramework")
    doLast {
        // Patch MinimumOSVersion from 13.0 to 16.0 in all Info.plist files
        val xcframeworkDir = layout.buildDirectory.dir("XCFrameworks/release/$xcfName.xcframework").get().asFile
        xcframeworkDir.walkTopDown()
            .filter { it.name == "Info.plist" }
            .forEach { plist ->
                val content = plist.readText()
                if (content.contains("<key>MinimumOSVersion</key>")) {
                    val patched = content.replace(
                        Regex("<key>MinimumOSVersion</key>\\s*<string>\\d+\\.\\d+</string>"),
                        "<key>MinimumOSVersion</key>\n\t<string>16.0</string>",
                    )
                    plist.writeText(patched)
                    println("Patched MinimumOSVersion to 16.0 in ${plist.path}")
                }
            }
    }
}

tasks.register("compileTargets") {
    dependsOn("compileKotlinIosArm64", "compileAndroidMain")
}

skie {
    build {
        produceDistributableFramework()
    }

    features {
        enableFlowCombineConvertorPreview = true

        group {
            DefaultArgumentInterop.Enabled(false)
            FlowInterop.Enabled(true)
        }
    }

    analytics {
        enabled.set(false)
    }
}
