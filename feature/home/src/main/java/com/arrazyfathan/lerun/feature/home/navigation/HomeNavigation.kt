package com.arrazyfathan.lerun.feature.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.lerun.feature.home.allruns.AllRunRoute
import com.arrazyfathan.lerun.feature.home.detail.DetailRunRoute
import com.arrazyfathan.lerun.feature.home.home.HomeRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeNavRoute : NavKey

typealias RunNavRoute = HomeNavRoute

@Serializable
data object AllRunsNavRoute : NavKey

@Serializable
data class DetailRunNavRoute(
    val runId: Int,
) : NavKey

fun EntryProviderScope<NavKey>.homeEntries(
    onOpenAllRuns: () -> Unit,
    onOpenRunDetail: (Int) -> Unit,
) {
    entry<HomeNavRoute> {
        HomeRoute(
            onOpenAllRuns = onOpenAllRuns,
            onOpenRunDetail = { run ->
                run.id?.let(onOpenRunDetail)
            },
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
