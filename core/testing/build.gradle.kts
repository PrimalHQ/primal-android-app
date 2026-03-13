plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    // Android target
    androidLibrary {
        namespace = "net.primal"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTestBuilder {}
    }

    // JVM Target
    jvm("desktop")

    // iOS Target
    listOf(iosArm64(), iosSimulatorArm64())

    // Source set declarations (https://kotlinlang.org/docs/multiplatform-hierarchy.html)
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core:utils"))
                implementation(project(":domain:nostr"))

                // Kotlin
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.test)

                // Data Store
                implementation(libs.datastore)
            }
        }

        androidMain {
            dependencies {
                // Testing
                implementation(libs.junit)
                implementation(libs.mockk)
            }
        }

        iosMain {
            dependencies {
            }
        }

        val desktopMain by getting
        desktopMain.dependencies {
        }
    }

    kotlin.sourceSets.all {
        languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}
