package com.arrazyfathan.lerun.domain.repository

import com.arrazyfathan.lerun.domain.model.UserImage
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    fun observeImage(id: String): Flow<UserImage?>

    suspend fun upsertImage(userImage: UserImage)
}
