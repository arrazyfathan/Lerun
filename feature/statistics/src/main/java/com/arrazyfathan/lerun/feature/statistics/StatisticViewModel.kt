package com.arrazyfathan.lerun.presentation.statistic

import androidx.lifecycle.ViewModel
import com.arrazyfathan.lerun.domain.repository.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel
    @Inject
    constructor(
        val repository: RunRepository,
    ) : ViewModel() {

        val totalTimeRun = repository.getTotalTimeInMillis()
        val totalDistance = repository.getTotalDistance()
        val totalCaloriesBurned = repository.getTotalCaloriesBurned()
        val totalAverageSpeed = repository.getTotalAvgSpeed()
        val runsSortedByDate = repository.getAllRunsSortedByDate()
    }
