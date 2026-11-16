package com.arrazyfathan.lerun.feature.statistics

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.domain.usecase.ObserveRunSummaryUseCase
import com.arrazyfathan.lerun.utils.TrackingUtility
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.round

@Immutable
data class StatisticState(
    val totalTimeRun: Long = 0L,
    val totalDistance: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val totalAverageSpeed: Float = 0f,
    val runsSortedByDate: List<Run> = emptyList(),
) {
    val formattedTotalTime: String
        get() = TrackingUtility.getFormattedStopWatchTime(totalTimeRun)

    val formattedTotalDistance: String
        get() {
            val km = totalDistance / 1000f
            val rounded = round(km * 10f) / 10
            return "${rounded}km"
        }

    val formattedAverageSpeed: String
        get() {
            val rounded = round(totalAverageSpeed * 10f) / 10
            return "${rounded}km/h"
        }

    val formattedCalories: String
        get() = "${totalCaloriesBurned}kcal"
}

sealed interface StatisticAction {
    data object OnStarted : StatisticAction
}

sealed interface StatisticEvent

@HiltViewModel
class StatisticViewModel
    @Inject
    constructor(
        observeRunSummaryUseCase: ObserveRunSummaryUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(StatisticState())
        val state = _state.asStateFlow()

        private val _event = Channel<StatisticEvent>(Channel.BUFFERED)
        val event = _event.receiveAsFlow()

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
                }.launchIn(viewModelScope)
        }

        fun onAction(action: StatisticAction) {
            when (action) {
                StatisticAction.OnStarted -> Unit
            }
        }
    }
