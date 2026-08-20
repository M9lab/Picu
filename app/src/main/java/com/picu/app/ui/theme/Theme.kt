package com.picu.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PicuColorScheme = lightColorScheme(
    primary = PicuBlue,
    onPrimary = PicuWhite,
    primaryContainer = PicuBluePale,
    onPrimaryContainer = PicuTextDark,
    secondary = PicuMint,
    onSecondary = PicuTextDark,
    secondaryContainer = PicuMint,
    onSecondaryContainer = PicuTextDark,
    tertiary = PicuPeach,
    onTertiary = PicuTextDark,
    background = PicuBackground,
    onBackground = PicuTextDark,
    surface = PicuSurface,
    onSurface = PicuTextDark,
    surfaceVariant = PicuBluePale,
    onSurfaceVariant = PicuTextDark,
    error = PicuError,
    onError = PicuWhite
)

@Composable
fun PicuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PicuColorScheme,
        typography = PicuTypography,
        content = content
    )
}
