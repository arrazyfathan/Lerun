package com.arrazyfathan.lerun.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.arrazyfathan.lerun.data.local.db.RunDAO
import com.arrazyfathan.lerun.data.mapper.toDomain
import com.arrazyfathan.lerun.data.mapper.toEntity
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.domain.repository.RunRepository
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

        override fun getAllRunsSortedByDate(): LiveData<List<Run>> = runDao.getAllRunsSortedByDate().mapRuns()

        override fun getAllRunsSortedByDateAsc(): LiveData<List<Run>> = runDao.getAllRunsSortedByDateAscending().mapRuns()

        override fun getAllRunsSortedByDistance(): LiveData<List<Run>> = runDao.getAllRunsSortedByDistance().mapRuns()

        override fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>> = runDao.getAllRunsSortedByTimeInMillis().mapRuns()

        override fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>> = runDao.getAllRunsSortedByAvgSpeed().mapRuns()

        override fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>> = runDao.getAllRunsSortedByCaloriesBurned().mapRuns()

        override fun getTotalAvgSpeed(): LiveData<Float> = runDao.getTotalAvgSpeed()

        override fun getTotalDistance(): LiveData<Int> = runDao.getTotalDistance()

        override fun getTotalCaloriesBurned(): LiveData<Int> = runDao.getTotalCaloriesBurned()

        override fun getTotalTimeInMillis(): LiveData<Long> = runDao.getTotalTimeInMillis()

        private fun LiveData<List<com.arrazyfathan.lerun.data.local.db.Run>>.mapRuns(): LiveData<List<Run>> {
            return map { runs -> runs.map { it.toDomain() } }
        }
    }
