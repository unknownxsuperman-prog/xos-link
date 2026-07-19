package com.x0s.link.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val Color_White = androidx.compose.ui.graphics.Color(0xFFFFFFFF)

private val XosDarkColorScheme = darkColorScheme(
    primary = XosAccent,
    onPrimary = Color_White,
    secondary = XosAccent,
    background = XosBg,
    onBackground = XosText,
    surface = XosCard,
    onSurface = XosText,
    surfaceVariant = XosCard2,
    onSurfaceVariant = XosMuted,
    outline = XosBorder,
    error = ErrorRed
)

private val XosLightColorScheme = lightColorScheme(
    primary = XosAccent,
    onPrimary = Color_White,
    secondary = XosAccent,
    background = XosBgLight,
    onBackground = XosTextLight,
    surface = XosCardLight,
    onSurface = XosTextLight,
    surfaceVariant = XosCardLight,
    onSurfaceVariant = XosMutedLight,
    outline = XosBorderLight,
    error = ErrorRed
)

// ... rest of the file


/**
 * [darkTheme] defaults to the system setting but is overridden by the in-app "Appearance"
 * toggle (mirrors the web app's Dark Mode pill in the drawer / dropdown menu).
 */
@Composable
fun XosLinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) XosDarkColorScheme else XosLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = XosTypography,
        content = content
    )
}
