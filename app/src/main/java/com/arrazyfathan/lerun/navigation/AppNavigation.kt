package com.arrazyfathan.lerun.navigation

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.arrazyfathan.lerun.feature.onboarding.navigation.OnboardingNavRoute
import com.arrazyfathan.lerun.feature.onboarding.navigation.onboardingEntry
import com.arrazyfathan.lerun.feature.run.navigation.AllRunsNavRoute
import com.arrazyfathan.lerun.feature.run.navigation.DetailRunNavRoute
import com.arrazyfathan.lerun.feature.run.navigation.RunNavRoute
import com.arrazyfathan.lerun.feature.run.navigation.TrackingNavRoute
import com.arrazyfathan.lerun.feature.run.navigation.runEntries
import com.arrazyfathan.lerun.feature.settings.navigation.SettingsNavRoute
import com.arrazyfathan.lerun.feature.settings.navigation.settingsEntry
import com.arrazyfathan.lerun.feature.statistics.navigation.StatisticsNavRoute
import com.arrazyfathan.lerun.feature.statistics.navigation.statisticsEntry
import com.arrazyfathan.lerun.ui.designsystem.LerunGreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import com.arrazyfathan.lerun.core.ui.R as UiR

private data class TopLevelDestination(
    val route: NavKey,
    val label: String,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
)

private val RunDestination = TopLevelDestination(
    route = RunNavRoute,
    label = "Activity",
    selectedIcon = UiR.drawable.ic_activity_selected,
    unselectedIcon = UiR.drawable.ic_activity_unselected,
)

private val SettingsDestination = TopLevelDestination(
    route = SettingsNavRoute,
    label = "Setting",
    selectedIcon = UiR.drawable.ic_setting_selected,
    unselectedIcon = UiR.drawable.ic_setting_unselected,
)

private val TopLevelRoutes: List<NavKey> = listOf(
    RunDestination.route,
    SettingsDestination.route,
)

private val BottomBarRoutes: Set<NavKey> = setOf(
    RunNavRoute,
    SettingsNavRoute,
    StatisticsNavRoute,
)

class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: NavBackStack<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
    val topLevelRoutes: Set<NavKey> = backStacks.keys,
) {
    private val topLevelStack = topLevelRoute

    val currentTopLevelRoute: NavKey
        get() = topLevelStack.last()

    val currentRoute: NavKey
        get() = backStacks[currentTopLevelRoute]?.lastOrNull() ?: currentTopLevelRoute

    fun navigateTopLevel(route: NavKey) {
        if (route == startRoute) {
            topLevelStack.removeAll { it != startRoute }
        } else {
            topLevelStack.remove(route)
            if (topLevelStack.lastOrNull() != route) {
                topLevelStack.add(route)
            }
        }
    }

    fun replaceRoot(route: NavKey) {
        topLevelStack.clear()
        topLevelStack.add(route)
    }

    fun popTopLevel(): Boolean {
        if (topLevelStack.size <= 1) return false
        topLevelStack.removeLastOrNull()
        return true
    }

    val stacksInUse: List<NavKey>
        get() = if (currentTopLevelRoute !in topLevelRoutes || currentTopLevelRoute == startRoute) {
            listOf(currentTopLevelRoute)
        } else {
            listOf(startRoute, currentTopLevelRoute)
        }
}

@Composable
fun rememberNavigationState(
    startRoute: NavKey,
    topLevelRoutes: List<NavKey>,
    homeRoute: NavKey = startRoute,
): NavigationState {
    val topLevelRoute = key("top_level_route") {
        rememberNavBackStack(startRoute)
    }
    val stackRoots = remember(startRoute, topLevelRoutes) {
        (listOf(startRoute) + topLevelRoutes).distinct()
    }
    val backStacks = stackRoots.associateWith { routeKey ->
        key("stack_$routeKey") {
            rememberNavBackStack(routeKey)
        }
    }

    return remember(homeRoute, topLevelRoutes, topLevelRoute, backStacks) {
        NavigationState(
            startRoute = homeRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks,
            topLevelRoutes = topLevelRoutes.toSet(),
        )
    }
}

