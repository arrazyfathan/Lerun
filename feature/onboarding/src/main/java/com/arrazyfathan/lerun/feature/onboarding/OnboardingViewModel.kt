package com.arrazyfathan.lerun.presentation.onboarding

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.lerun.domain.usecase.ObserveUserSettingsUseCase
import com.arrazyfathan.lerun.domain.usecase.SaveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class OnboardingState(
    val name: String = "",
    val weight: String = "",
    val isProfileCompleted: Boolean = false,
)

sealed interface OnboardingAction {
    data object OnStarted : OnboardingAction
    data class OnNameChanged(val name: String) : OnboardingAction
    data class OnWeightChanged(val weight: String) : OnboardingAction
    data object OnContinueClick : OnboardingAction
}

sealed interface OnboardingEvent {
    data class ShowMessage(val message: String) : OnboardingEvent
    data object NavigateToRun : OnboardingEvent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val observeUserSettingsUseCase: ObserveUserSettingsUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(
        OnboardingState(
            isProfileCompleted = observeUserSettingsUseCase.current().isProfileCompleted,
        ),
    )
    val state = _state.asStateFlow()

    private val _events = Channel<OnboardingEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeUserSettingsUseCase().collect { settings ->
                _state.update {
                    it.copy(isProfileCompleted = settings.isProfileCompleted)
                }
            }
        }
    }

    fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.OnStarted -> {
                if (observeUserSettingsUseCase.current().isProfileCompleted) {
                    viewModelScope.launch {
                        _events.send(OnboardingEvent.NavigateToRun)
                    }
                }
            }

            is OnboardingAction.OnNameChanged -> {
                _state.update { it.copy(name = action.name) }
            }

            is OnboardingAction.OnWeightChanged -> {
                _state.update { it.copy(weight = action.weight) }
            }

            OnboardingAction.OnContinueClick -> {
                submitProfile()
            }
        }
    }

    private fun submitProfile() {
        val trimmedName = _state.value.name.trim()
        val trimmedWeight = _state.value.weight.trim()
        val parsedWeight = trimmedWeight.toFloatOrNull()?.toInt() ?: 0

        viewModelScope.launch {
            if (trimmedName.isBlank() || trimmedWeight.isBlank() || parsedWeight <= 0) {
                _events.send(OnboardingEvent.ShowMessage("Please enter all the fields"))
            } else {
                saveUserProfileUseCase(trimmedName, trimmedWeight)
                _events.send(OnboardingEvent.NavigateToRun)
            }
        }
    }

    fun isUserFilledUserProfile(): Boolean = observeUserSettingsUseCase.current().isProfileCompleted
}
