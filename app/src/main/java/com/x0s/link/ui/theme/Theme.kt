package com.x0s.link.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp

private val XosDarkScheme = darkColorScheme(
    background = XosBackground,
    surface = XosCard,
    primary = XosAccent,
    onBackground = XosWhite,
    onSurface = XosWhite,
    outline = XosBorder
)

// Space Grotesk isn't bundled by default; fall back to the platform default
// sans family. Drop a Space Grotesk .ttf into res/font and swap this out
// for FontFamily(Font(R.font.space_grotesk)) to match the web app exactly.
val XosFontFamily = FontFamily.SansSerif

val XosTypography = Typography(
    titleLarge = TextStyle(fontFamily = XosFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = XosFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyLarge = TextStyle(fontFamily = XosFontFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = XosFontFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelSmall = TextStyle(fontFamily = XosFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp)
)

@Composable
fun X0sLinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = XosDarkScheme,
        typography = XosTypography,
        content = content
    )
}
