package com.stencilla.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val StencillaLightColors = lightColorScheme(
    primary            = StencillaTerracotta,
    onPrimary          = StencillaCardWhite,
    primaryContainer   = StencillaTerracottaLight,
    onPrimaryContainer = StencillaTerracottaDark,
    secondary          = StencillaLavenderDark,
    onSecondary        = StencillaCardWhite,
    secondaryContainer = StencillaLavender,
    onSecondaryContainer = StencillaLavenderDark,
    tertiary           = StencillaGold,
    background         = StencillaOffWhite,
    onBackground       = StencillaBlack,
    surface            = StencillaCardWhite,
    onSurface          = StencillaDarkGray,
    surfaceVariant     = StencillaLightGray,
    onSurfaceVariant   = StencillaDarkGray,
    outline            = StencillaLightGray,
    error              = StencillaError,
    onError            = StencillaCardWhite,
)

@Composable
fun StencillaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StencillaLightColors,
        typography  = StencillaTypography,
        content     = content,
    )
}
