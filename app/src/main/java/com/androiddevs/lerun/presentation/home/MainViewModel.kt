package com.androiddevs.lerun.presentation.home

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.lerun.data.local.db.Run
import com.androiddevs.lerun.domain.UserSettingStorage
import com.androiddevs.lerun.domain.repository.MainRepository
import com.androiddevs.lerun.utils.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val mainRepository: MainRepository,
    private val userSettingStorage: UserSettingStorage
) : ViewModel() {
    val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    val runsSortedByDateAsc = mainRepository.getAllRunsSortedByDateAsc()
    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    private val runsSortedByTimeInMillis = mainRepository.getAllRunsSortedByTimeInMillis()
    private val runsSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()

    val runs = MediatorLiveData<List<Run>>()

    fun getProfileName() = userSettingStorage.getUsername()
    fun getProfileWeight() = userSettingStorage.getUserWeight()

    var sortType = SortType.DATE

    init {
        runs.addSource(runsSortedByDate) { result ->
            if (sortType == SortType.DATE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByAvgSpeed) { result ->
            if (sortType == SortType.AVG_SPEED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByCaloriesBurned) { result ->
            if (sortType == SortType.CALORIES_BURNED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByDistance) { result ->
            if (sortType == SortType.DISTANCE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByTimeInMillis) { result ->
            if (sortType == SortType.RUNNING_TIME) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByDateAsc) { result ->
            if (sortType == SortType.DATE_ASC) {
                result?.let { runs.value = it }
            }
        }
    }

    fun isUserFilledUserProfile(): Boolean {
        return (userSettingStorage.getUserWeight() != 0 && !userSettingStorage.getUsername()
            .isNullOrBlank())
    }

    fun sortRuns(sortType: SortType) =
        when (sortType) {
            SortType.DATE -> runsSortedByDate.value?.let { runs.value = it }
            SortType.RUNNING_TIME -> runsSortedByTimeInMillis.value?.let { runs.value = it }
            SortType.AVG_SPEED -> runsSortedByAvgSpeed.value?.let { runs.value = it }
            SortType.DISTANCE -> runsSortedByDistance.value?.let { runs.value = it }
            SortType.CALORIES_BURNED -> runsSortedByCaloriesBurned.value?.let { runs.value = it }
            SortType.DATE_ASC -> runsSortedByDateAsc.value?.let { runs.value = it }
        }.also {
            this.sortType = sortType
        }

    fun insertRun(run: Run) =
        viewModelScope.launch {
            mainRepository.insertRun(run)
        }

    fun setUserProfile(name: String, weight: String) {
        viewModelScope.launch {
            userSettingStorage.setUserName(name)
            userSettingStorage.setUserWeight(weight.toInt())
        }
    }
}
