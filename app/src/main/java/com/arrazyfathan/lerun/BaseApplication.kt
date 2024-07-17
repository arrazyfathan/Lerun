package com.arrazyfathan.lerun

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.arrazyfathan.lerun.domain.UserSettingStorage
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
