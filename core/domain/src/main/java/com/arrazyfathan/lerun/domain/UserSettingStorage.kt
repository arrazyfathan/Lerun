package com.arrazyfathan.lerun.domain

interface UserSettingStorage {
    fun getUsername(): String?
    suspend fun setUserName(name: String)
    fun getUserWeight(): Int
    suspend fun setUserWeight(weight: Int)
    fun getUserThemes(): Int
    suspend fun setUserThemes(theme: Int)
}