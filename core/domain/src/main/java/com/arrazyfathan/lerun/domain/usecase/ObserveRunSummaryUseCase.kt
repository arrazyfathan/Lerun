package com.arrazyfathan.lerun.domain.usecase

import com.arrazyfathan.lerun.domain.model.RunSummary
import com.arrazyfathan.lerun.domain.repository.RunRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveRunSummaryUseCase @Inject constructor(
    private val runRepository: RunRepository,
) {
    operator fun invoke(): Flow<RunSummary> = combine(
        runRepository.observeTotalTimeInMillis(),
        runRepository.observeTotalDistance(),
        runRepository.observeTotalCaloriesBurned(),
        runRepository.observeTotalAvgSpeed(),
        combine(
            runRepository.observeRunsSortedByDateAsc(),
            runRepository.observeRunsSortedByDate(),
        ) { asc, desc -> asc to desc },
    ) { totalTime, totalDistance, totalCalories, totalAvgSpeed, (runsAsc, runsDesc) ->
        RunSummary(
            totalTimeInMillis = totalTime,
            totalDistanceInMeters = totalDistance,
            totalCaloriesBurned = totalCalories,
            totalAvgSpeedInKMH = totalAvgSpeed,
            runsSortedByDateAsc = runsAsc,
            runsSortedByDate = runsDesc,
        )
    }
}
