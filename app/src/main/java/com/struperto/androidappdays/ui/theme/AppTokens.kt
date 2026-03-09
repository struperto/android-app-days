package com.struperto.androidappdays.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AppColors(
    val surface: Color,
    val surfaceStrong: Color,
    val surfaceMuted: Color,
    val ink: Color,
    val muted: Color,
    val accent: Color,
    val accentSoft: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,
    val outline: Color,
    val outlineSoft: Color,
)

val LightAppColors = AppColors(
    surface = Color(0xFFF6F1EA),
    surfaceStrong = Color(0xFFFFFFFF),
    surfaceMuted = Color(0xFFF1E6DA),
    ink = Color(0xFF1B1A17),
    muted = Color(0xFF6D675C),
    accent = Color(0xFFE07A5F),
    accentSoft = Color(0xFFF3C1B0),
    success = Color(0xFF4F8A5B),
    warning = Color(0xFFC8933F),
    danger = Color(0xFFB85C38),
    info = Color(0xFF5C7FA8),
    outline = Color(0xFF1B1A17),
    outlineSoft = Color(0xFFCBC3B9),
)

data class AppTypography(
    val display: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val label: TextStyle,
    val mono: TextStyle,
)

private fun appTextStyle(
    fontSize: TextUnit,
    fontWeight: FontWeight,
    letterSpacing: TextUnit = 0.sp,
    fontFamily: FontFamily = FontFamily.SansSerif,
): TextStyle {
    return TextStyle(
        fontSize = fontSize,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
        fontFamily = fontFamily,
    )
}

val AppTypographyDefault = AppTypography(
    display = appTextStyle(28.sp, FontWeight.SemiBold, 0.5.sp),
    title = appTextStyle(18.sp, FontWeight.Medium),
    body = appTextStyle(14.sp, FontWeight.Normal),
    label = appTextStyle(12.sp, FontWeight.SemiBold, 1.2.sp),
    mono = appTextStyle(11.sp, FontWeight.Medium, 1.1.sp, FontFamily.Monospace),
)

data class AppDimensions(
    val spacingS: Dp = 8.dp,
    val spacingM: Dp = 12.dp,
    val spacingL: Dp = 16.dp,
    val spacingXl: Dp = 24.dp,
    val radiusM: Dp = 12.dp,
    val radiusL: Dp = 18.dp,
    val radiusXl: Dp = 26.dp,
    val screenPadding: Dp = 20.dp,
)

val AppDimensionsDefault = AppDimensions()
