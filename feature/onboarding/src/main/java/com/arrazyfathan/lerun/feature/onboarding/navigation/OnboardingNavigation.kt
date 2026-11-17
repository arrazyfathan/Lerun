package com.arrazyfathan.lerun.feature.onboarding.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.lerun.feature.onboarding.SetupRoute
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingNavRoute : NavKey

fun EntryProviderScope<NavKey>.onboardingEntry(onNavigateToRun: () -> Unit) {
    entry<OnboardingNavRoute> {
        SetupRoute(
            onNavigateToRun = onNavigateToRun,
        )
    }
}
