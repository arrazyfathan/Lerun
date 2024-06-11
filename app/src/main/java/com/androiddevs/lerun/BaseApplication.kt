package com.androiddevs.lerun

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.androiddevs.lerun.data.local.preferences.UserSettingStorageImpl
import com.androiddevs.lerun.domain.UserSettingStorage
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class BaseApplication : Application() {

    @Inject lateinit var userSettingStorage: UserSettingStorage

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        AppCompatDelegate.setDefaultNightMode(userSettingStorage.getUserThemes())
    }
}
