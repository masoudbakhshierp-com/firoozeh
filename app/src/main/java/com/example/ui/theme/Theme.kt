package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

private val ModernDarkColorScheme = darkColorScheme(
    primary = CleanGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = CleanGreenPrimaryLight,
    onPrimaryContainer = CleanGreenPrimaryDark,
    secondary = CleanGreenAccent,
    onSecondary = Color.White,
    tertiary = CleanOrangeAccent,
    onTertiary = Color.White,
    tertiaryContainer = CleanWarningBg,
    onTertiaryContainer = CleanWarningText,
    background = DarkCanvasStart,
    onBackground = DarkOnSurface,
    surface = DarkSurfaceBase,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceMuted,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

@Composable
fun ZomorrodDriverTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = ModernDarkColorScheme
    val context = LocalContext.current

    SideEffect {
        if (context is Activity) {
            context.window.statusBarColor = Color(0xFF064E3B).toArgb()
            context.window.navigationBarColor = Color(0xFF111A20).toArgb()
            val insetsController = WindowCompat.getInsetsController(context.window, context.window.decorView)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    // Force RTL direction for Persian Driver App
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

