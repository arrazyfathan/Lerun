package com.arrazyfathan.lerun.domain.usecase

import com.arrazyfathan.lerun.domain.UserSettingStorage
import javax.inject.Inject

class SaveUserProfileUseCase @Inject constructor(
    private val userSettingStorage: UserSettingStorage,
) {
    suspend operator fun invoke(name: String, weight: String) {
        val parsedWeight = weight.toFloatOrNull()?.toInt() ?: 0
        userSettingStorage.setUserName(name.trim())
        userSettingStorage.setUserWeight(parsedWeight)
    }
}
