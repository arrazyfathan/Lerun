package com.arrazyfathan.lerun.data.local.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {

    @Upsert
    suspend fun insertImage(userImage: UserImageEntity)

    @Query("SELECT * FROM user_image WHERE id = :id")
    fun observeImageById(id: String): Flow<UserImageEntity?>

    @Query("SELECT * FROM user_image WHERE id = :id")
    fun getImageById(id: String): LiveData<UserImageEntity?>
}
