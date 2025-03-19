import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.touchlab.skie)
    alias(libs.plugins.ktorfit)
}

private val xcfName = "PrimalNetworking"

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
            dependencies {
                implementation(project(":core:utils"))
                implementation(project(":core:networking-http"))
                implementation(project(":domain:primal"))


                // Kotlin
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)

                // Data Store
                implementation(libs.datastore)

                // Networking && Serialization
                implementation(libs.okio)
                implementation(libs.ktorfit.light)
                implementation(libs.ktorfit.converters.response)
                implementation(libs.ktorfit.converters.call)

                // Logging
                implementation(libs.napier)

                // Swift interop
                implementation(libs.skie.configuration.annotations)
            }
        }

        androidMain {
            dependencies {
                // Kotlin
                implementation(libs.kotlinx.coroutines.android)

                // Networking
                implementation(libs.ktor.client.okhttp)
            }
        }

        iosMain {
            dependencies {
                // Networking
                implementation(libs.ktor.client.darwin)
            }
        }

        val desktopMain by getting
        desktopMain.dependencies {
            // Ktor
            implementation(libs.ktor.client.cio)
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.assertions.json)
                implementation(libs.kotlinx.coroutines.test)
//                implementation(libs.mockk)
            }
        }
    }

    // Opting in to the experimental @ObjCName annotation for native coroutines on iOS targets
    kotlin.sourceSets.all {
        languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
        languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}

tasks.register("assembleXCFramework") {
    dependsOn("assemble${xcfName}ReleaseXCFramework")
}

skie {
    build {
        produceDistributableFramework()
    }

    analytics {
        enabled.set(false)
    }
}
