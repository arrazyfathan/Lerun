package com.arrazyfathan.lerun.domain.usecase

import com.arrazyfathan.lerun.domain.UserSettingStorage
import com.arrazyfathan.lerun.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserSettingsUseCase @Inject constructor(
    private val userSettingStorage: UserSettingStorage,
) {
    operator fun invoke(): Flow<UserSettings> = userSettingStorage.observeUserSettings()

    fun current(): UserSettings {
        val name = userSettingStorage.getUsername().orEmpty()
        val weightInKg = userSettingStorage.getUserWeight()
        return UserSettings(
            userName = name,
            weight = weightInKg.toString(),
            weightInKg = weightInKg,
            themeMode = userSettingStorage.getUserThemes(),
        )
    }
}
