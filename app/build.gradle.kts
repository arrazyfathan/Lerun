import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.firebase.appdistribution)
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    compileSdk = 37
    namespace = "com.arrazyfathan.lerun"

    defaultConfig {
        applicationId = "com.arrazyfathan.lerun"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.2.1"

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
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

kotlin {
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(project(":feature:run"))
    implementation(project(":feature:statistics"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:onboarding"))

    implementation(fileTree("src/main/libs") { include("*.jar") })
    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.ktx)
    implementation(libs.androidx.constrainLayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Material Design
    implementation(libs.material)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Coroutine Lifecycle Scopes
    implementation(libs.bundles.androidx.lifecycle)

    // Navigation Components
    implementation(libs.bundles.androidx.navigation)

    // Google Maps Location Services
    implementation(libs.google.play.service.location)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    // Activity KTX for viewModels()
    implementation(libs.androidx.activity.ktx)

    // Timber
    implementation(libs.timber)

    implementation(libs.androidx.lifecycle.service)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.perf.ktx)
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.messaging.ktx)

}
