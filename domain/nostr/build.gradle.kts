import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
}

private val xcfName = "NostrDomain"

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
                implementation(project(":core:utils"))

                // Kotlin
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)

                // Cryptography
//                implementation(libs.lightning.kmp)
                implementation(libs.bitcoin.kmp)
                implementation(libs.secp256k1.kmp)

                implementation(libs.ktor.io)
            }
        }

        androidMain {
            dependencies {
                // Kotlin
                implementation(libs.kotlinx.coroutines.android)

                // Cryptography
                implementation(libs.secp256k1.kmp.jni.android)
            }
        }

        iosMain {
            dependencies {
            }
        }

        val iosArm64Main by getting {
            dependencies {
                // Cryptography
//                implementation(libs.lightning.kmp.iosarm64)
                implementation(libs.bitcoin.kmp.iosarm64)
                implementation(libs.secp256k1.kmp.iosarm64)
            }
        }
        val iosSimulatorArm64Main by getting {
            dependencies {
                // Cryptography
//                implementation(libs.lightning.kmp.iossimulatorarm64)
                implementation(libs.bitcoin.kmp.iossimulatorarm64)
                implementation(libs.secp256k1.kmp.iossimulatorarm64)
            }
        }
        val iosX64Main by getting {
            dependencies {
                // Cryptography
//                implementation(libs.lightning.kmp.iosx64)
                implementation(libs.bitcoin.kmp.iosx64)
                implementation(libs.secp256k1.kmp.iosx64)
            }
        }

        val desktopMain by getting
        desktopMain.dependencies {
            // Cryptography
//                implementation(libs.lightning.kmp.jvm)
            implementation(libs.bitcoin.kmp.jvm)
            implementation(libs.secp256k1.kmp.jvm)
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
