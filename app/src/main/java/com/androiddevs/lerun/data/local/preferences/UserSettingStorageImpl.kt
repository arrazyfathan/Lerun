package com.androiddevs.lerun.data.local.preferences

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.androiddevs.lerun.domain.UserSettingStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingStorageImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : UserSettingStorage {

    override suspend fun setUserName(name: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences
                .edit()
                .putString(KEY_USERNAME, name)
                .commit()
        }
    }

    override fun getUserWeight(): Int {
        return sharedPreferences.getInt(KEY_WEIGHT, 0)
    }

    override suspend fun setUserWeight(weight: Int) {
        withContext(Dispatchers.IO) {
            sharedPreferences
                .edit()
                .putInt(KEY_WEIGHT, weight)
                .commit()
        }
    }

    override fun getUserThemes(): Int {
        return sharedPreferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_NO)
    }

    override suspend fun setUserThemes(theme: Int) {
        withContext(Dispatchers.IO) {
            sharedPreferences
                .edit()
                .putInt(KEY_THEME, theme)
                .commit()
        }
    }

    override fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    companion object {
        private const val KEY_USERNAME = "KEY_USERNAME"
        private const val KEY_WEIGHT = "KEY_WEIGHT"
        private const val KEY_THEME = "KEY_THEME"
    }
}

