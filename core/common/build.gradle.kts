plugins {
    alias(libs.plugins.lerun.android.library)
}

android {
    namespace = "com.arrazyfathan.lerun.core.common"
}

dependencies {
    implementation(libs.androidx.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
