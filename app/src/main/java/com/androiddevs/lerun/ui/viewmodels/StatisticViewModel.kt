package com.androiddevs.lerun.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.androiddevs.lerun.repositories.MainRepository

class StatisticViewModel @ViewModelInject constructor(
    val repository: MainRepository
) : ViewModel() {

    val totalTimeRun = repository.getTotalTimeInMillis()
    val totalDistance = repository.getTotalDistance()
    val totalCaloriesBurned = repository.getTotalCaloriesBurned()
    val totalAverageSpeed = repository.getTotalAvgSpeed()

    val runsSortedByDate = repository.getAllRunsSortedByDate()
}
