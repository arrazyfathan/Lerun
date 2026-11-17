package com.arrazyfathan.lerun.feature.home.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.domain.model.SortType
import com.arrazyfathan.lerun.domain.usecase.ObserveRunSummaryUseCase
import com.arrazyfathan.lerun.domain.usecase.ObserveRunsUseCase
import com.arrazyfathan.lerun.domain.usecase.ObserveUserImageUseCase
import com.arrazyfathan.lerun.domain.usecase.ObserveUserSettingsUseCase
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
data class HomeState(
    val profileName: String = "",
    val profileWeight: Int = 0,
    val profileImageBase64: String? = null,
    val bannerTitle: String = "Try full access for free now",
    val latestRuns: List<Run> = emptyList(),
    val runsSortedByDateAsc: List<Run> = emptyList(),
    val totalTimeInMillis: Long = 0L,
    val totalDistanceInMeters: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val totalAvgSpeedInKMH: Float = 0f,
    val selectedChartRun: Run? = null,
    val showPermissionDialog: Boolean = false,
    val isDarkTheme: Boolean = true,
)

sealed interface HomeAction {
    data object OnStarted : HomeAction

    data class OnBannerTitleUpdated(
        val title: String,
    ) : HomeAction

    data object OnPermissionDenied : HomeAction

    data object OnDismissPermissionDialog : HomeAction

    data object OnRetryPermissionRequest : HomeAction

    data object OnOpenAppSettings : HomeAction

    data class OnChartRunSelected(
        val run: Run?,
    ) : HomeAction
}

sealed interface HomeEvent {
    data object RequestLocationPermissions : HomeEvent

    data object OpenAppSettings : HomeEvent
}

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val observeRunsUseCase: ObserveRunsUseCase,
        private val observeRunSummaryUseCase: ObserveRunSummaryUseCase,
        private val observeUserSettingsUseCase: ObserveUserSettingsUseCase,
        private val observeUserImageUseCase: ObserveUserImageUseCase,
    ) : ViewModel() {
        private val currentSettings = observeUserSettingsUseCase.current()

        private val _state =
            MutableStateFlow(
                HomeState(
                    profileName = currentSettings.userName,
                    profileWeight = currentSettings.weightInKg,
                    isDarkTheme = currentSettings.isDarkTheme,
                ),
            )
        val state = _state.asStateFlow()

        private val _events = Channel<HomeEvent>(Channel.BUFFERED)
        val events = _events.receiveAsFlow()

        init {
            observeUserSettings()
            observeUserImage()
            observeRunSummary()
            observeLatestRuns()
        }

        fun onAction(action: HomeAction) {
            when (action) {
                HomeAction.OnStarted -> {
                    val latestSettings = observeUserSettingsUseCase.current()
                    _state.update {
                        it.copy(
                            profileName = latestSettings.userName,
                            profileWeight = latestSettings.weightInKg,
                        )
                    }
                }

                is HomeAction.OnBannerTitleUpdated -> {
                    if (action.title.isNotBlank()) {
                        _state.update { it.copy(bannerTitle = action.title) }
                    }
                }

                HomeAction.OnPermissionDenied -> {
                    _state.update { it.copy(showPermissionDialog = true) }
                }

                HomeAction.OnDismissPermissionDialog -> {
                    _state.update { it.copy(showPermissionDialog = false) }
                }

                HomeAction.OnRetryPermissionRequest -> {
                    _state.update { it.copy(showPermissionDialog = false) }
                    viewModelScope.launch {
                        _events.send(HomeEvent.RequestLocationPermissions)
                    }
                }

                HomeAction.OnOpenAppSettings -> {
                    _state.update { it.copy(showPermissionDialog = false) }
                    viewModelScope.launch {
                        _events.send(HomeEvent.OpenAppSettings)
                    }
                }

                is HomeAction.OnChartRunSelected -> {
                    _state.update { it.copy(selectedChartRun = action.run) }
                }
            }
        }

        private fun observeUserSettings() {
            observeUserSettingsUseCase()
                .onEach { settings ->
                    _state.update {
                        it.copy(
                            profileName = settings.userName,
                            profileWeight = settings.weightInKg,
                            isDarkTheme = settings.isDarkTheme,
                        )
                    }
                }.launchIn(viewModelScope)
        }

        private fun observeUserImage() {
            observeUserImageUseCase()
                .onEach { userImage ->
                    _state.update {
                        it.copy(profileImageBase64 = userImage?.imageString)
                    }
                }.launchIn(viewModelScope)
        }

        private fun observeRunSummary() {
            observeRunSummaryUseCase()
                .onEach { summary ->
                    _state.update {
                        it.copy(
                            totalTimeInMillis = summary.totalTimeInMillis,
                            totalDistanceInMeters = summary.totalDistanceInMeters,
                            totalCaloriesBurned = summary.totalCaloriesBurned,
                            totalAvgSpeedInKMH = summary.totalAvgSpeedInKMH,
                            runsSortedByDateAsc = summary.runsSortedByDateAsc,
                            selectedChartRun =
                                it.selectedChartRun?.let { selected ->
                                    summary.runsSortedByDateAsc.firstOrNull { run -> run.id == selected.id }
                                },
                        )
                    }
                }.launchIn(viewModelScope)
        }

        private fun observeLatestRuns() {
            observeRunsUseCase(SortType.DATE)
                .onEach { runs ->
                    _state.update { it.copy(latestRuns = runs.take(3)) }
                }.launchIn(viewModelScope)
        }
    }
