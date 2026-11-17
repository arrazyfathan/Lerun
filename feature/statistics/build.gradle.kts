plugins {
    alias(libs.plugins.lerun.android.feature)
}

android {
    namespace = "com.arrazyfathan.lerun.feature.statistics"
}

dependencies {
    implementation(libs.mpandroidchart)
}
