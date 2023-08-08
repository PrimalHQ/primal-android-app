import java.util.Properties

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    kotlin("kapt")
    kotlin("plugin.serialization")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val configProperties by lazy {
    val configFile = File("config.properties")
    if (configFile.exists()) {
        Properties().apply { load(configFile.reader()) }
    } else {
        null
    }
}

android {
    namespace = "net.primal.android"
    compileSdk = 33

    defaultConfig {
        applicationId = "net.primal.android"
        minSdk = 26
        targetSdk = 33
        versionCode = 27
        versionName = "0.14.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

        buildConfigField(
            type = "String",
            name = "LOCAL_STORAGE_KEY_ALIAS",
            value = "\"${configProperties?.getProperty("localStorage.keyAlias", "")}\""
        )
    }

    signingConfigs {
        val properties = configProperties
        val playStoreCertificateFile = properties?.getProperty("playStore.storeFile")
        if (properties != null && playStoreCertificateFile != null) {
            signingConfigs.create("playStore") {
                storeFile(File(playStoreCertificateFile))
                storePassword(properties.getProperty("playStore.storePassword"))
                keyAlias(properties.getProperty("playStore.keyAlias"))
                keyPassword(properties.getProperty("playStore.keyPassword"))
            }
        }

        val alternativeCertificateFile = properties?.getProperty("alternative.storeFile")
        if (properties != null && alternativeCertificateFile != null) {
            signingConfigs.create("alternative") {
                storeFile(File(alternativeCertificateFile))
                storePassword(properties.getProperty("alternative.storePassword"))
                keyAlias(properties.getProperty("alternative.keyAlias"))
                keyPassword(properties.getProperty("alternative.keyPassword"))
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        create("releasePlayStore") {
            initWith(getByName("release"))
            signingConfig = try {
                signingConfigs.getByName("playStore")
            } catch (error: UnknownDomainObjectException) {
                signingConfigs.getByName("debug")
            }
        }

        create("releaseAlternative") {
            initWith(getByName("release"))
            signingConfig = try {
                signingConfigs.getByName("alternative")
            } catch (error: UnknownDomainObjectException) {
                signingConfigs.getByName("debug")
            }
        }
    }

    sourceSets {
        named("releasePlayStore") {
            java.srcDirs("src/release/kotlin")
            res.srcDirs("src/release/res")
        }

        named("releaseAlternative") {
            java.srcDirs("src/release/kotlin")
            res.srcDirs("src/release/res")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    hilt {
        enableAggregatingTask = true
    }

    lint {
        checkDependencies = true
        checkTestSources = true
        checkReleaseBuilds = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"

            // JUnit
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    runtimeOnly(libs.androidx.appcompat)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.animation)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

    implementation(libs.androidx.material.icons.extended)

    implementation(libs.compose.placeholdermaterial)

    implementation(libs.constraintlayout)
    implementation(libs.compose.constraintlayout)

    implementation(libs.navigation.material)

    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)
    implementation(libs.paging.room)

    ksp(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)

    kapt(libs.bundles.hilt.kapt)
    implementation(libs.bundles.hilt)

    implementation(libs.datastore)
    implementation(libs.datastore.preferences)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.serialization.converter)

    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    implementation(libs.lottie.compose)

    implementation(libs.timber)

    implementation(libs.secp256k1.kmp.jvm)
    implementation(libs.secp256k1.kmp.jni.android)
    testImplementation(libs.secp256k1.kmp.jni.jvm)
    implementation(libs.spongycastle.core)
    implementation(libs.androidx.security.crypto)

    implementation(libs.url.detector)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.junit.android.runner)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.espresso.core)
    testImplementation(libs.mockk)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.assertions.json)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.room.testing)
    testImplementation(libs.okhttp.mockwebserver)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.junit.android.runner)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.kotest.assertions.core)
    androidTestImplementation(libs.kotest.assertions.json)
    androidTestImplementation(libs.mockk.android)
}
