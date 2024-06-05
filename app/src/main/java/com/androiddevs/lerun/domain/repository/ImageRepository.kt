package com.androiddevs.lerun.domain.repository

import androidx.lifecycle.LiveData
import com.androiddevs.lerun.domain.model.UserImage

interface ImageRepository {
    fun getImage(id: String): LiveData<UserImage?>
    suspend fun upsertImage(userImage: UserImage)
}