plugins {
    alias(libs.plugins.lerun.android.library)
    alias(libs.plugins.lerun.android.hilt)
}

android {
    namespace = "com.arrazyfathan.lerun.core.data"
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.androidx.ktx)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
