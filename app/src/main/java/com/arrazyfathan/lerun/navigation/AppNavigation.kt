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
import com.arrazyfathan.lerun.presentation.allrun.AllRunRoute
import com.arrazyfathan.lerun.presentation.detailrun.DetailRunRoute
import com.arrazyfathan.lerun.presentation.home.RunRoute
import com.arrazyfathan.lerun.presentation.run.TrackingRoute
import com.arrazyfathan.lerun.presentation.settings.SettingsRoute
import com.arrazyfathan.lerun.presentation.statistic.StatisticsRoute
import com.arrazyfathan.lerun.ui.designsystem.LerunGreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.Serializable
import com.arrazyfathan.lerun.core.ui.R as UiR

@Serializable
sealed class Screen(
    val label: String,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
) : NavKey {

    @Serializable
    data object Run : Screen(
        label = "Activity",
        selectedIcon = UiR.drawable.ic_activity_selected,
        unselectedIcon = UiR.drawable.ic_activity_unselected,
    )

    @Serializable
    data object Settings : Screen(
        label = "Setting",
        selectedIcon = UiR.drawable.ic_setting_selected,
        unselectedIcon = UiR.drawable.ic_setting_unselected,
    )

    @Serializable
    data object Statistics : Screen(
        label = "Statistics",
        selectedIcon = UiR.drawable.ic_activity_selected,
        unselectedIcon = UiR.drawable.ic_activity_unselected,
    )
}

@Serializable
data object TrackingNavRoute : NavKey

@Serializable
data object AllRunsNavRoute : NavKey

@Serializable
data class DetailRunNavRoute(val runId: Int) : NavKey

private val TopLevelScreens = listOf(
    Screen.Run,
    Screen.Settings,
)

class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: NavBackStack<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    private val topLevelStack = topLevelRoute

    val currentTopLevelRoute: NavKey
        get() = topLevelStack.last()

    val currentRoute: NavKey
        get() = backStacks[currentTopLevelRoute]?.lastOrNull() ?: currentTopLevelRoute

    val topLevelRoutes: Set<NavKey>
        get() = backStacks.keys

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

    fun popTopLevel(): Boolean {
        if (topLevelStack.size <= 1) return false
        topLevelStack.removeLastOrNull()
        return true
    }

    val stacksInUse: List<NavKey>
        get() = if (currentTopLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, currentTopLevelRoute)
        }
}

@Composable
fun rememberNavigationState(
    startRoute: NavKey,
    topLevelRoutes: List<NavKey>,
): NavigationState {
    val topLevelRoute = key("top_level_route") {
        rememberNavBackStack(startRoute)
    }
    val backStacks = topLevelRoutes.associateWith { routeKey ->
        key("stack_$routeKey") {
            rememberNavBackStack(routeKey)
        }
    }

    return remember(startRoute, topLevelRoutes, topLevelRoute, backStacks) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks,
        )
    }
}

class Navigator(val state: NavigationState) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
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
    trackingIntentEvents: Flow<Unit> = emptyFlow(),
    onTrackingIntentConsumed: () -> Unit = {},
) {
    val navigationState = rememberNavigationState(
        startRoute = Screen.Run,
        topLevelRoutes = TopLevelScreens,
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

    LaunchedEffect(trackingIntentEvents) {
        trackingIntentEvents.collect {
            onTrackingIntentConsumed()
            navigator.navigate(TrackingNavRoute)
        }
    }

    val entryProvider = entryProvider<NavKey> {
        entry<Screen.Run> {
            RunRoute(
                onOpenAllRuns = { navigator.navigate(AllRunsNavRoute) },
                onOpenRunDetail = { run ->
                    run.id?.let { id -> navigator.navigate(DetailRunNavRoute(id)) }
                },
            )
        }
        entry<Screen.Settings> {
            SettingsRoute()
        }
        entry<Screen.Statistics> {
            StatisticsRoute()
        }
        entry<TrackingNavRoute> {
            TrackingRoute(
                onCloseTrackingToRun = {
                    navigator.popToCurrentTopLevel()
                    navigator.navigate(Screen.Run)
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

    val currentRoute = navigationState.currentRoute
    val showBottomBar = currentRoute in setOf<NavKey>(
        Screen.Run,
        Screen.Settings,
        Screen.Statistics,
    )

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
                    val topLevelList = TopLevelScreens
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
                    val topLevelList = TopLevelScreens
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
                    val topLevelList = TopLevelScreens
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
    onNavigateToRoute: (Screen) -> Unit,
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
            val isRunSelected = currentRoute == Screen.Run
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Tab,
                        onClick = { onNavigateToRoute(Screen.Run) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isRunSelected) {
                            Screen.Run.selectedIcon
                        } else {
                            Screen.Run.unselectedIcon
                        },
                    ),
                    contentDescription = Screen.Run.label,
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

            val isSettingsSelected = currentRoute == Screen.Settings
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Tab,
                        onClick = { onNavigateToRoute(Screen.Settings) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isSettingsSelected) {
                            Screen.Settings.selectedIcon
                        } else {
                            Screen.Settings.unselectedIcon
                        },
                    ),
                    contentDescription = Screen.Settings.label,
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
