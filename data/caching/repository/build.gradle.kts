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
    android {
        namespace = "net.primal"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    // JVM Target
    jvm("desktop")

    // iOS Target
    val xcfFramework = XCFramework(xcfName)
    val iosTargets = listOf(iosArm64(), iosSimulatorArm64())

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
                implementation(project(":core:networking-http"))
                implementation(project(":core:caching"))

                implementation(project(":domain:nostr"))
                implementation(project(":domain:primal"))

                implementation(project(":data:caching:local"))
                implementation(project(":data:caching:remote"))

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
                implementation(project(":paging-runtime-ios"))
            }
        }

        val desktopMain by getting
        desktopMain.dependencies {
            // Add JVM-Desktop-specific dependencies here
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.junit)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.assertions.json)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val desktopTest by getting
        desktopTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotest.assertions.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.mockk)
            implementation(libs.ktor.client.mock)
        }
    }
}

// Forward the optional caching-DB snapshot path so the opt-in DB benchmarks can run against a real
// database: -PprimalDbSnapshot=/abs/path/primal_database.db. Absent it they no-op, keeping CI safe.
tasks.withType<Test>().configureEach {
    val snapshot = (project.findProperty("primalDbSnapshot") as String?)
        ?: System.getProperty("primal.db.snapshot")
    if (snapshot != null) {
        systemProperty("primal.db.snapshot", snapshot)
    }
    // Opt-in for the self-contained persist-mapping benchmark (bundled fixture, no snapshot needed):
    // -PpersistBench. Absent it the benchmark no-ops, keeping CI cheap. When opted in, stream the
    // benchmark's stdout to the console and force the task to always re-run (never UP-TO-DATE).
    if (project.hasProperty("persistBench")) {
        systemProperty("primal.persist.bench", "1")
        outputs.upToDateWhen { false }
        testLogging {
            showStandardStreams = true
            events("passed", "skipped", "failed")
        }
    }
}
