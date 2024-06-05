package com.androiddevs.lerun.data.mapper

import com.androiddevs.lerun.data.local.db.UserImageEntity
import com.androiddevs.lerun.domain.model.UserImage


fun UserImageEntity.toDomain(): UserImage {
    return UserImage(
        id = this.id,
        imageString = this.imageString.orEmpty()
    )
}


fun UserImage.toEntity(): UserImageEntity {
    return UserImageEntity(
        id = this.id,
        imageString = this.imageString.orEmpty()
    )
}