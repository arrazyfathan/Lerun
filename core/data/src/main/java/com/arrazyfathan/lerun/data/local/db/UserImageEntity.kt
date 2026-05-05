package com.arrazyfathan.lerun.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_image")
data class UserImageEntity(
    @PrimaryKey(autoGenerate = false)
    var id: String,
    var imageString: String?
)
