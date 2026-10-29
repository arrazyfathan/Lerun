plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.arrazyfathan.lerun.feature.statistics"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:common"))
    implementation(project(":core:ui"))

    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.mpandroidchart)
    implementation(libs.hilt.android)
    debugImplementation(libs.androidx.ui.tooling)
    ksp(libs.hilt.compiler)
}
