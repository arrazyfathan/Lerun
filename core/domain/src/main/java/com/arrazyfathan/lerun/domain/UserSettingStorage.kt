package com.arrazyfathan.lerun.domain

import com.arrazyfathan.lerun.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingStorage {
    fun observeUserSettings(): Flow<UserSettings>
    fun getUsername(): String?
    suspend fun setUserName(name: String)
    fun getUserWeight(): Int
    suspend fun setUserWeight(weight: Int)
    fun getUserThemes(): Int
    suspend fun setUserThemes(theme: Int)
}
