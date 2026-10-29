package com.arrazyfathan.lerun.domain.usecase

import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.domain.model.SortType
import com.arrazyfathan.lerun.domain.repository.RunRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRunsUseCase @Inject constructor(
    private val runRepository: RunRepository,
) {
    operator fun invoke(sortType: SortType): Flow<List<Run>> = runRepository.observeRunsSortedBy(sortType)
}
