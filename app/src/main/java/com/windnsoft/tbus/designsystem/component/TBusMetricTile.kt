package com.windnsoft.tbus.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.windnsoft.tbus.designsystem.theme.TBusTheme

@Composable
fun TBusMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    TBusGlassSurface(
        modifier = modifier,
        shape = TBusTheme.shapes.card,
        backgroundColor = TBusTheme.colors.glassLight,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 11.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.xxs)) {
            TBusText(
                text = label,
                color = TBusTheme.colors.contentSubtle,
                variant = TBusTextVariant.Caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TBusText(
                text = value,
                color = TBusTheme.colors.content,
                variant = TBusTextVariant.Label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
