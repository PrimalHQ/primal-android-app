import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
}

private val xcfName = "PrimalDataCachingRepository"

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
                // Internal
                implementation(project(":core:app-config"))
                implementation(project(":core:utils"))
                implementation(project(":core:networking-primal"))

                implementation(project(":domain:nostr"))
                implementation(project(":domain:primal"))

                implementation(project(":data:caching:local"))
                implementation(project(":data:caching:remote"))

                // Core
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)

                // Paging
                implementation(libs.paging.common)

                // Serialization
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.io)

                // Logging
                implementation(libs.napier)

                implementation(libs.bignum)
            }
        }

        androidMain {
            dependencies {
                // Coroutines
                implementation(libs.kotlinx.coroutines.android)

                // Paging
                implementation(libs.paging.runtime)
            }
        }

        iosMain {
            dependencies {
            }
        }

        val desktopMain by getting
        desktopMain.dependencies {
            // Add JVM-Desktop-specific dependencies here
        }

        commonTest {
            dependencies {
                implementation(libs.junit)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.assertions.json)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
