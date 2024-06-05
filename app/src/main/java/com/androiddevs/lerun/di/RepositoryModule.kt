package com.androiddevs.lerun.di

import com.androiddevs.lerun.data.local.preferences.UserSettingStorageImpl
import com.androiddevs.lerun.data.repository.ImageRepositoryImpl
import com.androiddevs.lerun.domain.UserSettingStorage
import com.androiddevs.lerun.domain.repository.ImageRepository
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
}