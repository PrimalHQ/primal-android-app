import co.touchlab.skie.configuration.DefaultArgumentInterop
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.jetpack.room)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.touchlab.skie)
}

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
    val xcfName = "PrimalShared"
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
                implementation(project(":domain:nostr"))
                implementation(project(":domain:primal"))

                // Core
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)

                // Koin
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
//                implementation(libs.koin.compose)
//                implementation(libs.koin.compose.viewmodel)
//                implementation(libs.koin.compose.viewmodel.navigation)

                // Room
                implementation(libs.room.runtime)
                implementation(libs.room.paging)
                implementation(libs.jetpack.sqlite.framework)

                // Data Store
                implementation(libs.datastore)
                implementation(libs.datastore.preferences)

                // Paging
                implementation(libs.paging.common)

                // Networking && Serialization
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.websockets)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.serialization.kotlinx.json)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.okio)
                implementation(libs.ktorfit.light)
                implementation(libs.ktorfit.converters.response)
                implementation(libs.ktorfit.converters.call)
                implementation(libs.ktorfit.converters.flow)

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

                // Koin
                implementation(libs.koin.android)
                implementation(libs.koin.androidx.compose)

                // Room
                implementation(libs.room.runtime.android)
                implementation(libs.jetpack.sqlite.framework.android)

                // Paging
                implementation(libs.paging.runtime)

                // Networking
                implementation(libs.ktor.client.okhttp)

                // Cryptography
//                implementation(libs.secp256k1.kmp.jni.android)
            }
        }

        iosMain {
            dependencies {
                // Networking
                implementation(libs.ktor.client.darwin)
            }
        }

        val iosArm64Main by getting {
            dependencies {
                // SQLite
                implementation(libs.jetpack.sqlite.framework.iosarm64)

                // Cryptography
//                implementation(libs.lightning.kmp.iosarm64)
//                implementation(libs.bitcoin.kmp.iosarm64)
//                implementation(libs.secp256k1.kmp.iosarm64)
            }
        }
        val iosSimulatorArm64Main by getting {
            dependencies {
                // SQLite
                implementation(libs.jetpack.sqlite.framework.iossimulatorarm64)

                // Cryptography
//                implementation(libs.lightning.kmp.iossimulatorarm64)
//                implementation(libs.bitcoin.kmp.iossimulatorarm64)
//                implementation(libs.secp256k1.kmp.iossimulatorarm64)
            }
        }
        val iosX64Main by getting {
            dependencies {
                // SQLite
                implementation(libs.jetpack.sqlite.framework.iosx64)

                // Cryptography
//                implementation(libs.lightning.kmp.iosx64)
//                implementation(libs.bitcoin.kmp.iosx64)
//                implementation(libs.secp256k1.kmp.iosx64)
            }
        }

//        val desktopMain by getting
//        desktopMain.dependencies {
//            // Add JVM-Desktop-specific dependencies here
//
//            // Room & SQLite
//            implementation(libs.jetpack.sqlite.bundled.jvm)
//
//            // Ktor
//            implementation(libs.ktor.client.cio)
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

    // Opting in to the experimental @ObjCName annotation for native coroutines on iOS targers
    kotlin.sourceSets.all {
        languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
    }
}

tasks.register("assembleXCFramework") {
    dependsOn("assemblePrimalSharedReleaseXCFramework")
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
