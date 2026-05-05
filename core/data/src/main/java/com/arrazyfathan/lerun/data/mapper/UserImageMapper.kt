package com.arrazyfathan.lerun.data.mapper

import com.arrazyfathan.lerun.data.local.db.UserImageEntity
import com.arrazyfathan.lerun.domain.model.UserImage


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