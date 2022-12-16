buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.6.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.0-beta01")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.28-alpha")
        classpath("com.google.gms:google-services:4.3.13")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
