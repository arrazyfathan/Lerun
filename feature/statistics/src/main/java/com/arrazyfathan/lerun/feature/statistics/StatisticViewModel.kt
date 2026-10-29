package com.arrazyfathan.lerun.presentation.statistic

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.domain.usecase.ObserveRunSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Immutable
data class StatisticState(
    val totalTimeRun: Long = 0L,
    val totalDistance: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val totalAverageSpeed: Float = 0f,
    val runsSortedByDate: List<Run> = emptyList(),
)

@HiltViewModel
class StatisticViewModel @Inject constructor(
    observeRunSummaryUseCase: ObserveRunSummaryUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticState())
    val state = _state.asStateFlow()

    init {
        observeRunSummaryUseCase()
            .onEach { summary ->
                _state.update {
                    StatisticState(
                        totalTimeRun = summary.totalTimeInMillis,
                        totalDistance = summary.totalDistanceInMeters,
                        totalCaloriesBurned = summary.totalCaloriesBurned,
                        totalAverageSpeed = summary.totalAvgSpeedInKMH,
                        runsSortedByDate = summary.runsSortedByDate,
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
