package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PremiumPrimary,
    onPrimary = Color.White,
    secondary = PremiumGradientEnd,
    onSecondary = Color.White,
    background = PremiumBg,
    onBackground = PremiumTextPrimary,
    surface = PremiumSurface,
    onSurface = PremiumTextPrimary,
    surfaceVariant = PremiumHighlight,
    onSurfaceVariant = PremiumTextSecondary,
    outline = PremiumBorder
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = LightColorScheme,
    typography = Typography,
    content = content
  )
}
