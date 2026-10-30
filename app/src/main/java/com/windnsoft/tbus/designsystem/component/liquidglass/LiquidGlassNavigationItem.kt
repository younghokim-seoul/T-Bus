package com.windnsoft.tbus.designsystem.component.liquidglass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class LiquidGlassNavigationItem(
    val id: String,
    val label: String,
    val icon: @Composable (selected: Boolean, contentColor: Color) -> Unit,
)
