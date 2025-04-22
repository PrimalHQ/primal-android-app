import java.util.*
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
}

val buildConfigGenerator by tasks.registering(Sync::class) {
    val releaseProperties = Properties().apply {
        val file = rootProject.file("release.properties")
        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }

    from(
        resources.text.fromString(
            """
            |package net.primal.core.utils
            |
            |object AndroidBuildConfig {
            |    const val APP_VERSION = "${releaseProperties.getProperty("version", "unknown")}"
            |}
            |
            """.trimMargin(),
        ),
    ) {
        rename { "AndroidBuildConfig.kt" }
        into("net/primal/core/utils")
    }

    into(layout.buildDirectory.dir("generated-src/kotlin/"))
}

private val xcfName = "PrimalCoreUtils"

kotlin {
    // Android target
    androidLibrary {
        namespace = "net.primal"
        compileSdk = 35
        minSdk = 26
        withHostTestBuilder {}
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
            kotlin.srcDir(
                buildConfigGenerator.map { it.destinationDir },
            )

            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.http)

                implementation(libs.napier)
                implementation(libs.bignum)

                // Cryptography
                implementation(libs.bitcoin.kmp)
                implementation(libs.secp256k1.kmp)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
            }
        }

        iosMain {
            dependencies {
            }
        }

        val desktopMain by getting
        desktopMain.dependencies {
        }

        getByName("androidHostTest") {
            dependencies {
                implementation(libs.junit)
                implementation(libs.androidx.test.core)
                implementation(libs.androidx.test.runner)
                implementation(libs.androidx.test.ext.junit)
                implementation(libs.androidx.test.ext.junit.ktx)
                implementation(libs.androidx.arch.core.testing)
                implementation(libs.mockk)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.assertions.json)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.assertions.json)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
