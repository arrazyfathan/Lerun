package com.arrazyfathan.lerun.domain.model

import android.graphics.Bitmap
import java.io.Serializable

data class Run(
    val id: Int? = null,
    val img: Bitmap? = null,
    val title: String = "",
    val timestamp: Long = 0L,
    val avgSpeedInKMH: Float = 0f,
    val distanceInMeters: Int = 0,
    val timeInMillis: Long = 0L,
    val caloriesBurned: Int = 0,
) : Serializable
