package com.arrazyfathan.lerun.presentation.onboarding

import androidx.lifecycle.ViewModel
import com.arrazyfathan.lerun.domain.UserSettingStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userSettingStorage: UserSettingStorage,
) : ViewModel() {

    fun isUserFilledUserProfile(): Boolean = (
        userSettingStorage.getUserWeight() != 0 && !userSettingStorage.getUsername()
            .isNullOrBlank()
        )

    suspend fun setUserProfile(name: String, weight: String) {
        val parsedWeight = weight.toFloatOrNull()?.toInt() ?: 0
        userSettingStorage.setUserName(name)
        userSettingStorage.setUserWeight(parsedWeight)
    }
}
