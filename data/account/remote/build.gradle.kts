import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
}

private val xcfName = "PrimalDataAccountRemote"

kotlin {
    // Android target
    androidLibrary {
        namespace = "net.primal.data.account.remote"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
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
                // Internal
                implementation(project(":core:utils"))
                implementation(project(":core:networking-http"))
                implementation(project(":core:networking-primal"))
                implementation(project(":domain:nostr"))
                implementation(project(":domain:primal"))

                // Core
                implementation(libs.kotlinx.coroutines.core)

                // Serialization
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.io)

                // Ktorfit
                implementation(libs.ktorfit.light)
                implementation(libs.ktorfit.converters.response)
                implementation(libs.ktorfit.converters.call)

                // Logging
                implementation(libs.napier)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.junit)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.assertions.json)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        androidMain {
            dependencies {
                // Coroutines
                implementation(libs.kotlinx.coroutines.android)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.test.runner)
                implementation(libs.androidx.test.core)
                implementation(libs.androidx.test.ext.junit)
            }
        }

        iosMain {
            dependencies {
            }
        }
    }

    // Opting in to the experimental @ObjCName annotation for native coroutines on iOS targets
    kotlin.sourceSets.all {
        languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
    }
}
