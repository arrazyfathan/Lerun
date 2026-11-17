package com.arrazyfathan.lerun.data.repository

import com.arrazyfathan.lerun.data.local.db.RunDAO
import com.arrazyfathan.lerun.data.mapper.toDomain
import com.arrazyfathan.lerun.data.mapper.toEntity
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.domain.model.SortType
import com.arrazyfathan.lerun.domain.repository.RunRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunRepositoryImpl
    @Inject
    constructor(
        private val runDao: RunDAO,
    ) : RunRepository {
        override suspend fun insertRun(run: Run) {
            runDao.insertRun(run.toEntity())
        }

        override suspend fun deleteRun(run: Run) {
            runDao.deleteRun(run.toEntity())
        }

        override suspend fun getRunById(id: Int): Run? = runDao.getRunById(id)?.toDomain()

        override fun observeRunsSortedBy(sortType: SortType): Flow<List<Run>> =
            when (sortType) {
                SortType.DATE -> runDao.observeAllRunsSortedByDate()
                SortType.DATE_ASC -> runDao.observeAllRunsSortedByDateAscending()
                SortType.RUNNING_TIME -> runDao.observeAllRunsSortedByTimeInMillis()
                SortType.AVG_SPEED -> runDao.observeAllRunsSortedByAvgSpeed()
                SortType.DISTANCE -> runDao.observeAllRunsSortedByDistance()
                SortType.CALORIES_BURNED -> runDao.observeAllRunsSortedByCaloriesBurned()
            }.mapRunsFlow()

        override fun observeRunsSortedByDate(): Flow<List<Run>> = runDao.observeAllRunsSortedByDate().mapRunsFlow()

        override fun observeRunsSortedByDateAsc(): Flow<List<Run>> = runDao.observeAllRunsSortedByDateAscending().mapRunsFlow()

        override fun observeTotalAvgSpeed(): Flow<Float> = runDao.observeTotalAvgSpeed()

        override fun observeTotalDistance(): Flow<Int> = runDao.observeTotalDistance()

        override fun observeTotalCaloriesBurned(): Flow<Int> = runDao.observeTotalCaloriesBurned()

        override fun observeTotalTimeInMillis(): Flow<Long> = runDao.observeTotalTimeInMillis()

        private fun Flow<List<com.arrazyfathan.lerun.data.local.db.Run>>.mapRunsFlow(): Flow<List<Run>> =
            map { runs -> runs.map { it.toDomain() } }
    }
