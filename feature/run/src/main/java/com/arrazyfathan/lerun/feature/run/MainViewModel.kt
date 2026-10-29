package com.arrazyfathan.lerun.presentation.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.domain.model.SortType
import com.arrazyfathan.lerun.domain.usecase.GetRunByIdUseCase
import com.arrazyfathan.lerun.domain.usecase.InsertRunUseCase
import com.arrazyfathan.lerun.domain.usecase.ObserveRunSummaryUseCase
import com.arrazyfathan.lerun.domain.usecase.ObserveRunsUseCase
import com.arrazyfathan.lerun.domain.usecase.ObserveUserImageUseCase
import com.arrazyfathan.lerun.domain.usecase.ObserveUserSettingsUseCase
import com.arrazyfathan.lerun.domain.usecase.SaveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class RunState(
    val profileName: String = "",
    val profileWeight: Int = 0,
    val profileImageBase64: String? = null,
    val bannerTitle: String = "Try full access for free now",
    val runs: List<Run> = emptyList(),
    val runsSortedByDateAsc: List<Run> = emptyList(),
    val totalTimeInMillis: Long = 0L,
    val totalDistanceInMeters: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val totalAvgSpeedInKMH: Float = 0f,
    val sortType: SortType = SortType.DATE,
    val selectedRun: Run? = null,
    val isDarkTheme: Boolean = true,
)

sealed interface RunAction {
    data object OnStarted : RunAction
    data class OnBannerTitleUpdated(val title: String) : RunAction
    data class OnSortTypeChanged(val sortType: SortType) : RunAction
    data class OnSaveRun(val run: Run) : RunAction
    data class OnSelectRun(val run: Run) : RunAction
    data class OnLoadRunById(val runId: Int) : RunAction
}

sealed interface RunEvent {
    data object RunSaved : RunEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val observeRunsUseCase: ObserveRunsUseCase,
    private val observeRunSummaryUseCase: ObserveRunSummaryUseCase,
    private val getRunByIdUseCase: GetRunByIdUseCase,
    private val insertRunUseCase: InsertRunUseCase,
    private val observeUserSettingsUseCase: ObserveUserSettingsUseCase,
    private val observeUserImageUseCase: ObserveUserImageUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
) : ViewModel() {

    private val currentSettings = observeUserSettingsUseCase.current()

    private val _state = MutableStateFlow(
        RunState(
            profileName = currentSettings.userName,
            profileWeight = currentSettings.weightInKg,
            isDarkTheme = currentSettings.isDarkTheme,
        ),
    )
    val state = _state.asStateFlow()

    private val _events = Channel<RunEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        observeUserSettings()
        observeUserImage()
        observeRunSummary()
        observeSortedRuns()
    }

    fun onAction(action: RunAction) {
        when (action) {
            RunAction.OnStarted -> {
                val latestSettings = observeUserSettingsUseCase.current()
                _state.update {
                    it.copy(
                        profileName = latestSettings.userName,
                        profileWeight = latestSettings.weightInKg,
                    )
                }
            }

            is RunAction.OnBannerTitleUpdated -> {
                if (action.title.isNotBlank()) {
                    _state.update { it.copy(bannerTitle = action.title) }
                }
            }

            is RunAction.OnSortTypeChanged -> {
                _state.update { it.copy(sortType = action.sortType) }
            }

            is RunAction.OnSaveRun -> {
                viewModelScope.launch {
                    insertRunUseCase(action.run)
                    _events.send(RunEvent.RunSaved)
                }
            }

            is RunAction.OnSelectRun -> {
                _state.update { it.copy(selectedRun = action.run) }
            }

            is RunAction.OnLoadRunById -> {
                viewModelScope.launch {
                    val existing = _state.value.selectedRun?.takeIf { it.id == action.runId }
                        ?: _state.value.runs.firstOrNull { it.id == action.runId }
                        ?: getRunByIdUseCase(action.runId)
                    if (existing != null) {
                        _state.update { it.copy(selectedRun = existing) }
                    }
                }
            }
        }
    }

    private fun observeUserSettings() {
        viewModelScope.launch {
            observeUserSettingsUseCase().collect { settings ->
                _state.update {
                    it.copy(
                        profileName = settings.userName,
                        profileWeight = settings.weightInKg,
                        isDarkTheme = settings.isDarkTheme,
                    )
                }
            }
        }
    }

    private fun observeUserImage() {
        viewModelScope.launch {
            observeUserImageUseCase().collect { userImage ->
                _state.update {
                    it.copy(profileImageBase64 = userImage?.imageString)
                }
            }
        }
    }

    private fun observeRunSummary() {
        viewModelScope.launch {
            observeRunSummaryUseCase().collect { summary ->
                _state.update {
                    it.copy(
                        totalTimeInMillis = summary.totalTimeInMillis,
                        totalDistanceInMeters = summary.totalDistanceInMeters,
                        totalCaloriesBurned = summary.totalCaloriesBurned,
                        totalAvgSpeedInKMH = summary.totalAvgSpeedInKMH,
                        runsSortedByDateAsc = summary.runsSortedByDateAsc,
                    )
                }
            }
        }
    }

    private fun observeSortedRuns() {
        viewModelScope.launch {
            _state
                .map { it.sortType }
                .distinctUntilChanged()
                .flatMapLatest { sortType ->
                    observeRunsUseCase(sortType)
                }
                .collect { sortedRuns ->
                    _state.update { it.copy(runs = sortedRuns) }
                }
        }
    }

    fun getProfileWeight(): Int = _state.value.profileWeight

    fun insertRun(run: Run) {
        onAction(RunAction.OnSaveRun(run))
    }

    fun sortRuns(sortType: SortType) {
        onAction(RunAction.OnSortTypeChanged(sortType))
    }

    fun setUserProfile(name: String, weight: String) {
        viewModelScope.launch {
            saveUserProfileUseCase(name, weight)
        }
    }
}
