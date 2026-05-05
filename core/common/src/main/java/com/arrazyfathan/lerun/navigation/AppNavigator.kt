package com.arrazyfathan.lerun.navigation

import com.arrazyfathan.lerun.domain.model.Run

interface AppNavigator {
    fun openRun(clearOnboardingFromBackStack: Boolean = false)
    fun openTracking()
    fun openAllRuns()
    fun openRunDetail(run: Run)
    fun closeTrackingToRun()
}