class Navigator(val state: NavigationState) {
    fun navigate(route: NavKey) {
        if (route in state.topLevelRoutes) {
            val targetStack = state.backStacks[route]
            if (route == state.currentTopLevelRoute && (targetStack?.size ?: 0) > 1) {
                while (targetStack != null && targetStack.size > 1) {
                    targetStack.removeLastOrNull()
                }
            } else {
                state.navigateTopLevel(route)
            }
        } else {
            val currentStack = state.backStacks[state.currentTopLevelRoute]
            if (currentStack?.lastOrNull() != route) {
                currentStack?.add(route)
            }
        }
    }

    fun completeOnboarding(targetRoute: NavKey = state.startRoute) {
        if (state.currentTopLevelRoute != targetRoute) {
            state.replaceRoot(targetRoute)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.currentTopLevelRoute]
            ?: error("Stack for ${state.currentTopLevelRoute} not found")
        val currentRoute = currentStack.last()

        if (currentRoute == state.currentTopLevelRoute) {
            state.popTopLevel()
        } else {
            currentStack.removeLastOrNull()
        }
    }

    fun popToCurrentTopLevel() {
        val currentStack = state.backStacks[state.currentTopLevelRoute] ?: return
        while (currentStack.size > 1) {
            currentStack.removeLastOrNull()
        }
    }
}

@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {
    val decoratedEntries = backStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            rememberViewModelStoreNavEntryDecorator(),
        )
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider,
        )
    }

    return stacksInUse
        .flatMap { decoratedEntries[it] ?: emptyList() }
        .toMutableStateList()
}

