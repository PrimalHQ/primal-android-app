import java.util.*

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.play.publishing)
    alias(libs.plugins.google.services)
}

val configProperties by lazy {
    val configFile = File("config.properties")
    if (configFile.exists()) {
        Properties().apply { load(configFile.reader()) }
    } else {
        null
    }
}

data class SigningConfigProperties(
    val storeName: String,
    val storeFile: File,
    val storePassword: String,
    val keyAlias: String,
    val keyAliasPassword: String,
)

fun extractSigningConfigProperties(storeName: String): SigningConfigProperties? {
    val properties = configProperties
    val storeFilePath = properties?.getProperty("$storeName.storeFile")
    if (properties == null || storeFilePath == null) return null

    val absoluteStoreFile = File(storeFilePath)
    val projectStoreFile = File(projectDir, storeFilePath)
    val storeFile = when {
        absoluteStoreFile.exists() -> absoluteStoreFile
        projectStoreFile.exists() -> projectStoreFile
        else -> throw IllegalArgumentException(
            "storeFile for $storeName can not be found " +
                "at $absoluteStoreFile or $projectStoreFile",
        )
    }

    return SigningConfigProperties(
        storeName = storeName,
        storeFile = storeFile,
        storePassword = properties.getProperty("$storeName.storePassword"),
        keyAlias = properties.getProperty("$storeName.keyAlias"),
        keyAliasPassword = properties.getProperty("$storeName.keyPassword"),
    )
}

val appVersionCode = 255
val appVersionName = "2.3.14"

tasks.register("generateReleaseProperties") {
    doLast {
        val file = File("${project.rootDir}/release.properties")
        file.writeText("version=$appVersionName")
    }
}

tasks.named("preBuild").configure {
    dependsOn("generateReleaseProperties")
}

android {
    namespace = "net.primal.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "net.primal.android"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            type = "String",
            name = "LOCAL_STORAGE_KEY_ALIAS",
            value = "\"${configProperties?.getProperty("localStorage.keyAlias", "")}\"",
        )

        buildConfigField(
            type = "boolean",
            name = "FEATURE_PRIMAL_CRASH_REPORTER",
            value = if (project.hasProperty("enablePrimalCrashReporter")) {
                project.properties["enablePrimalCrashReporter"] as String
            } else {
                "false"
            },
        )
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    signingConfigs {
        extractSigningConfigProperties("playStore")?.let {
            signingConfigs.create(it.storeName) {
                storeFile(it.storeFile)
                storePassword(it.storePassword)
                keyAlias(it.keyAlias)
                keyPassword(it.keyAliasPassword)
            }
        }

        extractSigningConfigProperties("alternative")?.let {
            signingConfigs.create(it.storeName) {
                storeFile(it.storeFile)
                storePassword(it.storePassword)
                keyAlias(it.keyAlias)
                keyPassword(it.keyAliasPassword)
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        create("playRelease") {
            initWith(getByName("release"))
            signingConfig = try {
                signingConfigs.getByName("playStore")
            } catch (_: UnknownDomainObjectException) {
                signingConfigs.getByName("debug")
            }
        }

        create("altRelease") {
            initWith(getByName("release"))
            signingConfig = try {
                signingConfigs.getByName("alternative")
            } catch (_: UnknownDomainObjectException) {
                signingConfigs.getByName("debug")
            }
        }
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }

    flavorDimensions.addAll(
        listOf("distribution"),
    )

    productFlavors {
        create("google") {
            dimension = "distribution"
        }

        create("aosp") {
            dimension = "distribution"
        }
    }

    sourceSets {
        named("playRelease") {
            java.srcDirs("src/release/kotlin")
            res.srcDirs("src/release/res")
        }

        named("altRelease") {
            java.srcDirs("src/release/kotlin")
            res.srcDirs("src/release/res")
        }

        named("benchmark") {
            java.srcDirs("src/release/kotlin")
            res.srcDirs("src/release/res")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    @Suppress("UnstableApiUsage")
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
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

    sourceSets {
        findByName("main")?.java?.srcDirs(project.file("src/main/kotlin"))
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation(project(":core:utils"))
    implementation(project(":core:app-config"))
    implementation(project(":core:networking-primal"))
    implementation(project(":core:networking-upload"))
    implementation(project(":core:networking-nwc"))
    implementation(project(":domain:nostr"))
    implementation(project(":domain:primal"))
    implementation(project(":domain:wallet"))
    implementation(project(":data:caching:remote"))
    implementation(project(":data:caching:repository"))

    implementation(libs.bignum)
    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.ktx)
    runtimeOnly(libs.androidx.appcompat)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.animation)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.placeholder.foundation)
    implementation(libs.compose.placeholder.material3)

    implementation(libs.compose.constraintlayout)
    implementation(libs.constraintlayout)

    implementation(libs.permissions.accompanist)

    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.emoji2.emojipicker)
    implementation(libs.androidx.webkit)

    implementation(libs.markwon.core)
    implementation(libs.markwon.image)
    implementation(libs.markwon.imagecoil)
    implementation(libs.markwon.inlineparser)
    implementation(libs.markwon.latex)
    implementation(libs.markwon.strikethrough)
    implementation(libs.markwon.tables)
    implementation(libs.markwon.tasklist)
    implementation(libs.markwon.html)
    implementation(libs.markwon.linkify)
    implementation(libs.markwon.simple)

    implementation(libs.navigation.material)

    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    ksp(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    implementation(libs.room.runtime)
    implementation(libs.sqlcipher.android)

    ksp(libs.bundles.hilt.compiler)
    implementation(libs.bundles.hilt)

    implementation(libs.datastore)
    implementation(libs.datastore.preferences)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.okio)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.scalars)
    implementation(libs.retrofit.serialization.converter)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.guava)

    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.coil.gif)
    implementation(libs.coil.video)
    implementation(libs.coil.network)
    implementation(libs.telephoto.zoomable.image)
    implementation(libs.telephoto.zoomable.peek.overlay)
    implementation(libs.telephoto.zoomable.image.coil)
    implementation(libs.media3.decoder)
    implementation(libs.media3.exoplayer.core)
    implementation(libs.media3.exoplayer.ui)
    implementation(libs.media3.exoplayer.ui.compose)
    implementation(libs.zoomimage.compose.coil3)

    implementation(libs.lottie.compose)
    implementation(libs.flippable)
    implementation(libs.reorderable)

    implementation(libs.timber)
    implementation(libs.napier)

    implementation(libs.lightning.kmp)
    implementation(libs.bitcoinj.core)
    implementation(libs.secp256k1.kmp.jvm)
    implementation(libs.secp256k1.kmp.jni.android)
    testImplementation(libs.secp256k1.kmp.jni.jvm)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.biometric)

    implementation(libs.url.detector)

    "googleImplementation"(libs.play.billing)
    "googleImplementation"(libs.play.billing.ktx)
    "googleImplementation"(platform(libs.firebase.bom))
    "googleImplementation"(libs.firebase.messaging)
    "googleImplementation"(libs.kotlinx.coroutines.play.services)
    "googleImplementation"(libs.mlkit.barcode.scanning)

    implementation(libs.qrcode.generator)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(files("$projectDir/libs/zbar.jar"))

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.test.ext.junit.ktx)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.espresso.core)
    testImplementation(libs.mockk)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.assertions.json)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.room.testing)
    testImplementation(libs.okhttp.mockwebserver)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.kotest.assertions.core)
    androidTestImplementation(libs.kotest.assertions.json)
    androidTestImplementation(libs.mockk.android)
}
