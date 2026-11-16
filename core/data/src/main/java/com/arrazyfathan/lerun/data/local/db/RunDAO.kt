package com.arrazyfathan.lerun.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM running_table WHERE id = :id LIMIT 1")
    suspend fun getRunById(id: Int): Run?

    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun observeAllRunsSortedByDate(): Flow<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timestamp ASC")
    fun observeAllRunsSortedByDateAscending(): Flow<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun observeAllRunsSortedByTimeInMillis(): Flow<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurned DESC")
    fun observeAllRunsSortedByCaloriesBurned(): Flow<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeedInKMH DESC")
    fun observeAllRunsSortedByAvgSpeed(): Flow<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distanceInMeters DESC")
    fun observeAllRunsSortedByDistance(): Flow<List<Run>>

    @Query("SELECT COALESCE(SUM(timeInMillis), 0) FROM running_table")
    fun observeTotalTimeInMillis(): Flow<Long>

    @Query("SELECT COALESCE(SUM(caloriesBurned), 0) FROM running_table")
    fun observeTotalCaloriesBurned(): Flow<Int>

    @Query("SELECT COALESCE(SUM(distanceInMeters), 0) FROM running_table")
    fun observeTotalDistance(): Flow<Int>

    @Query("SELECT COALESCE(AVG(avgSpeedInKMH), 0) FROM running_table")
    fun observeTotalAvgSpeed(): Flow<Float>
}
