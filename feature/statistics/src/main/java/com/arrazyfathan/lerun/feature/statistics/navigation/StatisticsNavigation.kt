package com.arrazyfathan.lerun.feature.statistics.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.lerun.presentation.statistic.StatisticsRoute
import kotlinx.serialization.Serializable

@Serializable
data object StatisticsNavRoute : NavKey

fun EntryProviderScope<NavKey>.statisticsEntry() {
    entry<StatisticsNavRoute> {
        StatisticsRoute()
    }
}
