package com.windnsoft.tbus.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

private val LocalTBusColors = staticCompositionLocalOf { TBusLightColorScheme }
private val LocalTBusTypography = staticCompositionLocalOf { DefaultTBusTypography }
private val LocalTBusSpacing = staticCompositionLocalOf { DefaultTBusSpacing }
private val LocalTBusShapes = staticCompositionLocalOf { DefaultTBusShapes }
private val LocalTBusElevation = staticCompositionLocalOf { DefaultTBusElevation }

private val TBusMaterialLightColorScheme = lightColorScheme(
    primary = TBusColorTokens.Ink,
    onPrimary = TBusColorTokens.Cloud,
    secondary = TBusColorTokens.RouteBlue,
    onSecondary = TBusColorTokens.Cloud,
    tertiary = TBusColorTokens.WarmClip,
    onTertiary = TBusColorTokens.Ink,
    background = TBusLightColorScheme.background,
    onBackground = TBusLightColorScheme.content,
    surface = TBusLightColorScheme.surface,
    onSurface = TBusLightColorScheme.content,
    surfaceVariant = TBusLightColorScheme.surfaceRaised,
    onSurfaceVariant = TBusLightColorScheme.contentMuted,
    outline = TBusLightColorScheme.line,
    error = TBusLightColorScheme.danger,
)

private val TBusMaterialDarkColorScheme = darkColorScheme(
    primary = TBusColorTokens.Cloud,
    onPrimary = TBusColorTokens.Ink,
    secondary = TBusColorTokens.RouteBlue,
    onSecondary = TBusColorTokens.Cloud,
    tertiary = TBusColorTokens.WarmClip,
    onTertiary = TBusColorTokens.Ink,
    background = TBusDarkColorScheme.background,
    onBackground = TBusDarkColorScheme.content,
    surface = TBusDarkColorScheme.surface,
    onSurface = TBusDarkColorScheme.content,
    surfaceVariant = TBusDarkColorScheme.surfaceRaised,
    onSurfaceVariant = TBusDarkColorScheme.contentMuted,
    outline = TBusDarkColorScheme.line,
    error = TBusDarkColorScheme.danger,
)

object TBusTheme {
    val colors: TBusColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalTBusColors.current

    val typography: TBusTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTBusTypography.current

    val spacing: TBusSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalTBusSpacing.current

    val shapes: TBusShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalTBusShapes.current

    val elevation: TBusElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalTBusElevation.current
}

@Composable
fun TBusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val tbusColors = if (darkTheme) TBusDarkColorScheme else TBusLightColorScheme
    val materialColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context is Activity -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> TBusMaterialDarkColorScheme
        else -> TBusMaterialLightColorScheme
    }

    CompositionLocalProvider(
        LocalTBusColors provides tbusColors,
        LocalTBusTypography provides DefaultTBusTypography,
        LocalTBusSpacing provides DefaultTBusSpacing,
        LocalTBusShapes provides DefaultTBusShapes,
        LocalTBusElevation provides DefaultTBusElevation,
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = MaterialTBusTypography,
            shapes = MaterialTBusShapes,
            content = content,
        )
    }
}

@Composable
fun tbusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    TBusTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content,
    )
}
