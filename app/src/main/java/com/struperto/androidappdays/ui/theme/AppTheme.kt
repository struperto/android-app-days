package com.struperto.androidappdays.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

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
        titleLarge = AppTypographyDefault.title,
        bodyLarge = AppTypographyDefault.body,
        bodyMedium = AppTypographyDefault.body,
        labelMedium = AppTypographyDefault.label,
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
