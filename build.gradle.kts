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
}

buildscript {
    dependencies {
        //noinspection UseTomlInstead
        classpath("com.squareup:javapoet:1.13.0") // Required for dagger
    }
}

dependencies {
    detektPlugins(libs.ktlint.compose.rules)
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

    apply(plugin = "io.gitlab.arturbosch.detekt")
    plugins.withId("io.gitlab.arturbosch.detekt") {
        detekt {
            buildUponDefaultConfig = true
            allRules = false
            config.setFrom("$rootDir/detekt.yml")
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
