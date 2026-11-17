package com.arrazyfathan.lerun.domain.model

data class UserSettings(
    val userName: String = "",
    val weight: String = "0",
    val weightInKg: Int = 0,
    val themeMode: Int = THEME_LIGHT,
) {
    val isProfileCompleted: Boolean
        get() = weightInKg > 0 && userName.isNotBlank()

    val isDarkTheme: Boolean
        get() = themeMode != THEME_LIGHT

    companion object {
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2
    }
}
