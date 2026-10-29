package com.arrazyfathan.lerun.domain.usecase

import com.arrazyfathan.lerun.domain.UserSettingStorage
import com.arrazyfathan.lerun.domain.model.UserImage
import com.arrazyfathan.lerun.domain.repository.ImageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveUserImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val userSettingStorage: UserSettingStorage,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<UserImage?> = userSettingStorage
        .observeUserSettings()
        .map { it.userName }
        .distinctUntilChanged()
        .flatMapLatest { userName ->
            imageRepository.observeImage(userName)
        }
}
