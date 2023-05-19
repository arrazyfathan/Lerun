import com.google.firebase.appdistribution.gradle.firebaseAppDistribution

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("com.google.firebase.appdistribution")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "com.androiddevs.lerun"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(fileTree("src/main/libs") { include("*.jar") })
    implementation(libs.kotlin)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.ktx)
    implementation(libs.androidx.constrainLayout)
    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")

    // Material Design
    implementation(libs.material)

    // Architectural Components
    // Room
    implementation(libs.bundles.room)
    kapt(libs.room.compiler)

    // Coroutines
    implementation(libs.bundles.corouitines)

    // Coroutine Lifecycle Scopes
    implementation(libs.bundles.androidx.lifecycle)

    // Navigation Components
    implementation(libs.bundles.androidx.navigation)

    // Glide
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    // Google Maps Location Services
    implementation(libs.bundles.location)

    // Dagger Core
    implementation("com.google.dagger:dagger:2.25.4")
    kapt("com.google.dagger:dagger-compiler:2.25.2")

    // Dagger Android
    api("com.google.dagger:dagger-android:2.23.2")
    api("com.google.dagger:dagger-android-support:2.23.2")
    kapt("com.google.dagger:dagger-android-processor:2.23.2")

    // Activity KTX for viewModels()
    implementation("androidx.activity:activity-ktx:1.1.0")

    // Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.28-alpha")
    kapt("com.google.dagger:hilt-android-compiler:2.28-alpha")

    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha01")
    kapt("androidx.hilt:hilt-compiler:1.0.0-alpha01")

    // Easy Permissions
    implementation("pub.devrel:easypermissions:3.0.0")

    // Timber
    implementation(libs.timber)

    // MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("android.arch.lifecycle:extensions:1.1.1")

    // material dialog
    implementation("com.afollestad.material-dialogs:core:3.3.0")
    implementation("com.afollestad.material-dialogs:input:3.3.0")

    // Sticky Header
    implementation("com.github.amarjain07:StickyScrollView:1.0.3")

    implementation("com.github.Dimezis:BlurView:version-2.0.3")

    implementation(platform("com.google.firebase:firebase-bom:31.1.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")
    implementation("com.google.firebase:firebase-config-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
}
