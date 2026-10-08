package com.bettyai.assistant.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BettyDarkColors = darkColorScheme(
    primary = BettyViolet,
    onPrimary = BettyOnDark,
    primaryContainer = BettyVioletDark,
    onPrimaryContainer = BettyOnDark,
    secondary = BettyCyan,
    onSecondary = BettyBackground,
    secondaryContainer = BettyCyanDark,
    onSecondaryContainer = BettyOnDark,
    background = BettyBackground,
    onBackground = BettyOnDark,
    surface = BettySurface,
    onSurface = BettyOnDark,
    surfaceVariant = BettySurfaceVariant,
    onSurfaceVariant = BettyOnDarkMuted,
    outline = BettyOutline,
    error = BettyError,
    onError = BettyOnDark
)

private val BettyLightColors = lightColorScheme(
    primary = BettyVioletDark,
    onPrimary = BettyOnDark,
    primaryContainer = BettyVioletLight,
    onPrimaryContainer = BettyOnLight,
    secondary = BettyCyanDark,
    onSecondary = BettyOnDark,
    secondaryContainer = BettyCyan,
    onSecondaryContainer = BettyOnLight,
    background = BettyLightBackground,
    onBackground = BettyOnLight,
    surface = BettyLightSurface,
    onSurface = BettyOnLight,
    surfaceVariant = BettyLightSurfaceVariant,
    onSurfaceVariant = BettyOnLightMuted,
    outline = BettyLightOutline,
    error = BettyError,
    onError = BettyOnDark
)

@Composable
fun BettyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BettyDarkColors else BettyLightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Android 15+ (API 35+) enforces edge-to-edge; the system bar colors are
            // driven by enableEdgeToEdge() in MainActivity. We only adjust the icon
            // appearance here so the deprecated color setters are no longer needed.
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BettyTypography,
        shapes = BettyShapes,
        content = content
    )
}
