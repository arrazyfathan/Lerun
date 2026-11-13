package com.arrazyfathan.lerun.feature.run.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.lerun.presentation.allrun.AllRunRoute
import com.arrazyfathan.lerun.presentation.detailrun.DetailRunRoute
import com.arrazyfathan.lerun.presentation.home.RunRoute
import com.arrazyfathan.lerun.presentation.run.TrackingRoute
import kotlinx.serialization.Serializable

@Serializable
data object RunNavRoute : NavKey

@Serializable
data object TrackingNavRoute : NavKey

@Serializable
data object AllRunsNavRoute : NavKey

@Serializable
data class DetailRunNavRoute(val runId: Int) : NavKey

fun EntryProviderScope<NavKey>.runEntries(
    onOpenAllRuns: () -> Unit,
    onOpenRunDetail: (Int) -> Unit,
    onCloseTrackingToRun: () -> Unit,
) {
    entry<RunNavRoute> {
        RunRoute(
            onOpenAllRuns = onOpenAllRuns,
            onOpenRunDetail = { run ->
                run.id?.let(onOpenRunDetail)
            },
        )
    }
    entry<TrackingNavRoute> {
        TrackingRoute(
            onCloseTrackingToRun = onCloseTrackingToRun,
        )
    }
    entry<AllRunsNavRoute> {
        AllRunRoute()
    }
    entry<DetailRunNavRoute> { route ->
        DetailRunRoute(
            runId = route.runId,
        )
    }
}
