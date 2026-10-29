package com.arrazyfathan.lerun.domain.usecase

import com.arrazyfathan.lerun.domain.UserSettingStorage
import com.arrazyfathan.lerun.domain.model.UserImage
import com.arrazyfathan.lerun.domain.repository.ImageRepository
import javax.inject.Inject

class SaveUserImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val userSettingStorage: UserSettingStorage,
) {
    suspend operator fun invoke(imageString: String) {
        val currentUsername = userSettingStorage.getUsername().orEmpty()
        imageRepository.upsertImage(
            UserImage(
                id = currentUsername,
                imageString = imageString,
            ),
        )
    }
}
