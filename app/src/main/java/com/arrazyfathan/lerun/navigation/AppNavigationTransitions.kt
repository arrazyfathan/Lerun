package com.arrazyfathan.lerun.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

object AppNavigationTransitions {
    private const val TopLevelEnterDurationMs = 220
    private const val TopLevelExitDurationMs = 180
    private const val HierarchicalEnterDurationMs = 260
    private const val HierarchicalExitDurationMs = 220
    private const val TopLevelSlideFactor = 6
    private const val HierarchicalForegroundSlideFactor = 3
    private const val HierarchicalBackgroundSlideFactor = 5

    val TopLevelForward: ContentTransform
        get() = topLevelForwardEnter() togetherWith topLevelForwardExit()

    val TopLevelBackward: ContentTransform
        get() = topLevelBackwardEnter() togetherWith topLevelBackwardExit()

    val HierarchicalPush: ContentTransform
        get() = hierarchicalPushEnter() togetherWith hierarchicalPushExit()

    val HierarchicalPop: ContentTransform
        get() = hierarchicalPopEnter() togetherWith hierarchicalPopExit()

    fun topLevelForwardEnter(): EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth / TopLevelSlideFactor },
        animationSpec = tween(
            durationMillis = TopLevelEnterDurationMs,
            easing = LinearOutSlowInEasing,
        ),
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = TopLevelEnterDurationMs,
            easing = LinearOutSlowInEasing,
        ),
    )

    fun topLevelForwardExit(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / TopLevelSlideFactor },
        animationSpec = tween(
            durationMillis = TopLevelExitDurationMs,
            easing = FastOutSlowInEasing,
        ),
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = TopLevelExitDurationMs,
            easing = FastOutSlowInEasing,
        ),
    )

    fun topLevelBackwardEnter(): EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / TopLevelSlideFactor },
        animationSpec = tween(
            durationMillis = TopLevelEnterDurationMs,
            easing = LinearOutSlowInEasing,
        ),
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = TopLevelEnterDurationMs,
            easing = LinearOutSlowInEasing,
        ),
    )

    fun topLevelBackwardExit(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth / TopLevelSlideFactor },
        animationSpec = tween(
            durationMillis = TopLevelExitDurationMs,
            easing = FastOutSlowInEasing,
        ),
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = TopLevelExitDurationMs,
            easing = FastOutSlowInEasing,
        ),
    )

    fun hierarchicalPushEnter(): EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth / HierarchicalForegroundSlideFactor },
        animationSpec = tween(
            durationMillis = HierarchicalEnterDurationMs,
            easing = LinearOutSlowInEasing,
        ),
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = HierarchicalEnterDurationMs,
            easing = LinearOutSlowInEasing,
        ),
    )

    fun hierarchicalPushExit(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / HierarchicalBackgroundSlideFactor },
        animationSpec = tween(
            durationMillis = HierarchicalExitDurationMs,
            easing = FastOutSlowInEasing,
        ),
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = HierarchicalExitDurationMs,
            easing = FastOutSlowInEasing,
        ),
    )

    fun hierarchicalPopEnter(): EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / HierarchicalBackgroundSlideFactor },
        animationSpec = tween(
            durationMillis = HierarchicalEnterDurationMs,
            easing = LinearOutSlowInEasing,
        ),
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = HierarchicalEnterDurationMs,
            easing = LinearOutSlowInEasing,
        ),
    )

    fun hierarchicalPopExit(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth / HierarchicalForegroundSlideFactor },
        animationSpec = tween(
            durationMillis = HierarchicalExitDurationMs,
            easing = FastOutSlowInEasing,
        ),
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = HierarchicalExitDurationMs,
            easing = FastOutSlowInEasing,
        ),
    )
}
