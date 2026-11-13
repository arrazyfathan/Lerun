package com.arrazyfathan.lerun.feature.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.lerun.presentation.settings.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object SettingsNavRoute : NavKey

fun EntryProviderScope<NavKey>.settingsEntry() {
    entry<SettingsNavRoute> {
        SettingsRoute()
    }
}
