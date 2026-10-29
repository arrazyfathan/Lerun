package com.arrazyfathan.lerun.domain.usecase

import com.arrazyfathan.lerun.domain.UserSettingStorage
import javax.inject.Inject

class SetUserThemeUseCase @Inject constructor(
    private val userSettingStorage: UserSettingStorage,
) {
    suspend operator fun invoke(themeMode: Int) {
        userSettingStorage.setUserThemes(themeMode)
    }
}
