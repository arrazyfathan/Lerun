package com.arrazyfathan.lerun.domain.usecase

import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.domain.repository.RunRepository
import javax.inject.Inject

class InsertRunUseCase @Inject constructor(
    private val runRepository: RunRepository,
) {
    suspend operator fun invoke(run: Run) {
        runRepository.insertRun(run)
    }
}
