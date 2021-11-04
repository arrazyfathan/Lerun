package com.androiddevs.lerun.repositories

import com.androiddevs.lerun.db.Run
import com.androiddevs.lerun.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDao: RunDAO
) {


    suspend fun insert(run: Run) = runDao.insertRun(run)

    suspend fun delete(run: Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByMillis() = runDao.getAllRunsSortedByTimeInMillis()

    fun getALlRunsSortedByAverageSpeed() = runDao.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalAverageSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalTimeMillis() = runDao.getTotalTimeInMillis()


}