@Composable
fun MainApp(
    isProfileCompleted: Boolean = true,
    trackingIntentEvents: Flow<Unit> = emptyFlow(),
    onTrackingIntentConsumed: () -> Unit = {},
) {
    val initialRoute: NavKey = if (isProfileCompleted) RunNavRoute else OnboardingNavRoute
    val navigationState = rememberNavigationState(
        startRoute = initialRoute,
        topLevelRoutes = TopLevelRoutes,
        homeRoute = RunNavRoute,
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

    LaunchedEffect(isProfileCompleted) {
        if (isProfileCompleted && navigationState.currentTopLevelRoute == OnboardingNavRoute) {
            navigator.completeOnboarding()
        }
    }

    LaunchedEffect(trackingIntentEvents) {
        trackingIntentEvents.collect {
            if (navigationState.currentTopLevelRoute != OnboardingNavRoute) {
                onTrackingIntentConsumed()
                navigator.navigate(TrackingNavRoute)
            }
        }
    }

    val entryProvider = entryProvider<NavKey> {
        onboardingEntry(
            onNavigateToRun = { navigator.completeOnboarding() },
        )
        runEntries(
            onOpenAllRuns = { navigator.navigate(AllRunsNavRoute) },
            onOpenRunDetail = { runId -> navigator.navigate(DetailRunNavRoute(runId)) },
            onCloseTrackingToRun = {
                navigator.popToCurrentTopLevel()
                navigator.navigate(RunNavRoute)
            },
        )
        settingsEntry()
        statisticsEntry()
    }

    val currentRoute = navigationState.currentRoute
    val showBottomBar = currentRoute in BottomBarRoutes

    val density = LocalDensity.current
    val maxHideOffsetPx = remember(density) { with(density) { 220.dp.toPx() } }
    var isBottomBarHidden by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(currentRoute) {
        isBottomBarHidden = 0f
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -4f) {
                    isBottomBarHidden = 1f
                } else if (available.y > 4f) {
                    isBottomBarHidden = 0f
                }
                return Offset.Zero
            }
        }
    }

    val animatedTranslationY by animateFloatAsState(
        targetValue = if (isBottomBarHidden > 0.5f) maxHideOffsetPx else 0f,
        animationSpec = tween(durationMillis = 225),
        label = "HideBottomViewOnScroll",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
    ) {
        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() },
            transitionSpec = {
                val initialKey = initialState.key
                val targetKey = targetState.key
                if (initialKey in navigationState.topLevelRoutes && targetKey in navigationState.topLevelRoutes) {
                    val topLevelList = TopLevelRoutes
                    val initialIndex = topLevelList.indexOf(initialKey)
                    val targetIndex = topLevelList.indexOf(targetKey)
                    if (targetIndex >= initialIndex) {
                        AppNavigationTransitions.TopLevelForward
                    } else {
                        AppNavigationTransitions.TopLevelBackward
                    }
                } else {
                    AppNavigationTransitions.HierarchicalPush
                }
            },
            popTransitionSpec = {
                val initialKey = initialState.key
                val targetKey = targetState.key
                if (initialKey in navigationState.topLevelRoutes && targetKey in navigationState.topLevelRoutes) {
                    val topLevelList = TopLevelRoutes
                    val initialIndex = topLevelList.indexOf(initialKey)
                    val targetIndex = topLevelList.indexOf(targetKey)
                    if (targetIndex >= initialIndex) {
                        AppNavigationTransitions.TopLevelForward
                    } else {
                        AppNavigationTransitions.TopLevelBackward
                    }
                } else {
                    AppNavigationTransitions.HierarchicalPop
                }
            },
            predictivePopTransitionSpec = {
                val initialKey = initialState.key
                val targetKey = targetState.key
                if (initialKey in navigationState.topLevelRoutes && targetKey in navigationState.topLevelRoutes) {
                    val topLevelList = TopLevelRoutes
                    val initialIndex = topLevelList.indexOf(initialKey)
                    val targetIndex = topLevelList.indexOf(targetKey)
                    if (targetIndex >= initialIndex) {
                        AppNavigationTransitions.TopLevelForward
                    } else {
                        AppNavigationTransitions.TopLevelBackward
                    }
                } else {
                    AppNavigationTransitions.HierarchicalPop
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        if (showBottomBar) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer {
                        translationY = animatedTranslationY
                    }
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background,
                            ),
                        ),
                    ),
            )

            LerunFloatingBottomBar(
                currentRoute = currentRoute,
                onNavigateToRoute = { route ->
                    navigator.navigate(route)
                },
                onOpenTracking = {
                    navigator.navigate(TrackingNavRoute)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .graphicsLayer {
                        translationY = animatedTranslationY
                    }
                    .navigationBarsPadding()
                    .padding(bottom = 30.dp),
            )
        }
    }
}

@Composable
fun LerunFloatingBottomBar(
    currentRoute: NavKey?,
    onNavigateToRoute: (NavKey) -> Unit,
    onOpenTracking: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 20.dp,
        modifier = modifier
            .width(270.dp)
            .height(90.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val isRunSelected = currentRoute == RunDestination.route
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Tab,
                        onClick = { onNavigateToRoute(RunDestination.route) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isRunSelected) {
                            RunDestination.selectedIcon
                        } else {
                            RunDestination.unselectedIcon
                        },
                    ),
                    contentDescription = RunDestination.label,
                    tint = if (isRunSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        Color(0xFF59595C)
                    },
                    modifier = Modifier.size(24.dp),
                )
            }

            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = LerunGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onOpenTracking),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = UiR.drawable.ic_add_black),
                        contentDescription = stringResource(id = UiR.string.floating),
                        tint = Color.Black,
                    )
                }
            }

            val isSettingsSelected = currentRoute == SettingsDestination.route
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Tab,
                        onClick = { onNavigateToRoute(SettingsDestination.route) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isSettingsSelected) {
                            SettingsDestination.selectedIcon
                        } else {
                            SettingsDestination.unselectedIcon
                        },
                    ),
                    contentDescription = SettingsDestination.label,
                    tint = if (isSettingsSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        Color(0xFF59595C)
                    },
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}
