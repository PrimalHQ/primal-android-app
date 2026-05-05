import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint.wrapper.plugin)
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.android.test) apply false
}

buildscript {
    dependencies {
        //noinspection UseTomlInstead
        classpath("com.squareup:javapoet:1.13.0") // Required for dagger
    }
}

dependencies {
    ktlintRuleset(libs.ktlint.compose.rules)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    plugins.withId("org.jlleitschuh.gradle.ktlint") {

        tasks.withType<KtLintCheckTask>().configureEach {
            exclude {
                it.file.invariantSeparatorsPath.contains("/build/generated/ksp/")
            }
        }

        tasks.withType<KtLintFormatTask>().configureEach {
            exclude {
                it.file.invariantSeparatorsPath.contains("/build/generated/ksp/")
            }
        }
        configure<KtlintExtension> {
            version = "1.0.1"
            android = true
            verbose = true

            filter {
                exclude {
                    it.file.invariantSeparatorsPath.contains("/build/generated/ksp/")
                }
            }
        }
    }

    if (name != "detekt-rules") {
        apply(plugin = "io.gitlab.arturbosch.detekt")
        plugins.withId("io.gitlab.arturbosch.detekt") {
            val isKmpModule = file("src/commonMain").exists()
            detekt {
                buildUponDefaultConfig = true
                allRules = false
                config.setFrom("$rootDir/detekt.yml")
            }
            dependencies {
                "detektPlugins"(project(":detekt-rules"))
            }

            tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
                exclude { it.file.invariantSeparatorsPath.contains("/build/generated/") }
                // Gradle's task-input validator inspects detekt's source roots
                // statically and flags an undeclared read from KSP output dirs.
                // Establish ordering even though the file-filter above excludes them.
                mustRunAfter(tasks.matching { it.name.startsWith("ksp") })
                if (isKmpModule && name != "detekt") {
                    // KMP per-source-set tasks otherwise all share the extension's
                    // default baseline file and clobber each other. Give each its own,
                    // but only when the file actually exists (detekt fails if a configured
                    // baseline file is missing).
                    val sourceSet = name.removePrefix("detekt").replaceFirstChar { it.lowercase() }
                    val baselineFile = file("$projectDir/detekt-baseline-$sourceSet.xml")
                    if (baselineFile.exists()) {
                        baseline.set(baselineFile)
                    }
                }
            }

            tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
                exclude { it.file.invariantSeparatorsPath.contains("/build/generated/") }
                mustRunAfter(tasks.matching { it.name.startsWith("ksp") })
                if (isKmpModule && name != "detektBaseline") {
                    val sourceSet = name.removePrefix("detektBaseline").replaceFirstChar { it.lowercase() }
                    baseline.set(file("$projectDir/detekt-baseline-$sourceSet.xml"))
                }
            }

            if (isKmpModule) {
                // KMP modules have per-source-set Detekt tasks (detektDesktopMain,
                // detektAndroidMain, etc.) but the plain `detekt` task is NO-SOURCE
                // because it expects src/main/kotlin. Aggregate so `:module:detekt`
                // actually runs PrimalRuleSet against KMP source sets.
                afterEvaluate {
                    tasks.named("detekt").configure {
                        dependsOn(
                            tasks.withType<io.gitlab.arturbosch.detekt.Detekt>()
                                .matching { it.name != "detekt" },
                        )
                    }
                    tasks.named("detektBaseline").configure {
                        dependsOn(
                            tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>()
                                .matching { it.name != "detektBaseline" },
                        )
                    }
                }
            }
        }
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
            compilerOptions {
                optIn.addAll(
                    "kotlin.time.ExperimentalTime",
                    "kotlin.uuid.ExperimentalUuidApi",
                )
            }
        }
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.android") {
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension> {
            compilerOptions {
                optIn.addAll(
                    "kotlin.time.ExperimentalTime",
                    "kotlin.uuid.ExperimentalUuidApi",
                )
            }
        }
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
            compilerOptions {
                optIn.addAll(
                    "kotlin.time.ExperimentalTime",
                    "kotlin.uuid.ExperimentalUuidApi",
                )
            }
        }
    }

    afterEvaluate {
        val kspTaskName = "kspCommonMainKotlinMetadata"
        tasks.matching { it.name.startsWith("runKtlint") && it.name.contains("CommonMain") }
            .configureEach {
                val kspTask = tasks.findByName(kspTaskName)
                if (kspTask != null) {
                    dependsOn(kspTask)
                }
            }
    }
}
