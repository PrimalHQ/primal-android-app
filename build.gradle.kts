// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}

buildscript {
    dependencies {
        //noinspection UseTomlInstead
        classpath("com.squareup:javapoet:1.13.0") // Required for dagger
    }
}