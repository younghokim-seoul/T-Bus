package com.windnsoft.tbus.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.windnsoft.tbus.designsystem.theme.TBusTheme

@Composable
fun TBusSurface(
    modifier: Modifier = Modifier,
    shape: Shape = TBusTheme.shapes.card,
    color: Color = TBusTheme.colors.surfaceRaised,
    contentColor: Color = TBusTheme.colors.content,
    tonalElevation: Dp = TBusTheme.elevation.none,
    shadowElevation: Dp = TBusTheme.elevation.none,
    border: BorderStroke? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
        content = content,
    )
}

@Composable
fun TBusCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(TBusTheme.spacing.lg),
    containerColor: Color = TBusTheme.colors.surfaceRaised,
    contentColor: Color = TBusTheme.colors.content,
    border: BorderStroke? = BorderStroke(TBusCardBorderWidth, TBusTheme.colors.line),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = TBusTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        border = border,
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun TBusHorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = TBusTheme.colors.line,
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color,
    )
}

private val TBusCardBorderWidth = 1.dp
