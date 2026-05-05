package com.arrazyfathan.lerun.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.lerun.domain.UserSettingStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userSettingStorage: UserSettingStorage
) : ViewModel() {


    fun isUserFilledUserProfile(): Boolean {
        return (userSettingStorage.getUserWeight() != 0 && !userSettingStorage.getUsername()
            .isNullOrBlank())
    }

    fun setUserProfile(name: String, weight: String) {
        viewModelScope.launch {
            userSettingStorage.setUserName(name)
            userSettingStorage.setUserWeight(weight.toInt())
        }
    }
}