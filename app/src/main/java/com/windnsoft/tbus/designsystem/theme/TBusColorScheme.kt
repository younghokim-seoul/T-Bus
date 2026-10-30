package com.windnsoft.tbus.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class TBusColorScheme(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceInverse: Color,
    val content: Color,
    val contentMuted: Color,
    val contentSubtle: Color,
    val contentOnMedia: Color,
    val line: Color,
    val glassLight: Color,
    val glassLightStrong: Color,
    val glassDark: Color,
    val glassStroke: Color,
    val signal: Color,
    val route: Color,
    val clip: Color,
    val night: Color,
    val danger: Color,
)

val TBusLightColorScheme = TBusColorScheme(
    background = TBusColorTokens.WarmMist,
    surface = TBusColorTokens.Mist,
    surfaceRaised = TBusColorTokens.Cloud,
    surfaceInverse = TBusColorTokens.Ink,
    content = TBusColorTokens.Ink,
    contentMuted = TBusColorTokens.Muted,
    contentSubtle = TBusColorTokens.Subtle,
    contentOnMedia = TBusColorTokens.Cloud,
    line = TBusColorTokens.Line,
    glassLight = TBusColorTokens.LightGlass,
    glassLightStrong = TBusColorTokens.LightGlassStrong,
    glassDark = TBusColorTokens.DarkGlass,
    glassStroke = TBusColorTokens.GlassStroke,
    signal = TBusColorTokens.SignalGreen,
    route = TBusColorTokens.RouteBlue,
    clip = TBusColorTokens.WarmClip,
    night = TBusColorTokens.NightViolet,
    danger = TBusColorTokens.AlertRed,
)

val TBusDarkColorScheme = TBusLightColorScheme.copy(
    background = Color(0xFF0E1013),
    surface = Color(0xFF171A1E),
    surfaceRaised = Color(0xFF21262C),
    surfaceInverse = TBusColorTokens.Cloud,
    content = Color(0xFFF7F8FA),
    contentMuted = Color(0xFFC1C7CE),
    contentSubtle = Color(0xFF89919B),
    line = Color(0x29FFFFFF),
    glassLight = Color(0x24FFFFFF),
    glassLightStrong = Color(0x36FFFFFF),
)
