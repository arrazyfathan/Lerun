package com.arrazyfathan.lerun.data.local.preferences

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.arrazyfathan.lerun.domain.UserSettingStorage
import com.arrazyfathan.lerun.domain.model.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingStorageImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : UserSettingStorage {

    private val _userSettings = MutableStateFlow(readCurrentUserSettings())

    override fun observeUserSettings(): Flow<UserSettings> = _userSettings.asStateFlow()

    override suspend fun setUserName(name: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences
                .edit()
                .putString(KEY_USERNAME, name)
                .commit()
            _userSettings.value = readCurrentUserSettings()
        }
    }

    override fun getUserWeight(): Int = sharedPreferences.getInt(KEY_WEIGHT, 0)

    override suspend fun setUserWeight(weight: Int) {
        withContext(Dispatchers.IO) {
            sharedPreferences
                .edit()
                .putInt(KEY_WEIGHT, weight)
                .commit()
            _userSettings.value = readCurrentUserSettings()
        }
    }

    override fun getUserThemes(): Int = sharedPreferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_NO)

    override suspend fun setUserThemes(theme: Int) {
        withContext(Dispatchers.IO) {
            sharedPreferences
                .edit()
                .putInt(KEY_THEME, theme)
                .commit()
            _userSettings.value = readCurrentUserSettings()
        }
    }

    override fun getUsername(): String? = sharedPreferences.getString(KEY_USERNAME, null)

    private fun readCurrentUserSettings(): UserSettings {
        val name = getUsername().orEmpty()
        val weight = getUserWeight()
        return UserSettings(
            userName = name,
            weight = weight.toString(),
            weightInKg = weight,
            themeMode = getUserThemes(),
        )
    }

    companion object {
        private const val KEY_USERNAME = "KEY_USERNAME"
        private const val KEY_WEIGHT = "KEY_WEIGHT"
        private const val KEY_THEME = "KEY_THEME"
    }
}
