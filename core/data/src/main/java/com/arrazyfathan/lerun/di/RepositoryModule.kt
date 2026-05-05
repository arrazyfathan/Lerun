package com.arrazyfathan.lerun.di

import com.arrazyfathan.lerun.data.local.preferences.UserSettingStorageImpl
import com.arrazyfathan.lerun.data.repository.ImageRepositoryImpl
import com.arrazyfathan.lerun.data.repository.RunRepositoryImpl
import com.arrazyfathan.lerun.domain.UserSettingStorage
import com.arrazyfathan.lerun.domain.repository.ImageRepository
import com.arrazyfathan.lerun.domain.repository.RunRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    fun provideUserSettingStorageRepository(userSettingStorageImpl: UserSettingStorageImpl): UserSettingStorage

    @Binds
    fun provideImageRepository(imageRepositoryImpl: ImageRepositoryImpl): ImageRepository

    @Binds
    fun provideRunRepository(runRepositoryImpl: RunRepositoryImpl): RunRepository
}
