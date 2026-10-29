package com.arrazyfathan.lerun.domain.model

data class RunSummary(
    val totalTimeInMillis: Long = 0L,
    val totalDistanceInMeters: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val totalAvgSpeedInKMH: Float = 0f,
    val runsSortedByDateAsc: List<Run> = emptyList(),
    val runsSortedByDate: List<Run> = emptyList(),
)
