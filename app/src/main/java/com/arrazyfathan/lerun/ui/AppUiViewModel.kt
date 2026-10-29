package com.arrazyfathan.lerun.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.lerun.domain.usecase.ObserveUserSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Immutable
data class AppUiState(
    val isDarkTheme: Boolean = true,
    val isProfileCompleted: Boolean = false,
)

@HiltViewModel
class AppUiViewModel @Inject constructor(
    observeUserSettingsUseCase: ObserveUserSettingsUseCase,
) : ViewModel() {

    private val initialSettings = observeUserSettingsUseCase.current()

    private val _state = MutableStateFlow(
        AppUiState(
            isDarkTheme = initialSettings.isDarkTheme,
            isProfileCompleted = initialSettings.isProfileCompleted,
        ),
    )
    val state = _state.asStateFlow()

    init {
        observeUserSettingsUseCase()
            .onEach { settings ->
                _state.update {
                    AppUiState(
                        isDarkTheme = settings.isDarkTheme,
                        isProfileCompleted = settings.isProfileCompleted,
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
