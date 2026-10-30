package com.windnsoft.tbus.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.windnsoft.tbus.designsystem.theme.TBusTheme

@Composable
fun TBusGradientSummaryCard(
    label: String,
    title: String,
    description: String,
    palette: List<Color>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val shape = TBusTheme.shapes.cardLarge
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .background(TBusTheme.colors.glassLightStrong),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(58.dp)
                .height(112.dp)
                .background(Brush.verticalGradient(palette.ifEmpty { DefaultSummaryPalette })),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = TBusTheme.spacing.lg, vertical = TBusTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.xs),
        ) {
            TBusText(
                text = label,
                color = TBusTheme.colors.contentSubtle,
                variant = TBusTextVariant.Caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TBusText(
                text = title,
                color = TBusTheme.colors.content,
                variant = TBusTextVariant.Headline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TBusText(
                text = description,
                color = TBusTheme.colors.contentMuted,
                variant = TBusTextVariant.Label,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val DefaultSummaryPalette = listOf(
    Color(0xFF101114),
    Color(0xFF6B7178),
    Color(0xFFF4F6F8),
)
