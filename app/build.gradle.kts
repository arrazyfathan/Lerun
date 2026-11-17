import com.google.firebase.appdistribution.gradle.firebaseAppDistribution

plugins {
    alias(libs.plugins.lerun.android.application)
    alias(libs.plugins.lerun.android.compose)
    alias(libs.plugins.lerun.android.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.firebase.appdistribution)
}

android {
    namespace = "com.arrazyfathan.lerun"

    defaultConfig {
        applicationId = "com.arrazyfathan.lerun"
        versionCode = 1
        versionName = "1.2.1"

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
            firebaseAppDistribution {
                artifactType = "APK"
                releaseNotesFile = "notes.txt"
                testersFile = "tester.txt"
                groupsFile = "groups.txt"
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(project(":feature:home"))
    implementation(project(":feature:tracking"))
    implementation(project(":feature:statistics"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:onboarding"))

    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Coroutine Lifecycle Scopes
    implementation(libs.bundles.androidx.lifecycle)

    // Navigation 3 & Serialization
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Google Maps Location Services (for ServiceModule)
    implementation(libs.google.play.service.location)

    // Hilt
    ksp(libs.androidx.hilt.compiler)

    // Activity KTX for viewModels()
    implementation(libs.androidx.activity.ktx)

    // Timber
    implementation(libs.timber)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.config)
    implementation(libs.firebase.messaging)
}
