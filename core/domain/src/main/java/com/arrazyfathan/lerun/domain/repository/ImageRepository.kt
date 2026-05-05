package com.arrazyfathan.lerun.domain.repository

import androidx.lifecycle.LiveData
import com.arrazyfathan.lerun.domain.model.UserImage

interface ImageRepository {
    fun getImage(id: String): LiveData<UserImage?>
    suspend fun upsertImage(userImage: UserImage)
}