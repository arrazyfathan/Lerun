package com.androiddevs.lerun.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.lerun.domain.UserSettingStorage
import com.androiddevs.lerun.domain.model.UserImage
import com.androiddevs.lerun.domain.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingViewModel @Inject constructor(
    private val imageRepository: ImageRepository,
    private val userSettingStorage: UserSettingStorage
) : ViewModel() {

    fun userImage(): LiveData<UserImage?> {
        return imageRepository.getImage(userSettingStorage.getUsername()!!)
    }

    fun getTheme(): Int = userSettingStorage.getUserThemes()

    fun setTheme(theme: Int) {
        viewModelScope.launch {
            userSettingStorage.setUserThemes(theme)
        }
    }

    fun changeImage(imageString: String) {
        viewModelScope.launch {
            val userImage = UserImage(
                id = getUsername(),
                imageString = imageString
            )
            imageRepository.upsertImage(userImage)
        }
    }

    fun getUsername(): String {
        return userSettingStorage.getUsername().orEmpty()
    }
}