package com.androiddevs.lerun.di

import com.androiddevs.lerun.data.local.preferences.UserSettingStorageImpl
import com.androiddevs.lerun.domain.UserSettingStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    fun provideLoggingSsoRepository(userSettingStorageImpl: UserSettingStorageImpl): UserSettingStorage
}