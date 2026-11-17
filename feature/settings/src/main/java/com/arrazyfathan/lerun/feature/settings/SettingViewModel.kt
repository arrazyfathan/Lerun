package com.arrazyfathan.lerun.feature.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.lerun.domain.model.UserSettings
import com.arrazyfathan.lerun.domain.usecase.ObserveUserImageUseCase
import com.arrazyfathan.lerun.domain.usecase.ObserveUserSettingsUseCase
import com.arrazyfathan.lerun.domain.usecase.SaveUserImageUseCase
import com.arrazyfathan.lerun.domain.usecase.SaveUserProfileUseCase
import com.arrazyfathan.lerun.domain.usecase.SetUserThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class SettingState(
    val username: String = "",
    val weight: Int = 0,
    val profileImageBase64: String? = null,
    val isDarkTheme: Boolean = true,
    val showEditProfileSheet: Boolean = false,
    val showLicensesDialog: Boolean = false,
    val editName: String = "",
    val editWeight: String = "",
    val showValidationError: Boolean = false,
)

sealed interface SettingAction {
    data object ShowEditProfileSheet : SettingAction

    data object DismissEditProfileSheet : SettingAction

    data class OnEditNameChanged(
        val name: String,
    ) : SettingAction

    data class OnEditWeightChanged(
        val weight: String,
    ) : SettingAction

    data object SubmitEditProfile : SettingAction

    data class SaveProfile(
        val name: String,
        val weight: String,
    ) : SettingAction

    data class ChangeImage(
        val imageBase64: String,
    ) : SettingAction

    data class SetDarkTheme(
        val isDarkTheme: Boolean,
    ) : SettingAction

    data object OpenLicenses : SettingAction

    data object DismissLicenses : SettingAction
}

sealed interface SettingEvent {
    data class ShowMessage(
        val message: String,
    ) : SettingEvent
}

@HiltViewModel
class SettingViewModel
    @Inject
    constructor(
        observeUserSettingsUseCase: ObserveUserSettingsUseCase,
        observeUserImageUseCase: ObserveUserImageUseCase,
        private val saveUserProfileUseCase: SaveUserProfileUseCase,
        private val saveUserImageUseCase: SaveUserImageUseCase,
        private val setUserThemeUseCase: SetUserThemeUseCase,
    ) : ViewModel() {
        private val initialSettings = observeUserSettingsUseCase.current()

        private val _state =
            MutableStateFlow(
                SettingState(
                    username = initialSettings.userName,
                    weight = initialSettings.weightInKg,
                    isDarkTheme = initialSettings.isDarkTheme,
                    editName = initialSettings.userName,
                    editWeight = initialSettings.weightInKg.toString(),
                ),
            )
        val state = _state.asStateFlow()

        private val _event = Channel<SettingEvent>(Channel.BUFFERED)
        val event = _event.receiveAsFlow()

        init {
            observeUserSettingsUseCase()
                .onEach { settings ->
                    _state.update {
                        it.copy(
                            username = settings.userName,
                            weight = settings.weightInKg,
                            isDarkTheme = settings.isDarkTheme,
                        )
                    }
                }.launchIn(viewModelScope)

            observeUserImageUseCase()
                .onEach { userImage ->
                    _state.update { it.copy(profileImageBase64 = userImage?.imageString) }
                }.launchIn(viewModelScope)
        }

        fun onAction(action: SettingAction) {
            when (action) {
                SettingAction.ShowEditProfileSheet -> {
                    _state.update {
                        it.copy(
                            showEditProfileSheet = true,
                            editName = it.username,
                            editWeight = it.weight.toString(),
                            showValidationError = false,
                        )
                    }
                }

                SettingAction.DismissEditProfileSheet -> {
                    _state.update {
                        it.copy(
                            showEditProfileSheet = false,
                            showValidationError = false,
                        )
                    }
                }

                is SettingAction.OnEditNameChanged -> {
                    _state.update {
                        it.copy(
                            editName = action.name,
                            showValidationError = false,
                        )
                    }
                }

                is SettingAction.OnEditWeightChanged -> {
                    _state.update {
                        it.copy(
                            editWeight = action.weight,
                            showValidationError = false,
                        )
                    }
                }

                SettingAction.SubmitEditProfile -> {
                    val current = _state.value
                    val trimmedName = current.editName.trim()
                    val trimmedWeight = current.editWeight.trim()
                    if (trimmedName.isBlank() || trimmedWeight.isBlank()) {
                        _state.update { it.copy(showValidationError = true) }
                    } else {
                        saveUserProfile(trimmedName, trimmedWeight)
                    }
                }

                is SettingAction.SaveProfile -> {
                    val trimmedName = action.name.trim()
                    val trimmedWeight = action.weight.trim()
                    if (trimmedName.isBlank() || trimmedWeight.isBlank()) {
                        _state.update { it.copy(showValidationError = true) }
                    } else {
                        saveUserProfile(trimmedName, trimmedWeight)
                    }
                }

                is SettingAction.ChangeImage -> {
                    changeImage(action.imageBase64)
                }

                is SettingAction.SetDarkTheme -> {
                    setDarkTheme(action.isDarkTheme)
                }

                SettingAction.OpenLicenses -> {
                    _state.update { it.copy(showLicensesDialog = true) }
                }

                SettingAction.DismissLicenses -> {
                    _state.update { it.copy(showLicensesDialog = false) }
                }
            }
        }

        private fun setDarkTheme(isDarkTheme: Boolean) {
            val mode =
                if (isDarkTheme) {
                    UserSettings.THEME_DARK
                } else {
                    UserSettings.THEME_LIGHT
                }
            _state.update { it.copy(isDarkTheme = isDarkTheme) }
            viewModelScope.launch {
                setUserThemeUseCase(mode)
            }
        }

        private fun changeImage(imageString: String) {
            viewModelScope.launch {
                saveUserImageUseCase(imageString = imageString)
            }
        }

        private fun saveUserProfile(
            name: String,
            weight: String,
        ) {
            viewModelScope.launch {
                saveUserProfileUseCase(name = name, weight = weight)
                _state.update {
                    it.copy(
                        showEditProfileSheet = false,
                        showValidationError = false,
                    )
                }
                _event.send(SettingEvent.ShowMessage("Saved"))
            }
        }
    }
