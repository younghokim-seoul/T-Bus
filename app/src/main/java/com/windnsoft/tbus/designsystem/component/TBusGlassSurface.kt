package com.windnsoft.tbus.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.windnsoft.tbus.designsystem.theme.TBusTheme

@Composable
fun TBusGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = TBusTheme.shapes.cardLarge,
    backgroundColor: Color = TBusTheme.colors.glassLight,
    borderColor: Color = TBusTheme.colors.glassStroke,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(
                border = BorderStroke(width = TBusGlassBorderWidth, color = borderColor),
                shape = shape,
            )
            .padding(contentPadding),
        content = content,
    )
}

private val TBusGlassBorderWidth = 1.dp
