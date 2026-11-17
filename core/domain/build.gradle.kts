plugins {
    alias(libs.plugins.lerun.android.library)
}

android {
    namespace = "com.arrazyfathan.lerun.core.domain"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.hilt.android)
}
