package com.arrazyfathan.lerun.domain.repository

import androidx.lifecycle.LiveData
import com.arrazyfathan.lerun.domain.model.UserImage
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    fun observeImage(id: String): Flow<UserImage?>
    fun getImage(id: String): LiveData<UserImage?>
    suspend fun upsertImage(userImage: UserImage)
}
