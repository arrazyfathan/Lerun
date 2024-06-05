package com.androiddevs.lerun.data.local.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ImageDao {

    @Upsert
    suspend fun insertImage(userImage: UserImageEntity)

    @Query("SELECT * FROM user_image WHERE id = :id")
    fun getImageById(id: String): LiveData<UserImageEntity?>
}
