plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.arrazyfathan.lerun.core.domain"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.hilt.android)
}
