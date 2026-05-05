package com.arrazyfathan.lerun.ui.designsystem

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val DarkColorScheme = darkColorScheme(
    primary = LerunGreen,
    background = LerunBlack,
    surface = LerunDarkGray,
    secondary = LerunWhite,
    tertiary = LerunWhite,
    primaryContainer = LerunGreen30,
    onPrimary = LerunBlack,
    onBackground = LerunWhite,
    onSurface = LerunWhite,
    onSurfaceVariant = LerunGray,
    error = LerunDarkRed,
    errorContainer = LerunDarkRed5
)

@Composable
fun LerunTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}