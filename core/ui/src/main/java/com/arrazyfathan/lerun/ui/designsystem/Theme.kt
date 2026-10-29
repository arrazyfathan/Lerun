package com.arrazyfathan.lerun.ui.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val DarkColorScheme = darkColorScheme(
    primary = LerunGreen,
    background = Color.Black,
    surface = LerunSecondaryDark,
    surfaceVariant = LerunMainDark,
    secondary = LerunWhite,
    tertiary = LerunWhite,
    primaryContainer = LerunGreen30,
    onPrimary = LerunMainDark,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = LerunDarkAccentText,
    error = LerunErrorColor,
    errorContainer = LerunDarkRed5,
)

val LightColorScheme = lightColorScheme(
    primary = LerunGreen,
    background = LerunMainLight,
    surface = LerunSecondaryLight,
    surfaceVariant = LerunMainLight,
    secondary = LerunMainDark,
    tertiary = LerunMainDark,
    primaryContainer = LerunGreen30,
    onPrimary = LerunMainDark,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = LerunDarkAccentText,
    error = LerunErrorColor,
    errorContainer = LerunDarkRed5,
)

@Composable
fun LerunTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
