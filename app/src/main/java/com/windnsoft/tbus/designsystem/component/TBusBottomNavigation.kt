package com.windnsoft.tbus.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.windnsoft.tbus.designsystem.component.liquidglass.LiquidGlassBottomNavigation
import com.windnsoft.tbus.designsystem.component.liquidglass.LiquidGlassNavigationAction
import com.windnsoft.tbus.designsystem.component.liquidglass.LiquidGlassNavigationItem
import com.windnsoft.tbus.designsystem.component.liquidglass.LiquidGlassRenderMode
import com.windnsoft.tbus.designsystem.component.liquidglass.rememberLiquidGlassNavigationState
import com.kyant.backdrop.Backdrop

@Composable
fun TBusBottomNavigation(
    items: List<TBusBottomNavigationItem>,
    selectedItemId: String,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    adaptiveBackgroundColor: Color = Color.Unspecified,
    renderMode: LiquidGlassRenderMode = LiquidGlassRenderMode.Automatic,
    trailingAction: LiquidGlassNavigationAction? = null,
    onItemSelected: (TBusBottomNavigationItem) -> Unit = {},
) {
    if (items.isEmpty()) return

    val navigationItems = remember(items) {
        items.map { item ->
            LiquidGlassNavigationItem(
                id = item.id,
                label = item.label,
                icon = item.icon,
            )
        }
    }
    val state = rememberLiquidGlassNavigationState(
        initialSelectedItemId = selectedItemId,
    )
    LaunchedEffect(selectedItemId) {
        if (state.selectedItemId != selectedItemId) {
            state.select(selectedItemId)
        }
    }

    LiquidGlassBottomNavigation(
        items = navigationItems,
        backdrop = backdrop,
        modifier = modifier,
        state = state,
        style = TBusBottomNavigationDefaults.style(),
        renderMode = renderMode,
        adaptiveBackgroundColor = adaptiveBackgroundColor,
        trailingAction = trailingAction,
        onItemSelected = { item ->
            items.firstOrNull { it.id == item.id }?.let(onItemSelected)
        },
    )
}
