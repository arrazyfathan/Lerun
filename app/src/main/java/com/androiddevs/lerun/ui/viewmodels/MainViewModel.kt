package com.androiddevs.lerun.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.lerun.db.Run
import com.androiddevs.lerun.repositories.MainRepository
import kotlinx.coroutines.launch


class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel() {


    // insert current run into database
    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insert(run)
    }


}