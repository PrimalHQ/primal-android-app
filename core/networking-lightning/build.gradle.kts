import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
}

private val xcfName = "PrimalCoreLightingNetwork"

kotlin {
    // Android target
    androidLibrary {
        namespace = "net.primal"
        compileSdk = 35
        minSdk = 26
    }

    // JVM Target
    jvm("desktop")

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
                implementation(project(":core:utils"))
                implementation(project(":core:networking-http"))
                implementation(project(":domain:nostr"))
                implementation(project(":domain:wallet"))

                implementation(libs.kotlinx.coroutines.core)

                implementation(libs.okio)
                implementation(libs.napier)

                // Serialization
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.io)

                // Ktorfit
                implementation(libs.ktorfit.light)
                implementation(libs.ktorfit.converters.response)
                implementation(libs.ktorfit.converters.call)

                implementation(libs.bignum)
            }
        }

        androidMain {
            dependencies {
                // Kotlin
                implementation(libs.kotlinx.coroutines.android)
            }
        }

        iosMain {
            dependencies {
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.assertions.json)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
            }
        }

        val desktopMain by getting
        desktopMain.dependencies {
        }
    }

    // Opting in to the experimental @ObjCName annotation for native coroutines on iOS targets
    kotlin.sourceSets.all {
        languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
    }
}
