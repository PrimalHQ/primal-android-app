import co.touchlab.skie.configuration.DefaultArgumentInterop
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.jetpack.room)
    alias(libs.plugins.touchlab.skie)
}

private val xcfName = "PrimalDataLocalCaching"

kotlin {
    // Android target
    androidLibrary {
        namespace = "net.primal"
        compileSdk = 35
        minSdk = 26
    }

    // JVM Target
//    jvm("desktop")

    // iOS Target
    val xcfFramework = XCFramework(xcfName)
    val iosTargets = listOf(iosX64(), iosArm64(), iosSimulatorArm64())

    iosTargets.forEach {
        it.binaries.framework {
            baseName = xcfName
            xcfFramework.add(this)
        }
    }

    // Source set declarations (https://kotlinlang.org/docs/multiplatform-hierarchy.html)
    sourceSets {
        commonMain {
            dependencies {
                // Internal
                implementation(project(":core:utils"))
                implementation(project(":core:networking-primal"))
                implementation(project(":data:remote-caching"))
                implementation(project(":domain:nostr"))
                implementation(project(":domain:primal"))

                // Core
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)

                // Room
                api(libs.room.runtime)
                implementation(libs.room.paging)
                implementation(libs.jetpack.sqlite.framework)

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

                // Room
                api(libs.room.runtime.android)
                implementation(libs.jetpack.sqlite.framework.android)
            }
        }

        iosMain {
            dependencies {
                // Paging
                api(libs.cash.app.paging.runtime.uikit)
            }
        }

        val iosArm64Main by getting {
            dependencies {
                // SQLite
                implementation(libs.jetpack.sqlite.framework.iosarm64)
            }
        }
        val iosSimulatorArm64Main by getting {
            dependencies {
                // SQLite
                implementation(libs.jetpack.sqlite.framework.iossimulatorarm64)
            }
        }
        val iosX64Main by getting {
            dependencies {
                // SQLite
                implementation(libs.jetpack.sqlite.framework.iosx64)
            }
        }

//        val desktopMain by getting
//        desktopMain.dependencies {
//            // Add JVM-Desktop-specific dependencies here
//
//            // Room & SQLite
//            implementation(libs.jetpack.sqlite.bundled.jvm)
//        }

        commonTest {
            dependencies {
                implementation(libs.junit)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.assertions.json)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
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

tasks.register("assembleXCFramework") {
    dependsOn("assemble${xcfName}ReleaseXCFramework")
}

tasks.register("compileTargets") {
    dependsOn("compileKotlinIosArm64", "compileAndroidMain")
}

room {
    schemaDirectory("$projectDir/schemas")
}

skie {
    build {
        produceDistributableFramework()
    }

    features {
        enableFlowCombineConvertorPreview = true

        group {
            DefaultArgumentInterop.Enabled(false)
        }
    }

    analytics {
        enabled.set(false)
    }
}

dependencies {
    listOf(
        "kspAndroid",
//        "kspDesktop",
        "kspIosSimulatorArm64",
        "kspIosX64",
        "kspIosArm64",
    ).forEach {
        add(it, libs.room.compiler)
    }
}
