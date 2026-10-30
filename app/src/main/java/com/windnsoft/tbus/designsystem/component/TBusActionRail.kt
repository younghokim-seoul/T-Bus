package com.windnsoft.tbus.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.windnsoft.tbus.designsystem.theme.TBusTheme

@Immutable
data class TBusActionRailItem(
    val id: String,
    val label: String,
    val value: String? = null,
    val selected: Boolean = false,
    val icon: @Composable (contentColor: Color) -> Unit,
    val onClick: () -> Unit,
)

@Composable
fun TBusActionRail(
    items: List<TBusActionRailItem>,
    modifier: Modifier = Modifier,
    darkOnMedia: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items.forEach { item ->
            TBusActionRailButton(
                item = item,
                darkOnMedia = darkOnMedia,
            )
        }
    }
}

@Composable
private fun TBusActionRailButton(
    item: TBusActionRailItem,
    darkOnMedia: Boolean,
) {
    val selectedColor = if (item.selected) TBusTheme.colors.clip else TBusTheme.colors.contentOnMedia
    val contentColor = if (darkOnMedia) selectedColor else TBusTheme.colors.content
    val backgroundColor = if (darkOnMedia) {
        TBusTheme.colors.glassDark
    } else {
        TBusTheme.colors.glassLight
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.xs),
    ) {
        TBusGlassSurface(
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = item.onClick),
            shape = TBusTheme.shapes.pill,
            backgroundColor = backgroundColor,
            contentPadding = PaddingValues(TBusTheme.spacing.md),
        ) {
            item.icon(contentColor)
        }
        TBusText(
            text = item.value ?: item.label,
            color = contentColor,
            variant = TBusTextVariant.Caption,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
