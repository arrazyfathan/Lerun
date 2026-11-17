plugins {
    alias(libs.plugins.lerun.android.feature)
}

android {
    namespace = "com.arrazyfathan.lerun.feature.home"
}

dependencies {
    implementation(libs.mpandroidchart)
    implementation(libs.haze)
    implementation(libs.haze.blur)
    implementation(libs.haze.materials)
    implementation(libs.timber)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
}
