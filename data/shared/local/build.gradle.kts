import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
}

private val xcfName = "PrimalDataSharedLocal"

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

                // Core
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)

                // Cryptography
                implementation(libs.whyoleg.cryptography.core)

                // Room
                api(libs.room.runtime)
                api(libs.room.paging)

                // Serialization
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.io)
            }
        }

        androidMain {
            dependencies {
                // Coroutines
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.core.ktx)

                // Cryptography
                implementation(libs.whyoleg.cryptography.provider.jdk)
                implementation(libs.androidx.security.crypto)

                // Room
                api(libs.room.runtime.android)
                api(libs.jetpack.sqlite.framework.android)
            }
        }

        iosMain {
            dependencies {
                // Cryptography
                implementation(libs.whyoleg.cryptography.provider.apple)
            }
        }

        val iosArm64Main by getting {
            dependencies {
                // SQLite
                api(libs.jetpack.sqlite.framework.iosarm64)
            }
        }
        val iosSimulatorArm64Main by getting {
            dependencies {
                // SQLite
                api(libs.jetpack.sqlite.framework.iossimulatorarm64)
            }
        }
        val iosX64Main by getting {
            dependencies {
                // SQLite
                api(libs.jetpack.sqlite.framework.iosx64)
            }
        }

        val desktopMain by getting
        desktopMain.dependencies {
            // Add JVM-Desktop-specific dependencies here

            // Room & SQLite
            api(libs.jetpack.sqlite.bundled.jvm)
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

    // This tells the Kotlin/Native compiler to link against the system SQLite library
    // and ensures that NativeSQLiteDriver (used on iOS targets) can find libsqlite3 at
    // runtime without missing symbols.
    targets.withType<KotlinNativeTarget> {
        binaries.all {
            linkerOpts("-lsqlite3")
        }
    }
}

dependencies {
    listOf(
        "kspAndroid",
        "kspDesktop",
        "kspIosSimulatorArm64",
        "kspIosX64",
        "kspIosArm64",
    ).forEach {
        add(it, libs.room.compiler)
    }
}
