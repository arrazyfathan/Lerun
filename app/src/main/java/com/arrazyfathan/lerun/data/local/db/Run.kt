package com.arrazyfathan.lerun.data.local.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "running_table")
data class Run(
    var img: Bitmap? = null,
    var title: String = "",
    var timestamp: Long = 0L,
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L,
    var caloriesBurned: Int = 0,
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
