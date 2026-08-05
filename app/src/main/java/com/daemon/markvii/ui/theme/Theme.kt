package com.daemon.markvii.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Custom color scheme for app-specific colors
data class AppColors(
    val topBarBackground: Color,
    val surfaceVariant: Color,
    val surfaceTertiary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val onAccent: Color,
    val error: Color,
    val inputBackground: Color,
    val divider: Color
)

private val DarkAppColors = AppColors(
    topBarBackground = DarkTopBar,
    surfaceVariant = DarkSurfaceVariant,
    surfaceTertiary = DarkSurfaceTertiary,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    accent = DarkPrimary,
    onAccent = Color.White,
    error = DarkError,
    inputBackground = DarkSurfaceTertiary,
    divider = DarkSurfaceTertiary
)

private val LightAppColors = AppColors(
    topBarBackground = LightTopBar,
    surfaceVariant = LightSurfaceVariant,
    surfaceTertiary = LightSurfaceTertiary,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    accent = LightPrimary,
    onAccent = Color.White,
    error = LightError,
    inputBackground = LightSurfaceVariant,
    divider = LightSurfaceTertiary
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTextPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightTextPrimary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary
)

@Composable
fun MarkVIITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true, // Enabled to use device monet colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val baseAppColors = if (darkTheme) DarkAppColors else LightAppColors
    val appColors = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        baseAppColors.copy(
            accent = colorScheme.primary,
            onAccent = colorScheme.onPrimary
        )
    } else {
        baseAppColors
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        @Suppress("DEPRECATION")
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar to transparent to allow edge-to-edge unified look
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}