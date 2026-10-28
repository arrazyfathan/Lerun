package com.arrazyfathan.lerun.data.mapper

import com.arrazyfathan.lerun.data.local.db.UserImageEntity
import com.arrazyfathan.lerun.domain.model.UserImage

fun UserImageEntity.toDomain(): UserImage = UserImage(
    id = this.id,
    imageString = this.imageString.orEmpty(),
)

fun UserImage.toEntity(): UserImageEntity = UserImageEntity(
    id = this.id,
    imageString = this.imageString.orEmpty(),
)
