package com.arrazyfathan.lerun.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.lerun.feature.home.navigation.AllRunsNavRoute
import com.arrazyfathan.lerun.feature.home.navigation.DetailRunNavRoute
import com.arrazyfathan.lerun.feature.home.navigation.RunNavRoute
import com.arrazyfathan.lerun.feature.onboarding.navigation.OnboardingNavRoute
import com.arrazyfathan.lerun.feature.settings.navigation.SettingsNavRoute
import com.arrazyfathan.lerun.feature.tracking.navigation.TrackingNavRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationStateTest {
    private fun createNavigator(startRoute: NavKey): Pair<NavigationState, Navigator> {
        val topLevelRoutes = setOf<NavKey>(RunNavRoute, SettingsNavRoute)
        val backStacks =
            (listOf(startRoute) + topLevelRoutes).distinct().associateWith { route ->
                NavBackStack(route)
            }
        val state =
            NavigationState(
                startRoute = RunNavRoute,
                topLevelRoute = NavBackStack(startRoute),
                backStacks = backStacks,
                topLevelRoutes = topLevelRoutes,
            )
        return state to Navigator(state)
    }

    @Test
    fun `completing onboarding replaces onboarding root with run route`() {
        val (state, navigator) = createNavigator(startRoute = OnboardingNavRoute)

        assertEquals(OnboardingNavRoute, state.currentRoute)
        assertEquals(listOf(OnboardingNavRoute), state.stacksInUse)

        navigator.completeOnboarding()

        assertEquals(RunNavRoute, state.currentRoute)
        assertEquals(RunNavRoute, state.currentTopLevelRoute)
        assertEquals(listOf(RunNavRoute), state.stacksInUse)
        assertFalse(state.popTopLevel())
    }

    @Test
    fun `top level and hierarchical navigation preserves backstack behavior`() {
        val (state, navigator) = createNavigator(startRoute = RunNavRoute)

        navigator.navigate(AllRunsNavRoute)
        assertEquals(AllRunsNavRoute, state.currentRoute)

        navigator.navigate(DetailRunNavRoute(runId = 42))
        assertEquals(DetailRunNavRoute(runId = 42), state.currentRoute)

        navigator.navigate(SettingsNavRoute)
        assertEquals(SettingsNavRoute, state.currentRoute)
        assertEquals(listOf(RunNavRoute, SettingsNavRoute), state.stacksInUse)

        navigator.goBack()
        assertEquals(DetailRunNavRoute(runId = 42), state.currentRoute)

        navigator.goBack()
        assertEquals(AllRunsNavRoute, state.currentRoute)

        navigator.navigate(TrackingNavRoute)
        assertEquals(TrackingNavRoute, state.currentRoute)

        navigator.popToCurrentTopLevel()
        navigator.navigate(RunNavRoute)
        assertEquals(RunNavRoute, state.currentRoute)
        assertTrue(state.backStacks[RunNavRoute]?.size == 1)
    }
}
