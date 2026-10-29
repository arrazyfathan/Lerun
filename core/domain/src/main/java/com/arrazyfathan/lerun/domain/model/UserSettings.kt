package com.arrazyfathan.lerun.domain.model

import androidx.appcompat.app.AppCompatDelegate

data class UserSettings(
    val userName: String = "",
    val weight: String = "0",
    val weightInKg: Int = 0,
    val themeMode: Int = AppCompatDelegate.MODE_NIGHT_NO,
) {
    val isProfileCompleted: Boolean
        get() = weightInKg > 0 && userName.isNotBlank()

    val isDarkTheme: Boolean
        get() = themeMode != AppCompatDelegate.MODE_NIGHT_NO
}
