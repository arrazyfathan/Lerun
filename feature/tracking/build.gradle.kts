plugins {
    alias(libs.plugins.lerun.android.feature)
}

android {
    namespace = "com.arrazyfathan.lerun.feature.tracking"
}

dependencies {
    implementation(libs.google.play.service.maps)
    implementation(libs.google.play.service.location)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.timber)
}
