package com.x0s.link.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The web app uses "Space Grotesk" throughout. To match it exactly:
 *  1. Download the Space Grotesk .ttf files (fonts.google.com/specimen/Space+Grotesk)
 *  2. Drop them into res/font/ as: space_grotesk_light.ttf, space_grotesk_regular.ttf,
 *     space_grotesk_medium.ttf, space_grotesk_semibold.ttf, space_grotesk_bold.ttf
 *  3. Replace the line below with:
 *
 *     val SpaceGrotesk = FontFamily(
 *         Font(R.font.space_grotesk_light, FontWeight.Light),
 *         Font(R.font.space_grotesk_regular, FontWeight.Normal),
 *         Font(R.font.space_grotesk_medium, FontWeight.Medium),
 *         Font(R.font.space_grotesk_semibold, FontWeight.SemiBold),
 *         Font(R.font.space_grotesk_bold, FontWeight.Bold)
 *     )
 *
 * Until then this falls back to the system SansSerif family so the project builds with
 * zero extra assets required.
 */
val SpaceGrotesk: FontFamily = FontFamily.SansSerif

val XosTypography = Typography(
    displayLarge = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 32.sp, letterSpacing = (-0.03).sp),
    headlineMedium = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = (-0.02).sp),
    titleLarge = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 19.sp, letterSpacing = (-0.02).sp),
    titleMedium = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Light, fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Normal, fontSize = 11.sp),
    labelLarge = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    labelMedium = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 0.05.sp),
    labelSmall = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 0.14.sp)
)
