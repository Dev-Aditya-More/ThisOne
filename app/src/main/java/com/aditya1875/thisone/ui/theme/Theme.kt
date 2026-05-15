package com.aditya1875.thisone.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    // Primary — Vibe Purple
    primary = Vibe400,          // buttons, FABs, active nav
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Vibe800,          // chip backgrounds, selected states
    onPrimaryContainer = Vibe200,

    // Secondary — softer purple tint
    secondary = Vibe300,
    onSecondary = Vibe900,
    secondaryContainer = Night700,
    onSecondaryContainer = Vibe200,

    // Tertiary — Spark Amber
    tertiary = Spark500,
    onTertiary = Night900,
    tertiaryContainer = Spark700,
    onTertiaryContainer = Spark100,

    // Surfaces
    background = Night900,
    onBackground = TextPrimaryDark,
    surface = Night800,
    onSurface = TextPrimaryDark,
    surfaceVariant = Night700,
    onSurfaceVariant = TextSecondaryDark,
    surfaceTint = Vibe600,

    // Outlines
    outline = Night600,
    outlineVariant = Night500,

    // Status
    error = Danger500,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF4A1515),
    onErrorContainer = Danger100,

    // Inverse (snackbars etc.)
    inverseSurface = Cloud100,
    inverseOnSurface = TextPrimaryLight,
    inversePrimary = Vibe600,

    // Scrim / overlays
    scrim = Night950,
)

// ── Light scheme ─────────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    // Primary — Vibe Purple (slightly darker for contrast on white)
    primary = Vibe600,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Vibe100,
    onPrimaryContainer = Vibe800,

    // Secondary
    secondary = Vibe500,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Vibe050,
    onSecondaryContainer = Vibe700,

    // Tertiary — Spark Amber
    tertiary = Spark600,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Spark100,
    onTertiaryContainer = Spark700,

    // Surfaces
    background = Cloud050,
    onBackground = TextPrimaryLight,
    surface = Color(0xFFFFFFFF),
    onSurface = TextPrimaryLight,
    surfaceVariant = Cloud100,
    onSurfaceVariant = TextSecondaryLight,
    surfaceTint = Vibe600,

    // Outlines
    outline = Cloud300,
    outlineVariant = Cloud200,

    // Status
    error = Danger500,
    onError = Color(0xFFFFFFFF),
    errorContainer = Danger100,
    onErrorContainer = Color(0xFF7F1D1D),

    // Inverse
    inverseSurface = Night800,
    inverseOnSurface = TextPrimaryDark,
    inversePrimary = Vibe400,

    scrim = Color(0xFF000000),
)

// ── Theme entry point ─────────────────────────────────────────────────────────

@Composable
fun ThisOneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Note: dynamic color (Material You) intentionally disabled —
    // ThisOne has a strong brand identity that should stay consistent.
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Make status bar match the surface colour and use appropriate icon tint.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ThisOneTypography,
        shapes = ThisOneShapes,
        content = content,
    )
}