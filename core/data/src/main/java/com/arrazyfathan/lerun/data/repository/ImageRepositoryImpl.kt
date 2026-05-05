package com.arrazyfathan.lerun.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.arrazyfathan.lerun.data.local.db.ImageDao
import com.arrazyfathan.lerun.data.mapper.toDomain
import com.arrazyfathan.lerun.data.mapper.toEntity
import com.arrazyfathan.lerun.domain.model.UserImage
import com.arrazyfathan.lerun.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ImageRepositoryImpl @Inject constructor(
    private val imageDao: ImageDao
) : ImageRepository {


    override fun getImage(id: String): LiveData<UserImage?> {
        return imageDao.getImageById(id).map { it?.toDomain() }
    }

    override suspend fun upsertImage(userImage: UserImage) {
        withContext(Dispatchers.IO) {
            imageDao.insertImage(userImage.toEntity())
        }
    }
}