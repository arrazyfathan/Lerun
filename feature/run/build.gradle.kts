plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.arrazyfathan.lerun.feature.run"
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
    implementation(libs.google.play.service.maps)
    implementation(libs.google.play.service.location)
    implementation(libs.easypermissions)
    implementation(libs.mpandroidchart)
    implementation(libs.haze)
    implementation(libs.haze.materials)
    implementation(libs.timber)
    implementation(libs.androidx.lifecycle.service)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    debugImplementation(libs.androidx.ui.tooling)
    ksp(libs.hilt.compiler)
}
