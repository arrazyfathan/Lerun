package com.arrazyfathan.lerun.domain.repository

import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.domain.model.SortType
import kotlinx.coroutines.flow.Flow

interface RunRepository {
    suspend fun insertRun(run: Run)

    suspend fun deleteRun(run: Run)

    suspend fun getRunById(id: Int): Run?

    fun observeRunsSortedBy(sortType: SortType): Flow<List<Run>>

    fun observeRunsSortedByDate(): Flow<List<Run>>

    fun observeRunsSortedByDateAsc(): Flow<List<Run>>

    fun observeTotalAvgSpeed(): Flow<Float>

    fun observeTotalDistance(): Flow<Int>

    fun observeTotalCaloriesBurned(): Flow<Int>

    fun observeTotalTimeInMillis(): Flow<Long>
}
