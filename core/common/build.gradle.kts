plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.arrazyfathan.lerun.core.common"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.google.play.service.maps)
    implementation(libs.easypermissions)
    implementation(libs.mpandroidchart)
}
