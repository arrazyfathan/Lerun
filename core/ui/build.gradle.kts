plugins {
    alias(libs.plugins.lerun.android.library)
    alias(libs.plugins.lerun.android.compose)
}

android {
    namespace = "com.arrazyfathan.lerun.core.ui"
}

dependencies {
    api(libs.androidx.appcompat)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.activity.compose)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.ui)
    api(libs.androidx.ui.graphics)
    api(libs.androidx.ui.tooling.preview)
    api(libs.androidx.material3)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.androidx.lifecycle.viewmodel.compose)
}
