package com.arrazyfathan.lerun.domain.repository

import androidx.lifecycle.LiveData
import com.arrazyfathan.lerun.domain.model.Run

interface RunRepository {
    suspend fun insertRun(run: Run)
    suspend fun deleteRun(run: Run)
    fun getAllRunsSortedByDate(): LiveData<List<Run>>
    fun getAllRunsSortedByDateAsc(): LiveData<List<Run>>
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>
    fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>>
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>
    fun getTotalAvgSpeed(): LiveData<Float>
    fun getTotalDistance(): LiveData<Int>
    fun getTotalCaloriesBurned(): LiveData<Int>
    fun getTotalTimeInMillis(): LiveData<Long>
}
