import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
}

private val xcfName = "PrimalDataWalletRepository"

kotlin {
    // Android target
    androidLibrary {
        namespace = "net.primal"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder {
        }
    }

    // iOS Target (minimum iOS 16 to match shared module)
    val xcfFramework = XCFramework(xcfName)

    fun KotlinNativeTarget.configureFramework(platformName: String) {
        binaries.framework {
            baseName = xcfName
            linkerOpts += listOf("-platform_version", platformName, "16.0", "16.0")
            xcfFramework.add(this)
        }
    }

    iosArm64 { configureFramework("ios") }
    iosSimulatorArm64 { configureFramework("ios-simulator") }

    // Source set declarations (https://kotlinlang.org/docs/multiplatform-hierarchy.html)
    sourceSets {
        commonMain {
            dependencies {
                // Internal
                implementation(project(":core:app-config"))
                implementation(project(":core:utils"))
                implementation(project(":core:networking-primal"))
                implementation(project(":core:networking-lightning"))

                implementation(project(":domain:nostr"))
                implementation(project(":domain:primal"))
                implementation(project(":domain:wallet"))

                implementation(project(":data:wallet:local"))
                implementation(project(":data:wallet:remote-primal"))
                implementation(project(":data:wallet:remote-nwc"))

                // Core
                implementation(libs.kotlinx.coroutines.core)

                // Paging
                implementation(libs.paging.common)

                // Serialization
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.io)

                // Logging
                implementation(libs.napier)

                implementation(libs.bignum)

                // Bitcoin
                implementation(libs.bitcoin.kmp)
                implementation(libs.breez.sdk.spark.kmp)

                // Crypto
                implementation(libs.korlibs.crypto)

                implementation(libs.kotlinx.datetime)
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
                implementation(project(":paging-runtime-ios"))
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

        val androidHostTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.androidx.test.core)
                implementation(libs.androidx.test.ext.junit)
                implementation(libs.robolectric)
            }
        }
    }
}
