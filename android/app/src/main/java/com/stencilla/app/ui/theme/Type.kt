package com.stencilla.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.stencilla.app.R

val DmSans = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_light, FontWeight.Light),
)

val StencillaTypography = Typography(
    headlineLarge  = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Light,  fontSize = 32.sp, letterSpacing = 0.3.sp),
    headlineMedium = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Light,  fontSize = 26.sp, letterSpacing = 0.3.sp),
    titleLarge     = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 20.sp),
    titleMedium    = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 17.sp),
    bodyLarge      = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge     = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 0.8.sp),
    labelMedium    = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.6.sp),
)
