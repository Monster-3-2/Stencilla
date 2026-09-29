package com.stencilla.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Using system sans-serif which maps to Roboto on Android.
// To use DM Sans: add dm_sans_regular.ttf / dm_sans_medium.ttf / dm_sans_light.ttf
// to app/src/main/res/font/ (download from fonts.google.com/specimen/DM+Sans)
// then replace FontFamily.SansSerif below with:
//   FontFamily(
//       Font(R.font.dm_sans_regular, FontWeight.Normal),
//       Font(R.font.dm_sans_medium,  FontWeight.Medium),
//       Font(R.font.dm_sans_light,   FontWeight.Light),
//   )

private val AppFontFamily = FontFamily.SansSerif

val StencillaTypography = Typography(
    headlineLarge  = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Light,  fontSize = 32.sp, letterSpacing = 0.3.sp),
    headlineMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Light,  fontSize = 26.sp, letterSpacing = 0.3.sp),
    headlineSmall  = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 22.sp),
    titleLarge     = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 20.sp),
    titleMedium    = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 17.sp),
    titleSmall     = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp),
    bodyLarge      = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge     = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 0.8.sp),
    labelMedium    = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.6.sp),
    labelSmall     = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp, letterSpacing = 0.6.sp),
)
