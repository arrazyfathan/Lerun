package com.arrazyfathan.lerun.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.arrazyfathan.lerun.data.local.db.RunningDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app: Context,
    ) = Room
        .databaseBuilder(
            app,
            RunningDatabase::class.java,
            RUNNING_DATABASE_NAME,
        ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideImageDao(db: RunningDatabase) = db.getUserImageDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(
        @ApplicationContext context: Context,
    ): SharedPreferences =
        context.getSharedPreferences(
            USER_SETTING_PREF_NAME,
            Context.MODE_PRIVATE,
        )

    private const val RUNNING_DATABASE_NAME = "running_db"
    private const val USER_SETTING_PREF_NAME = "user_setting_pref"
}
