package com.arrazyfathan.lerun.data.mapper

import com.arrazyfathan.lerun.data.local.db.Run
import com.arrazyfathan.lerun.domain.model.Run as DomainRun

fun Run.toDomain(): DomainRun {
    return DomainRun(
        id = id,
        img = img,
        title = title,
        timestamp = timestamp,
        avgSpeedInKMH = avgSpeedInKMH,
        distanceInMeters = distanceInMeters,
        timeInMillis = timeInMillis,
        caloriesBurned = caloriesBurned,
    )
}

fun DomainRun.toEntity(): Run {
    return Run(
        img = img,
        title = title,
        timestamp = timestamp,
        avgSpeedInKMH = avgSpeedInKMH,
        distanceInMeters = distanceInMeters,
        timeInMillis = timeInMillis,
        caloriesBurned = caloriesBurned,
    ).also { it.id = id }
}
