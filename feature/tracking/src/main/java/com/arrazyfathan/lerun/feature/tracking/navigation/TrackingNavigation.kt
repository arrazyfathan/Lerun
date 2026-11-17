package com.arrazyfathan.lerun.feature.tracking.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.lerun.feature.tracking.TrackingRoute
import kotlinx.serialization.Serializable

@Serializable
data object TrackingNavRoute : NavKey

fun EntryProviderScope<NavKey>.trackingEntry(onCloseTrackingToRun: () -> Unit) {
    entry<TrackingNavRoute> {
        TrackingRoute(
            onCloseTrackingToRun = onCloseTrackingToRun,
        )
    }
}
