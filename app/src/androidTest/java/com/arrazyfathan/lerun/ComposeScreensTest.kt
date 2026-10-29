package com.arrazyfathan.lerun

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.domain.model.SortType
import com.arrazyfathan.lerun.navigation.LerunFloatingBottomBar
import com.arrazyfathan.lerun.navigation.Navigator
import com.arrazyfathan.lerun.navigation.Screen
import com.arrazyfathan.lerun.navigation.rememberNavigationState
import com.arrazyfathan.lerun.navigation.toEntries
import com.arrazyfathan.lerun.presentation.allrun.AllRunScreenContent
import com.arrazyfathan.lerun.presentation.detailrun.DetailRunScreen
import com.arrazyfathan.lerun.presentation.home.RunScreenContent
import com.arrazyfathan.lerun.presentation.onboarding.SetupScreenContent
import com.arrazyfathan.lerun.presentation.run.TrackingScreenContent
import com.arrazyfathan.lerun.presentation.settings.SettingsScreenContent
import com.arrazyfathan.lerun.presentation.statistic.StatisticsScreenContent
import com.arrazyfathan.lerun.ui.designsystem.LerunTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun setupScreen_displaysInputsAndContinueButton() {
        composeTestRule.setContent {
            LerunTheme(darkTheme = true) {
                SetupScreenContent(
                    snackbarHostState = remember { SnackbarHostState() },
                    onContinueClick = { _, _ -> },
                )
            }
        }
        composeTestRule.onNodeWithText("Your name :").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weight (Kg) :").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continue ->").assertIsDisplayed()
    }

    @Test
    fun runScreen_displaysStatsAndRecentActivities() {
        val sampleRun = Run(
            id = 1,
            title = "Morning 5K",
            timestamp = 1700000000000L,
            avgSpeedInKMH = 10.5f,
            distanceInMeters = 5000,
            timeInMillis = 1800000L,
            caloriesBurned = 350,
        )
        composeTestRule.setContent {
            LerunTheme(darkTheme = true) {
                RunScreenContent(
                    greeting = "Good Morning",
                    profileName = "Ar Razy",
                    profileImageBase64 = null,
                    bannerTitle = "Try full access for free now",
                    totalCalories = 350,
                    totalAvgSpeed = 10.5f,
                    totalDistanceMeters = 5000,
                    totalTimeMillis = 1800000L,
                    chartRuns = emptyList(),
                    latestRuns = listOf(sampleRun),
                    onOpenAllRuns = {},
                    onOpenRunDetail = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Good Morning").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ar Razy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Statistics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Morning 5K").assertIsDisplayed()
    }

    @Test
    fun trackingScreen_displaysTimerAndControls() {
        composeTestRule.setContent {
            LerunTheme(darkTheme = true) {
                TrackingScreenContent(
                    formattedTimer = "00:05:12:34",
                    isTracking = false,
                    hasStarted = true,
                    onToggleRun = {},
                    onFinishClick = {},
                    onCancelClick = {},
                    mapContent = { Box(modifier = Modifier.fillMaxSize()) },
                )
            }
        }
        composeTestRule.onNodeWithText("00:05:12:34").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
        composeTestRule.onNodeWithText("Finish").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel Run").assertIsDisplayed()
    }

    @Test
    fun trackingScreen_disablesStartButtonWhenMapNotReady() {
        composeTestRule.setContent {
            LerunTheme(darkTheme = true) {
                TrackingScreenContent(
                    formattedTimer = "00:00:00:00",
                    isTracking = false,
                    hasStarted = false,
                    isMapReady = false,
                    onToggleRun = {},
                    onFinishClick = {},
                    onCancelClick = {},
                    mapContent = { Box(modifier = Modifier.fillMaxSize()) },
                )
            }
        }
        composeTestRule.onNodeWithText("Start").assertIsNotEnabled()
    }

    @Test
    fun allRunScreen_expandsRunCardOnClick() {
        val sampleRun = Run(
            id = 1,
            title = "Evening Jog",
            timestamp = 1700000000000L,
            avgSpeedInKMH = 9.2f,
            distanceInMeters = 4200,
            timeInMillis = 1600000L,
            caloriesBurned = 290,
        )
        composeTestRule.setContent {
            LerunTheme(darkTheme = true) {
                AllRunScreenContent(
                    runs = listOf(sampleRun),
                    selectedSortType = SortType.DATE,
                    onSortTypeSelected = {},
                )
            }
        }
        composeTestRule.onNodeWithText("All Activities").assertIsDisplayed()
        composeTestRule.onNodeWithText("Evening Jog").performClick()
        composeTestRule.onNodeWithText("Calories Burned").assertIsDisplayed()
    }

    @Test
    fun detailRunScreen_displaysRunDetails() {
        val sampleRun = Run(
            id = 1,
            title = "Weekend Long Run",
            timestamp = 1700000000000L,
            avgSpeedInKMH = 11.0f,
            distanceInMeters = 10000,
            timeInMillis = 3270000L,
            caloriesBurned = 700,
        )
        composeTestRule.setContent {
            LerunTheme(darkTheme = true) {
                DetailRunScreen(run = sampleRun)
            }
        }
        composeTestRule.onNodeWithText("Detail Run").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weekend Long Run").assertIsDisplayed()
        composeTestRule.onNodeWithText("700kcal").assertIsDisplayed()
    }

    @Test
    fun statisticsScreen_displaysMetrics() {
        composeTestRule.setContent {
            LerunTheme(darkTheme = true) {
                StatisticsScreenContent(
                    totalTime = "01:15:00",
                    totalDistance = "12.5km",
                    totalCalories = "850kcal",
                    averageSpeed = "10.0km/h",
                    runs = emptyList(),
                )
            }
        }
        composeTestRule.onNodeWithText("01:15:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("12.5km").assertIsDisplayed()
        composeTestRule.onNodeWithText("850kcal").assertIsDisplayed()
        composeTestRule.onNodeWithText("10.0km/h").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysOptions() {
        composeTestRule.setContent {
            LerunTheme(darkTheme = true) {
                SettingsScreenContent(
                    username = "Ar Razy",
                    profileImageBase64 = null,
                    isDarkTheme = true,
                    versionText = "Lerun version v1.2.1",
                    onEditProfileClick = {},
                    onDarkThemeToggle = {},
                    onLicensesClick = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ar Razy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Edit Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Licenses").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lerun version v1.2.1").assertIsDisplayed()
    }

    @Test
    fun navigation3_switchesFromRunToSettingsAndBackToRun() {
        composeTestRule.setContent {
            LerunTheme(darkTheme = true) {
                val navigationState = rememberNavigationState(
                    startRoute = Screen.Run,
                    topLevelRoutes = listOf(Screen.Run, Screen.Settings),
                )
                val navigator = remember(navigationState) { Navigator(navigationState) }
                val entryProvider = entryProvider<NavKey> {
                    entry<Screen.Run> {
                        Text("Home Screen Content")
                    }
                    entry<Screen.Settings> {
                        Text("Settings Screen Content")
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    NavDisplay(
                        entries = navigationState.toEntries(entryProvider),
                        onBack = { navigator.goBack() },
                        modifier = Modifier.fillMaxSize(),
                    )
                    LerunFloatingBottomBar(
                        currentRoute = navigationState.currentRoute,
                        onNavigateToRoute = { navigator.navigate(it) },
                        onOpenTracking = {},
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Home Screen Content").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Setting").performClick()
        composeTestRule.onNodeWithText("Settings Screen Content").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Activity").performClick()
        composeTestRule.onNodeWithText("Home Screen Content").assertIsDisplayed()
    }
}
