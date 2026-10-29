package com.arrazyfathan.lerun.domain.usecase

import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.domain.repository.RunRepository
import javax.inject.Inject

class GetRunByIdUseCase @Inject constructor(
    private val runRepository: RunRepository,
) {
    suspend operator fun invoke(id: Int): Run? = runRepository.getRunById(id)
}
