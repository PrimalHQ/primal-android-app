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
        compileSdk = 35
        minSdk = 26
    }

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
                implementation(project(":core:networking-lightning"))

                implementation(project(":domain:nostr"))
                implementation(project(":domain:primal"))
                implementation(project(":domain:wallet"))

                implementation(project(":data:wallet:local"))
                implementation(project(":data:wallet:remote-primal"))
                implementation(project(":data:wallet:remote-nwc"))

                val tsunamiSdkDependencyProvider = libs.primal.tsunami.sdk.kmp
                val hasTsunamiSdkCompositeBuild = gradle.includedBuilds.any { it.name.contains("tsunami") }

                if (hasTsunamiSdkCompositeBuild) {
                    implementation(tsunamiSdkDependencyProvider)
                    println("✓️ Using composite build for tsunami sdk.")
                } else {
                    val isTsunamiSdkPubliclyAvailable = providers.provider {
                        try {
                            configurations.detachedConfiguration(
                                dependencies.create(tsunamiSdkDependencyProvider),
                            ).resolve()
                            true
                        } catch (e: Exception) {
                            false
                        }
                    }.get()

                    if (isTsunamiSdkPubliclyAvailable) {
                        implementation(tsunamiSdkDependencyProvider)
                        println("✓️ Using tsunami sdk maven artifact.")
                    } else {
                        implementation(project(":data:wallet:remote-tsunami"))
                        println("⚠️ Using stub for tsunami sdk.")
                    }
                }

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
