import com.google.firebase.appdistribution.gradle.firebaseAppDistribution

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.navigation.safe.args)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.firebase.appdistribution)
    alias(libs.plugins.compose.compiler)
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    compileSdk = 34
    namespace = "com.arrazyfathan.lerun"

    defaultConfig {
        applicationId = "com.arrazyfathan.lerun"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            keyAlias = "KeyLerun"
            keyPassword = "release@lerun"
            storeFile = file("key_lerun.jks")
            storePassword = "release@lerun"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }

        getByName("debug") {
            val testerGroup = rootProject.file("testergroup.properties")
            firebaseAppDistribution {
                artifactType = "APK"
                releaseNotesFile = "notes.txt"
                testersFile = "tester.txt"
                groupsFile = "groups.txt"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs +=
            listOf(
                "-Xcontext-receivers",
            )
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation(fileTree("src/main/libs") { include("*.jar") })

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.ktx)
    implementation(libs.androidx.constrainLayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Material Design
    implementation(libs.material)

    // Room
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.paging)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Coroutine Lifecycle Scopes
    implementation(libs.bundles.androidx.lifecycle)

    // Navigation Components
    implementation(libs.bundles.androidx.navigation)

    // Glide
    implementation(libs.glide)
    ksp(libs.glide.compiler)

    // Google Maps Location Services
    implementation(libs.google.play.service.maps)
    implementation(libs.google.play.service.location)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    // Activity KTX for viewModels()
    implementation(libs.androidx.activity.ktx)

    // Easy Permissions
    implementation(libs.easypermissions)

    // Timber
    implementation(libs.timber)

    // MPAndroidChart
    implementation(libs.mpandroidchart)

    implementation(libs.androidx.lifecycle.service)

    // material dialog
    implementation(libs.core)
    implementation(libs.input)

    // Sticky Header
    implementation(libs.stickyscrollview)

    implementation(libs.blurview)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.perf.ktx)
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.messaging.ktx)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.play.services.oss.licenses)
    implementation(libs.android.image.cropper)

    implementation(libs.androidx.security.crypto.ktx)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

composeCompiler {
    enableStrongSkippingMode = true
}
