package com.stencilla.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val StencillaLightColors = lightColorScheme(
    primary = StencillaBlack,
    onPrimary = StencillaWhite,
    secondary = StencillaGold,
    onSecondary = StencillaBlack,
    background = StencillaOffWhite,
    onBackground = StencillaBlack,
    surface = StencillaWhite,
    onSurface = StencillaBlack,
    surfaceVariant = StencillaLightGray,
    onSurfaceVariant = StencillaCharcoal,
    outline = StencillaLightGray,
    error = StencillaError,
)

private val StencillaDarkColors = darkColorScheme(
    primary = StencillaWhite,
    onPrimary = StencillaBlack,
    secondary = StencillaGold,
    onSecondary = StencillaBlack,
    background = StencillaBlack,
    onBackground = StencillaWhite,
    surface = StencillaCharcoal,
    onSurface = StencillaWhite,
    surfaceVariant = StencillaCharcoal,
    onSurfaceVariant = StencillaLightGray,
    outline = StencillaGray,
    error = StencillaError,
)

@Composable
fun StencillaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) StencillaDarkColors else StencillaLightColors
    MaterialTheme(
        colorScheme = colors,
        typography = StencillaTypography,
        content = content,
    )
}
