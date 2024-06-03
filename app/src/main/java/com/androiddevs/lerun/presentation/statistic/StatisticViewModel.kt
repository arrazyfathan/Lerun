package com.androiddevs.lerun.presentation.statistic

import androidx.lifecycle.ViewModel
import com.androiddevs.lerun.domain.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel
    @Inject
    constructor(
        val repository: MainRepository,
    ) : ViewModel() {

        val totalTimeRun = repository.getTotalTimeInMillis()
        val totalDistance = repository.getTotalDistance()
        val totalCaloriesBurned = repository.getTotalCaloriesBurned()
        val totalAverageSpeed = repository.getTotalAvgSpeed()
        val runsSortedByDate = repository.getAllRunsSortedByDate()
    }