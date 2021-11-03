package com.androiddevs.lerun.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.androiddevs.lerun.repositories.MainRepository


class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel() {


}