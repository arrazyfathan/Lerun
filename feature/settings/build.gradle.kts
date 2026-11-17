plugins {
    alias(libs.plugins.lerun.android.feature)
}

android {
    namespace = "com.arrazyfathan.lerun.feature.settings"
}

dependencies {
    implementation(libs.timber)
}
