package com.struperto.androidappdays.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LocalAppColors = staticCompositionLocalOf { LightAppColors }
private val LocalAppTypography = staticCompositionLocalOf { AppTypographyDefault }
private val LocalAppDimensions = staticCompositionLocalOf { AppDimensionsDefault }

object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
    val typography: AppTypography
        @Composable get() = LocalAppTypography.current
    val dimensions: AppDimensions
        @Composable get() = LocalAppDimensions.current
}

@Composable
fun DaysTheme(content: @Composable () -> Unit) {
    val colors = LightAppColors
    val materialTypography = Typography(
        displayLarge = AppTypographyDefault.display,
        headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 30.sp),
        headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 26.sp),
        headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
        titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
        titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
        titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp),
        bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
        bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
        labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.1.sp),
        labelMedium = AppTypographyDefault.label,
        labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.1.sp),
    )
    val materialColors = lightColorScheme(
        primary = colors.accent,
        onPrimary = colors.surfaceStrong,
        primaryContainer = colors.accentSoft,
        onPrimaryContainer = colors.ink,
        surface = colors.surface,
        onSurface = colors.ink,
        surfaceBright = colors.surfaceStrong,
        surfaceContainer = colors.surfaceMuted,
        surfaceContainerHigh = colors.surfaceStrong,
        surfaceContainerHighest = colors.surfaceStrong,
        onSurfaceVariant = colors.muted,
        outline = colors.outlineSoft,
        background = colors.surface,
        onBackground = colors.ink,
        secondary = colors.info,
        tertiary = colors.success,
        error = colors.danger,
    )

    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides AppTypographyDefault,
        LocalAppDimensions provides AppDimensionsDefault,
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = materialTypography,
            content = content,
        )
    }
}
